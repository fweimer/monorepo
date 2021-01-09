package enyo.io;

import java.io.Reader;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.List;

/**
 * A reader for CSV (Comma-Separated Values) files.
 * @see <a href="https://tools.ietf.org/html/rfc4180">RFC 4180</a>
 */
public final class CSVReader {
    /**
     * Format configuration for {@link CSVReader}s.
     */
    public static final class Configuration {
        /**
         * Creates an new configuration object with default values.
         */
        public Configuration() {
        }

        /**
         * Treatment of byte order marks (BOMs) for {@link CSVReader}s.
         */
        public enum ByteOrderMark {
            /**
             * Indicates that a byte-order mark should be preserved
             * in all cases, and returned as field values.
             */
            PRESERVE,

            /**
             * Indicates that one byte-order mark at the start of the file
             * (first row, first column) should be dropped.
             */
            CONSUME_ONCE_AT_START,
        }

        /**
         * Sets the CSV column separator character.
         * The default is {@code ','}.
         * @param separator the separator to use
         * @return this object
         */
        public Configuration separator(char separator) {
            this.separator = separator;
            return this;
        }

        /**
         * Sets the CSV quotation mark character.
         * The default is {@code '"'}.
         * @param quote the quotation mark to use
         * @return this object
         */
        public Configuration quote(char quote) {
            this.separator = quote;
            return this;
        }

        /**
         * Sets the treatment of byte-order marks (BOMs).
         * The default treatment is {@link ByteOrderMark#PRESERVE}.
         * @param byteOrderMark the requested treatment
         * @return this object
         */
        public Configuration byteOrderMark(ByteOrderMark byteOrderMark) {
            this.byteOrderMark = Objects.requireNonNull(byteOrderMark);
            return this;
        }

        private char separator = ',';
        private char quote = '"';
        private ByteOrderMark byteOrderMark = ByteOrderMark.PRESERVE;
    }

    /**
     * Creates a new CSV reader with the default
     * {@linkplain Configuration configuration}.
     * @param reader the source of the underlying data
     * @throws IOException if an input error occurred
     */
    public CSVReader(Reader reader) throws IOException {
        this(reader, new Configuration());
    }

    /**
     * Creates a new CSV reader with the specified configuration.
     * @param reader the source of the underlying data
     * @param configuration the format configuration to use
     * @throws IOException if an input error occurred
     */
    public CSVReader(Reader reader, Configuration configuration)
        throws IOException {
        this.reader = Objects.requireNonNull(reader);
        this.separator = configuration.separator;
        this.quote = configuration.quote;
        switch (configuration.byteOrderMark) {
        case PRESERVE:
            break;
        case CONSUME_ONCE_AT_START:
            consumeByteOrderMark();
        }
    }

    /**
     * Advances the read position to the next row.
     * Remaining columns on the current row (if any) are discarded.
     * @return true if another row (possibly empty) is available, false
     *   if the end of the file has been reached
     * @throws IOException if an input error occurred
     */
    public boolean nextRow() throws IOException {
        if (atEOF)
            return false;
        if (rowNumber >= 0)
            while (!atEOL)
                skipColumn();
        ++rowNumber;
        atEOL = false;
        lastColumn = -1;
        if (peek()) {
            if (lineNumber == 0)
                lineNumber = 1;
            lineOfRow = lineNumber;
            return true;
        } else {
            atEOF = true;
            return false;
        }
    }

    /**
     * Returns true if more columns are available in the current row.
     * @return true if the current row has more columns to read,
     *   false if not
     * @throws IOException if an input error occurred
     * @throws IllegalStateException if no row has been
     *   {@linkplain #nextRow() read} or end-of-file has been reached
     */
    public boolean moreColumns() throws IOException {
        checkForRow();
        return !atEOL && peek();
    }

    /**
     * Reads a column and returns its value.
     * Unquoted empty columns are returned as empty strings ({@code ""}).
     * @return a string, possibly empty
     * @throws IOException if an input error occurred
     * @throws IllegalStateException if no row has been
     *   {@linkplain #nextRow() read} or end-of-file has been reached
     * @see #fetchOptionalColumn()
     */
    public String fetchColumn() throws IOException {
        checkForRow();
        copyColumn();
        String result = builder.toString();
        builder.setLength(0);
        return result;
    }

    /**
     * Advances to the next row and returns its columns as a list.
     * @return an empty optional value if there are no more rows;
     *   or a list of strings containing the column values
     * @throws IOException if an input error occurred, including malformed data
     * @see #fetchRow()
     */
    public Optional<List<String>> fetchRow() throws IOException {
        if (!nextRow())
            return Optional.empty();
        var row = new ArrayList<String>();
        while (moreColumns()) {
            row.add(fetchColumn());
        }
        return Optional.of(List.copyOf(row));
    }

    /**
     * Reads a column and returns its value.
     * An empty column without
     * {@linkplain Configuration#quote(char) quotation marks}
     * is treated as an absent optional value; other column values
     * are returned as strings.
     *
     * <p>The <i>optional</i> aspect refers to the return type;
     * the column must always exist within the current row.
     * @return an optional string whose value is absent if the column
     *   was completely empty
     * @throws IOException if an input error occurred, including malformed data
     * @throws IllegalStateException if no row has been
     *   {@linkplain #nextRow() read} or end-of-file has been reached
     * @see #fetchColumn()
     */
    public Optional<String> fetchOptionalColumn() throws IOException {
        String value = fetchColumn();
        if (value.isEmpty() && !wasQuoted)
            return Optional.empty();
        else
            return Optional.of(value);
    }

    /**
     * Returns whether the last column read was quoted.
     * @return true if the last column that was read was surrounded
     *   in {@linkplain Configuration#quote(char) quotation marks},
     *   false otherwise
     * @see #lastColumnIndex()
     * @throws IllegalStateException if no row has been
     *   {@linkplain #nextRow() read} or end-of-file has been reached
     */
    public boolean lastColumnWasQuoted() {
        checkForRow();
        return wasQuoted;
    }

    /**
     * Returns the index of the column that was read last.
     * @return the column number, the first column has number 0;
     *   if no column has been read on the current row, -1 is returned
     * @see #lastColumnWasQuoted()
     * @throws IllegalStateException if no row has been
     *   {@linkplain #nextRow() read} or end-of-file has been reached
     */
    public int lastColumnIndex() {
        checkForRow();
        return lastColumn;
    }

    /**
     * Returns the number of the row currently being read.
     * Row and line numbers can be distinct if quoted colunn text
     * contains a newline character.
     * @return the line number, starting at 0
     * @throws IllegalStateException if no row has been
     *   {@linkplain #nextRow() read} or end-of-file has been reached
     * @see #rowNumber()
     */
    public long lineNumber() {
        checkForRow();
        return lineOfRow;
    }

    /**
     * Returns the number of the line currently being read.
     * Row and line numbers can be distinct if quoted colunn text
     * contains a newline character.
     *
     * <p>After {@link #nextRow()} has returned false, this function
     * returns the total number of rows that have been read, which can
     * be zero.
     * @return the row number, starting at 0
     * @throws IllegalStateException
     *   if {@link #nextRow()} has not been called
     * @see #lineNumber()
     */
    public long rowNumber() {
        checkForAnyRow();
        return rowNumber;
    }

    private final Reader reader;
    private final char separator;
    private final char quote;
    private final StringBuilder builder = new StringBuilder();
    private long lineNumber;
    private long lineOfRow; // Start of current row.
    private long rowNumber = -1;
    private int lastColumn;
    private boolean atEOL;
    private boolean atEOF;
    private int peekCh = -1;
    private boolean wasQuoted;

    private void checkForAnyRow() {
        if (rowNumber < 0)
            throw new IllegalStateException("No row has been read");
    }

    private void checkForRow() {
        checkForAnyRow();
        if (atEOF)
            throw new IllegalStateException("End of file has been reached");
    }

    private boolean peek() throws IOException {
        if (peekCh >= 0)
            return true;
        peekCh = reader.read();
        return peekCh >= 0;
    }

    private void consumeByteOrderMark() throws IOException {
        if (peek() && peekCh == '\uFEFF')
            peekCh = -1;
    }

    private void skipColumn() throws IOException {
        copyColumn();
        builder.setLength(0);
    }

    private void copyColumn() throws IOException {
        if (atEOL)
            throw new IOException(
                "End of line when column was expected");
        if (!peek())
            throw new EOFException("EOF when column was expected");

        int ch = peekCh;
        peekCh = -1;
        wasQuoted = false;

        if (ch == '\n') {
            atEOL = true;
            ++lineNumber;
        } else if (ch == separator)
            // Nothing to copy.
            ;
        else if (ch != quote) {
            builder.append((char) ch);
            UNQUOTED: while (true) {
                ch = reader.read();
                if (ch < 0)
                    throw new EOFException("Missing line terminator");
                else if (ch == '\n') {
                    ++lineNumber;
                    atEOL = true;
                    break UNQUOTED;
                } else if (ch == separator)
                    break UNQUOTED;
                else if (ch == quote)
                    throw new IOException(
                        "Quote character in the middle of a column");
                builder.append((char) ch);
            }
        } else {
            QUOTED: while (true) {
                ch = reader.read();
                if (ch < 0)
                    throw new EOFException("EOF in quoted string");
                if (ch == quote) {
                    ch = reader.read();
                    if (ch < 0)
                        throw new EOFException("Missing line terminator");
                    else if (ch == '\n') {
                        ++lineNumber;
                        atEOL = true;
                        break QUOTED;
                    } else if (ch == separator)
                        break QUOTED;
                    else if (ch == quote)
                        builder.append((char) ch);
                    else
                        throw new IOException("Invalid character after quote");
                } else {
                    if (ch == '\n')
                        ++lineNumber;
                    builder.append((char) ch);
                }
            }
            wasQuoted = true;
        }
        ++lastColumn;
    }
}
