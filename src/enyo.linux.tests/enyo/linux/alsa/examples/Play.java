package enyo.linux.alsa.examples;

import enyo.io.BufferInput;
import enyo.io.function.IOFunction;
import enyo.io.terminfo.TermFile;
import enyo.io.terminfo.UntypedStringCapability;
import enyo.linux.alsa.PCM;
import enyo.linux.alsa.SoundException;
import enyo.linux.alsa.UnderflowException;
import enyo.sound.flac.FlacException;
import enyo.sound.flac.FlacInput;
import enyo.sound.pcm.PCMFormat;
import enyo.sound.pcm.PCMTransformation;
import enyo.util.EnumBitSet;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public final class Play {
    public static void main(String[] args)
        throws IOException, InterruptedException {
        int pos = 0;
        String deviceName = args[pos++];

        PCM.Device dev = new PCM.Device(deviceName);
        try (PCM pcm = new PCM(dev)) {
            Playlist playlist;
            if (args.length == 1)
                // Restore from a crash.
                playlist = new Playlist();
            else
                // Set a new playlist.
                playlist = new Playlist(
                    Arrays.asList(args).subList(1, args.length)
                    .stream().map(Play::argToURL).toList());
            play(pcm, playlist);
        }
    }

    private static URL argToURL(String arg) {
        var file = new File(arg);
        try {
            if (arg.indexOf(':') < 0 // No schema part.
                || file.exists())
                return file.toURI().toURL();
            else
                return new URI(arg).toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    private static void play(PCM pcm, Playlist playlist)
        throws IOException, InterruptedException {
        Global global = new Global(playlist);

        DiscoveryThread discovery = new DiscoveryThread(global);
        discovery.start();
        PlayerThread player = new PlayerThread(global, pcm);
        player.start();
        ReporterThread reporter = new ReporterThread(global, player);
        reporter.start();

        try {
            // Skip part of the playlist that has already been played.
            long skipSeconds = playlist.offsetInSeconds();

            double totalSeconds = 0;
            while (true) {
                URL url = global.playlist.take();
                if (url == Global.PLAYLIST_END_MARKER)
                    break;
                totalSeconds = play(global, player, url,
                    totalSeconds, skipSeconds);
            }
            if (player.exception == null)
                player.queue.put(QueueItem.MARKER_END);
            player.join();
            {
                Throwable exc = player.exception;
                if (exc != null) {
                    if (exc instanceof RuntimeException)
                        throw (RuntimeException) exc;
                    else if (exc instanceof Error)
                        throw (Error) exc;
                    else
                        throw new RuntimeException(exc);
                }
            }
        } finally {
            reporter.interrupt();
            reporter.join();
            discovery.join();
        }
    }

    // Returns the new total sample count.
    private static double play(Global global, PlayerThread thread, URL url,
        double previousSeconds, double skipSeconds)
        throws IOException, InterruptedException {
        try (DecodedStream decoded0 = openStream(url)) {
            DecodedStream decoded = reencode(global, thread, decoded0)
                .orElseGet(() -> {
                        System.err.format("%s: unsupported: %s%n",
                            url, decoded0.format());
                        System.exit(1);
                        return null;
                    });
            int sampleSize = decoded.format.frame().frameSizeInBytes();
            long sampleCount = decoded.sampleCount;

            double newSeconds = previousSeconds
                + (double) sampleCount / decoded.format.rate();
            if (newSeconds < skipSeconds)
                // The file is skipped completely.
                return newSeconds;

            // Skip over the part that has already been played.
            long byteCount;
            if (skipSeconds > previousSeconds) {
                // Round to a whole frame/sample.
                long skipSamples = (long) ((skipSeconds - previousSeconds)
                    * decoded.format.rate());
                byteCount = skipSamples * sampleSize;
                decoded.in.skipNBytes(byteCount);
            } else
                byteCount = 0;

            PlaylistEntry entry = new PlaylistEntry(
                url, previousSeconds, decoded.format, sampleCount);

            byte[] blob = new byte[SampleData.SIZE];
            while (true) {
                ByteBuffer buffer = decoded.in.fillBuffer(0);
                if (!buffer.hasRemaining()) {
                    break;
                }

                byte[] allocated = null;
                if (buffer.remaining() >= blob.length)
                    buffer.get(blob);
                else {
                    int copied = buffer.remaining();
                    buffer.get(blob, 0, copied);
                    do {
                        buffer = decoded.in.fillBuffer(0);
                        if (!buffer.hasRemaining()) {
                            allocated = Arrays.copyOf(blob, copied);
                            break;
                        }
                        int toCopy = Math.min(blob.length - copied,
                            buffer.remaining());
                        buffer.get(blob, copied, toCopy);
                        copied += toCopy;
                    } while (copied < blob.length);
                }
                if (allocated == null)
                    allocated = blob.clone();

                // If the player thread has failed, do not try to
                // submit more data (which could eventually block
                // without this check).
                if (thread.exception != null)
                    break;

                // Submit the data for playback.
                thread.queue.put(new SampleData(
                        entry, byteCount / sampleSize, allocated));

                byteCount += blob.length;
            }

            return newSeconds;
        }
    }

    private record DecodedStream(BufferInput in, PCMFormat format,
        long sampleCount) implements Closeable {
        public void close() throws IOException {
            in.close();
        }
    }

    private static DecodedStream openStream(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        try {
            var result = openStream0(conn, in);
            in = null;
            return result;
        } finally {
            if (in != null)
                in.close();
        }
    }


    private static DecodedStream openStream0(
        URLConnection conn, InputStream in0) throws IOException {
        BufferInput in = BufferInput.wrap(in0);

        PCMFormat pcm = null;
        long sampleCount = 0;
        ByteBuffer buffer = in.fillBuffer(WaveFormat.MINIMUM_HEADER_SIZE);
        int oldPosition = buffer.position();

        IOException wavException = null;
        try {
            WaveFormat format = WaveFormat.getHeader(buffer);
            if (conn.getContentLengthLong() >= 0
                && conn.getContentLengthLong() != format.expectedFileSize())
                wavException = new IOException(
                    conn.getURL() + ": expected file size "
                    + format.expectedFileSize()
                    + ", got " + conn.getContentLengthLong());
            else {
                pcm = format.format();
                sampleCount = format.sampleCount();
            }
        } catch (IOException e) {
            wavException = e;
        }

        if (wavException != null)
            try {
                buffer.position(oldPosition);
                FlacInput flac = new FlacInput(in);
                sampleCount = flac.streaminfo().totalSamples();
                if (sampleCount == 0)
                    throw new FlacException(
                        conn.getURL() + ": unknown total samples count");
                pcm = flac.format();
                in = flac;
            } catch (Throwable e) {
                e.addSuppressed(wavException);
                throw e;
            }

        return new DecodedStream(in, pcm, sampleCount);
    }

    private static Optional<DecodedStream> reencode(
        Global global, PlayerThread player, DecodedStream stream)
        throws InterruptedException {
        // Check to see if a cached result is available (without
        // querying the PCM device).
        {
            var cached = global.formats.findTransformation(stream.format());
            if (cached.isPresent())
                return transform(stream, cached.get());
        }

        // The format is not directly supported.  Try to find a
        // supported transformation using the player thread.
        var item = new FindTransformation(stream.format());
        player.queue.put(item);
        Optional<FormatSupport.Transformation> result;
        try {
            result = item.result.get();
        } catch (ExecutionException e) {
            var e1 = e.getCause();
            if (e1 instanceof RuntimeException)
                throw (RuntimeException) e1;
            else
                throw new RuntimeException(e);
        }
        return transform(stream, result);
    }

    private static Optional<DecodedStream> transform(DecodedStream stream,
        Optional<FormatSupport.Transformation> tf) {
        return tf.map(tf1 -> new DecodedStream(
                tf1.transformer().apply(stream.in()),
                tf1.destination(),
                // The number of samples is unchanged by the transformation.
                stream.sampleCount()));
    }

    /**
     * Global state of the player.
     */
    private static class Global {
        Global(Playlist playlist) {
            staticPlaylist = playlist;
        }

        // Keeps track of support formats and their transformations.
        final FormatSupport formats = new FormatSupport();

        // Used by the discovery thread to populate the actual
        // playlist and its length.  Used by the reporter thread to
        // update the playback offset.
        final Playlist staticPlaylist;

        // The playlist, as expanded by the discovery thread.
        final LinkedBlockingQueue<URL> playlist = new LinkedBlockingQueue<>();

        // Last item in playlist.
        final static URL PLAYLIST_END_MARKER;
        static {
            try {
                PLAYLIST_END_MARKER = new URI("file:///").toURL();
            } catch (MalformedURLException | URISyntaxException e) {
                throw new InternalError(e);
            }
        }

        // This is updated by the discovery thread as more playlist
        // entries are discovered.  It is used by the reporter thread
        // to calculate the playlist playing times.
        volatile double totalSeconds;

        // This is updated by the player thread each time an underflow
        // is encountered.  It is used by the reporter thread to
        // report underflows.
        final AtomicLong underflows = new AtomicLong();
    }

    private record PlaylistEntry(URL url, double playlistSecondsOffset,
        PCMFormat format, long sampleLength) { }

    private interface QueueItem {
        // Marks the end of the data stream and requests that the
        // player thread shuts down.
        static final QueueItem MARKER_END = new QueueItem() { };
    }

    private record SampleData(PlaylistEntry entry, long entrySampleOffset,
        byte[] data) implements QueueItem {

        // The target size for the data array.
        private static final int SIZE = 16384;
    }

    /**
     * Discovers a reasonable transformation to a supported format on
     * the player thread.
     */
    private record FindTransformation(PCMFormat source,
        CompletableFuture<Optional<FormatSupport.Transformation>> result)
        implements QueueItem {
        FindTransformation(PCMFormat source) {
            this(source, new CompletableFuture<>());
        }
    }

    private static final class PlayerThread extends Thread {
        private final Global global;
        private final PCM pcm;
        final ArrayBlockingQueue<QueueItem> queue;
        volatile SampleData current;
        volatile Throwable exception;

        PlayerThread(Global global, PCM pcm) {
            super("Player");
            this.global = global;
            this.pcm = pcm;

            // Use at least 20 MB of memory for the queue, and target
            // 50% of maximum heap size.  This needs to be changed
            // once this is not the only queue (the player thread
            // queue should probably be shortest one).
            int heapPercent  = 50;
            long maxMemory = Runtime.getRuntime().maxMemory();
            if (maxMemory >= Long.MAX_VALUE / 100)
                // If this value is not available, use the currently
                // used memory as a reference, which is clearly hack,
                // but it gives us at least some number.
                maxMemory = Runtime.getRuntime().totalMemory();
            long targetBytes = maxMemory * heapPercent / 100;
            int lowerLimit = 20 * 1024 * 1024;
            if (targetBytes < lowerLimit)
                targetBytes = lowerLimit;

            // Estimate of the SampleData and array overhead.
            int overhead = 96;

            // Assume that each queue entry uses the maximum size for
            // its array.  Cap the queue length at a fraction of the
            // maximum array length.
            int queueLength = (int) Math.min(
                targetBytes / (SampleData.SIZE + overhead),
                Integer.MAX_VALUE / 16);

            queue = new ArrayBlockingQueue<>(queueLength);
        }

        @Override
        public void run() {
            PCMFormat pcmFormat = null;
            ByteBuffer buffer = null;

            try {
                while (true) {
                    SampleData item;
                    {
                        QueueItem item0 = queue.take();
                        if (item0 == QueueItem.MARKER_END) {
                            if (pcmFormat != null)
                                drain(buffer);
                            current = null;
                            break;
                        }
                        if (item0.getClass() != SampleData.class) {
                            handleNonSampleData(item0);
                            // Non-sample processing can reconfigure
                            // the playback device.
                            pcmFormat = null;
                            continue;
                        }
                        item = (SampleData) item0;
                    }

                    // Do not use queuing with the reporter thread, to
                    // avoid blocking.
                    current = item;

                    // If the format changes, adjust the PCM device.
                    {
                        var currentFormat = item.entry.format;
                        if (currentFormat != pcmFormat
                            && !currentFormat.equals(pcmFormat)) {
                            if (pcmFormat != null)
                                drain(buffer);
                            var prep = FormatSupport.prepare(
                                pcm, currentFormat);
                            pcmFormat = currentFormat;

                            if (buffer == null
                                || buffer.capacity() != prep.periodBytes())
                                buffer = ByteBuffer.allocateDirect(
                                    prep.periodBytes());
                        }
                    }

                    // It is necessary to-repacketize the audio data
                    // because with just two periods per buffer, it is
                    // necessary to fill one period exactly before
                    // starting the next one.  Otherwise, buffer
                    // underruns occur.
                    for (int dataPos = 0; dataPos < item.data.length; ) {
                        int dataRemaining = item.data.length - dataPos;
                        int toCopy = Math.min(
                            dataRemaining, buffer.remaining());
                        buffer.put(item.data, dataPos, toCopy);
                        if (!buffer.hasRemaining())
                            play(buffer);
                        dataPos += toCopy;
                    }
                }
            } catch (InterruptedException e) {
                // Exit this thread.
            } catch (Throwable e) {
                exception = e;
                // Wake up the producer.
                queue.clear();
            }
        }

        private void handleNonSampleData(QueueItem item) {
            if (item.getClass() == FindTransformation.class)
                findTransformation((FindTransformation) item);
        }

        private void findTransformation(FindTransformation ctx) {
            Optional<FormatSupport.Transformation> tf;
            try {
                tf = global.formats.findTransformation(pcm, ctx.source());
            } catch (Throwable e) {
                ctx.result.completeExceptionally(e);
                return;
            }
            ctx.result.complete(tf);
        }

        // Writes the data up to buffer.position() to the device.
        // Afterwards, reset the buffer position and the limit to the
        // capacity.
        private void play(ByteBuffer buffer) {
            buffer.flip();
            while (buffer.hasRemaining())
                try {
                    pcm.writeInterleaved(buffer);
                } catch (UnderflowException e) {
                    global.underflows.incrementAndGet();
                    pcm.prepare();
                }
            buffer.clear();
        }

        // Plays the buffer contents and write until all playing has
        // completed.
        private void drain(ByteBuffer buffer) {
            if (buffer != null)
                play(buffer);
            pcm.drain();
        }
    }

    private static final class ReporterThread extends Thread {
        private final Global global;
        private final PlayerThread player;
        private final Capabilities capabilities = new Capabilities();

        // The queue item from the previous loop iteration.  Used to
        // skip updates if there are no changes.
        private SampleData last;

        // Seconds for the current playlist entry.  Used to skip
        // updates if unchanged.
        private long currentSeconds = -1;
        private long totalSeconds = -1; // Written to by DiscoveryThread.
        private long remainingSeconds = -1;

        // The same, but for the entire playlist.
        private long fullCurrentSeconds = -1;
        private long fullTotalSeconds = -1;
        private long fullRemainingSeconds = -1;

        private final StringBuilder stringBuilder = new StringBuilder();

        ReporterThread(Global global, PlayerThread player) {
            super("Reporting player status");
            this.global = global;
            this.player = player;
        }

        @Override
        public void run() {
            long lastSeconds = -1;
            long lastUnderflows = 0;

            // Local cache of global.playlist, to avoid volatile
            // reads.
            Playlist playlist = null;

            while (true) {
                try {
                    // Hopefully a 100 ms delay results in fairly
                    // smooth updates.
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }

                {
                    long newUnderflows = global.underflows.get();
                    if (newUnderflows != lastUnderflows) {
                        capabilities.clearLine();
                        synchronized (System.out) {
                            System.out.println("warning: underflow detected");
                        }
                        lastUnderflows = newUnderflows;
                        lastSeconds = -1; // Trigger timestamp update.
                    }
                }

                SampleData current = player.current;
                if (current == null) {
                    synchronized (System.out) {
                        capabilities.clearLine();
                    }
                    last = null;
                    continue;
                }

                if (current == last)
                    continue;
                if (last == null || current.entry != last.entry) {
                    outputNewPath(current);
                    lastSeconds = -1; // Trigger timestamp update.
                }

                // None of this takes the kernel buffer size into
                // account, so it must not be too large.

                // Obtain a snapshot, so that the numbers are consistent.
                double totalSeconds0 = global.totalSeconds;

                if (capabilities.supported()
                    && checkForTimeChange(current, totalSeconds0))
                    synchronized (System.out) {
                        capabilities.clearLine();
                        stringBuilder.delete(0, stringBuilder.length());
                        appendMS(currentSeconds);
                        stringBuilder.append(" of ");
                        appendMS(totalSeconds);
                        stringBuilder.append(" (");
                        appendMS(remainingSeconds);
                        stringBuilder.append("), total: ");
                        appendMS(fullCurrentSeconds);
                        stringBuilder.append(" of ");
                        appendMS(fullTotalSeconds);
                        stringBuilder.append(" (");
                        appendMS(fullRemainingSeconds);
                        stringBuilder.append(')');
                        System.out.append(stringBuilder);
                        System.out.flush();
                    }

                // Try to obtain the global playlist object.
                if (playlist == null)
                    playlist = global.staticPlaylist;

                // Update the playback offset in the saved playlist.
                if (playlist != null
                    && fullCurrentSeconds < Integer.MAX_VALUE)
                    playlist.offsetInSeconds((int) fullCurrentSeconds);

                last = current;
            }
            synchronized (System.out) {
                capabilities.clearLine();
            }
        }

        // Writes the new path to System.out.  This message is not
        // overwritten by subsequent timestamp updates.
        private void outputNewPath(SampleData current) {
            synchronized (System.out) {
                capabilities.clearLine();

                abbreviatedURL(current).ifPresentOrElse(
                    shortened -> System.out.format("info: %s/%s%n",
                        pathPlaceholder(), shortened),
                    () -> System.out.format(
                        "info: %s%n", current.entry.url()));
                System.out.flush();
            }
        }

        // Returns the URL spec for abbreviation, or null if it is not
        // availab.e
        private static String getAbbreviatedSpec(URL url) {
            String path = url.getPath();
            int slash = path.lastIndexOf('/');
            if (slash >= 0 && slash != path.length() - 1)
                return path.substring(slash + 1);
            else if (!path.isEmpty())
                return path;
            else
                return null;
        }

        // Returns the abbreviated string (by sharing the prefix) if one
        // is available.
        private Optional<String> abbreviatedURL(SampleData current) {
            String spec = getAbbreviatedSpec(current.entry.url());
            try {
                if (last != null && spec != null
                    && last.entry.url().toURI().resolve(spec).toURL()
                    .equals(current.entry.url()))
                    return Optional.of(spec);
            } catch (URISyntaxException | MalformedURLException e) {
                // Fall through.
            }
            return Optional.empty();
        }

        // Returns the placeholder for the shortened path.  Use
        // Unicode if possible.
        private static String pathPlaceholder() {
            if (Charset.defaultCharset().equals(StandardCharsets.UTF_8))
                return "\u2026";
            else
                return "...";
        }

        private boolean checkForTimeChange(
            SampleData current, double newFullTotalSeconds0) {
            int framesPerSecond = current.entry.format().rate();
            long newCurrentSeconds = current.entrySampleOffset
                / framesPerSecond;
            long newTotalSeconds = current.entry.sampleLength
                / framesPerSecond;
            long newRemainingSeconds =
                (current.entry.sampleLength - current.entrySampleOffset)
                / framesPerSecond;

            // The global timing data (across the playlist).
            double newFullCurrentSeconds0 =
                current.entry.playlistSecondsOffset
                + (double) current.entrySampleOffset / framesPerSecond;
            long newFullCurrentSeconds = (long) newFullCurrentSeconds0;
            long newFullTotalSeconds = (long) newFullTotalSeconds0;
            long newFullRemainingSeconds =
                (long) (newFullTotalSeconds0 - newFullCurrentSeconds0);

            boolean updated = newCurrentSeconds != currentSeconds
                || newTotalSeconds != totalSeconds
                || newRemainingSeconds != remainingSeconds
                || newFullCurrentSeconds != fullCurrentSeconds
                || newFullTotalSeconds != fullTotalSeconds
                || newFullRemainingSeconds != fullRemainingSeconds;
            currentSeconds = newCurrentSeconds;
            totalSeconds = newTotalSeconds;
            remainingSeconds = newRemainingSeconds;
            fullCurrentSeconds = newFullCurrentSeconds;
            fullTotalSeconds = newFullTotalSeconds;
            fullRemainingSeconds = newFullRemainingSeconds;
            return updated;
        }

        private void appendMS(long seconds) {
            long minutes = seconds / 60;
            seconds = seconds % 60;
            stringBuilder.append(minutes);
            stringBuilder.append(':');
            if (seconds < 10)
                stringBuilder.append('0');
            stringBuilder.append(seconds);
        }
    }

    /**
     * Cached terminal capabilities.
     */
    private static final class Capabilities {
        private final byte[] carriageReturn;
        private final byte[] clearToEOL;

        Capabilities() {
            TermFile term = TermFile.current();
            carriageReturn = term.get(UntypedStringCapability.CARRIAGE_RETURN);
            clearToEOL = term.get(UntypedStringCapability.CLR_EOL);
        }

        boolean supported() {
            return carriageReturn != null && clearToEOL != null;
        }

        void clearLine() {
            if (!supported())
                return;
            try {
                System.out.write(carriageReturn);
                System.out.write(clearToEOL);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static final class DiscoveryThread extends Thread {
        private final Global global;

        DiscoveryThread(Global global) {
            super("File discovery thread");
            this.global = global;
        }

        @Override
        public void run() {
            expandList(global.staticPlaylist.files());
            try {
                global.playlist.put(Global.PLAYLIST_END_MARKER);
            } catch (InterruptedException e) {
            }
        }

        private void expandList(List<? extends URL> unsorted) {
            for (URL url : sorted(unsorted)) {
                {
                    var directoryFile = getDirectory(url);
                    if (directoryFile != null) {
                        expandDirectory(directoryFile);
                        continue;
                    }
                }
                try {
                    var conn = url.openConnection();
                    if (isHtml(conn)) {
                        var list = parseHtml(url, conn);
                        conn.getInputStream().close();
                        expandList(list);
                        continue;
                    }
                    try (var decoded = openStream0(
                            conn, conn.getInputStream())) {
                        double length = (double) decoded.sampleCount
                            / decoded.format.rate();
                        // Only this thread is writing to global.totalSeconds,
                        // so no synchronization is needed.
                        global.totalSeconds += length;
                    } finally {
                        conn.getInputStream().close();
                    }
                    try {
                        global.playlist.put(url);
                    } catch (InterruptedException e) {
                        break;
                    }
                } catch (IOException e) {
                    // Will eventually be reported by the player thread.
                }
            }
        }

        private void expandDirectory(File directory) {
            expandList(Arrays.stream(directory.listFiles())
                .map(IOFunction.of(file -> file.toURI().toURL()))
                .toList());
        }

        private static boolean isHtml(URLConnection conn) {
            var html = "text/html";
            var ctype = conn.getContentType().trim();
            if (html.equals(ctype))
                return true;
            int semicolon = ctype.indexOf(';');
            if (semicolon < 0)
                return false;
            return html.equals(ctype.substring(0, semicolon).trim());
        }

        private static List<URL> parseHtml(
            URL url, URLConnection conn) throws IOException {
            var in = conn.getInputStream();
            String pattern = "<a href=\"";
            int patternIdx = 0;
            var sb = new StringBuilder();
            var result = new ArrayList<URL>();
            OUTER: while (true) {
                int b = in.read();
                if (b < 0)
                    break OUTER;
                if (pattern.charAt(patternIdx) == b) {
                    ++patternIdx;
                    if (patternIdx < pattern.length())
                        continue;

                    sb.delete(0, sb.length());
                    while (true) {
                        b = in.read();
                        if (b < 0)
                            break OUTER;
                        if (b == '"')
                            break;
                        // We assume that paths are just ASCII, with
                        // %-encoded URLs.
                        sb.append((char) b);
                    }
                    var href = sb.toString();
                    if (!href.startsWith("?")
                        && !href.startsWith("/")
                        && !href.isEmpty())
                        try {
                            var suburl = conn.getURL().toURI()
                                .resolve(href).toURL();
                            result.add(suburl);
                        } catch (URISyntaxException e) {
                            throw new IOException(e);
                        }
                }
                patternIdx = 0;
            }
            return result;
        }

        private static <T extends URL> List<T> sorted(List<T> urls) {
            return urls.stream()
                .sorted((a, b) -> basename(a).compareTo(basename(b)))
                .toList();
        }

        private static String basename(URL url) {
            String path = url.getPath();
            if (path.isEmpty()) {
                int slash = path.lastIndexOf('/');
                if (slash < path.length())
                    return path.substring(slash + 1);
            }
            return url.toString();
        }

        private static File getDirectory(URL url) {
            if ("file".equals(url.getProtocol()))
                try {
                    var file = new File(url.toURI());
                    if (file.isDirectory())
                        return file;
                } catch (URISyntaxException e) {
                    // Treat as non-directory.
                }
            return null;
        }
    }
}
