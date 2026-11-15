package enyo.linux.alsa.tests;

import enyo.linux.SyscallException;
import enyo.linux.alsa.Control;
import enyo.linux.alsa.PCM;
import enyo.linux.alsa.TLV;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ShowCard {
    public static void main(String[] args) {
        int card;
        try {
            card = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.format("usage: %s CARD-NUMBER%n",
                ShowCard.class.getName());
            System.exit(1);
            return;
        }

        try (Control control = new Control(card)) {
            dump(control);

            List<Control.Element> elements = control.elements();
            if (elements.isEmpty()) {
                System.out.println();
                System.out.println("Card has no elements.");
            } else
                for (Control.Element element : elements) {
                    System.out.println();
                    dump(control, control.elementInfo(element));
                }

            List<PCM.Device> pcms = PCM.devices().stream()
                .filter(dev -> dev.card() == control.number())
                .collect(Collectors.toList());
            if (pcms.isEmpty()) {
                System.out.println();
                System.out.println("Card has no PCM devices.");
            } else
                for (PCM.Device pcm : pcms) {
                    System.out.println();
                    dump(pcm);
                }
        }
    }

    private static void dump(Control control) {
        Control.CardInfo info = control.info();
        System.out.format("id: %s%n", info.id());
        System.out.format("driver: %s%n", info.driver());
        System.out.format("name: %s%n", info.name());
        System.out.format("  %s%n", info.longName());
        System.out.format("mixer: %s%n", info.mixerName());
        List<String> components = info.components();
        if (!components.isEmpty())
            for (String component : components)
                System.out.format("  %s%n", component);
    }

    private static void dump(Control control, Control.ElementInfo element) {
        System.out.format("Element #%d:%n", element.element().id());
        System.out.format("  interface: %s%n",
            element.element().interfaceType());
        System.out.format("  device: %d%n", element.element().device());
        System.out.format("  subdevice: %d%n", element.element().subdevice());
        System.out.format("  name: %s%n", element.element().name());
        System.out.format("  index: %d%n", element.element().index());
        System.out.format("  type: %s%n", element.type());
        System.out.format("  access: %s%n",
            element.access().stream().sorted().collect(Collectors.toList()));
        System.out.format("  count: %d%n", element.count());
        System.out.format("  owner: %d%n", element.owner());

        boolean readable = element.access().contains(
            Control.ElementInfo.Access.READ);

        if (element.type().equals(Control.ElementInfo.Type.E.INTEGER)
            || element.type().equals(Control.ElementInfo.Type.E.INTEGER64)) {
            System.out.format("  min: %d%n", element.min());
            System.out.format("  max: %d%n", element.max());
            System.out.format("  step: %d%n", element.step());
            if (readable)
                System.out.format("  values: %s%n",
                    Arrays.stream(control.longValues(element))
                    .boxed()
                    .collect(Collectors.toList()));
        } else if (element.type().equals(Control.ElementInfo.Type.E.BOOLEAN)) {
            if (readable) {
                    boolean[] values = control.booleanValues(element);
                    System.out.format("  values: %s%n",
                        IntStream.range(0, values.length)
                        .mapToObj(i -> values[i])
                        .collect(Collectors.toList()));
            }
        }

        if (element.access().contains(Control.ElementInfo.Access.TLV_READ)) {
            TLV tlv = control.readTlv(element);
            ByteBuffer contents = tlv.contents().asByteBuffer();
            System.out.format("  TLV: type=%s, length=%d%n",
                tlv.type(), contents.remaining());
            if (contents.hasRemaining()) {
                System.out.print("   ");
                do
                    System.out.format(" %02x",
                        Byte.toUnsignedInt(contents.get()));
                while (contents.hasRemaining());
                System.out.println();
            }
        }
    }

    private static void dump(PCM.Device device) {
        PCM pcm;
        try {
            pcm = new PCM(device);
        } catch (SyscallException e) {
            System.out.format("PCM device %d (%s): Cannot open: %d%n",
                device.device(), device.direction().toString().toLowerCase(),
                e.errno());
            return;
        }
        try {
            System.out.format("PCM device %d (%s), %s:%n",
                device.device(), device.direction().toString().toLowerCase(),
                device.toString());
            System.out.format("  protocol version: 0x%x%n",
                pcm.protocolVersion());

            PCM.Info info = pcm.info();
            System.out.format("  device: %d%n", info.device());
            System.out.format("  subdevice: %d%n", info.subdevice());
            System.out.format("  stream: %s%n", info.stream());
            System.out.format("  card: %d%n", info.subdevice());
            System.out.format("  id: %s%n", info.id());
            System.out.format("  name: %s%n", info.name());
            System.out.format("  subdevice name: %s%n", info.subdeviceName());
            System.out.format("  device class: %s%n", info.deviceClass());
            System.out.format("  subdevices count: %d%n",
                info.subdevicesCount());
            System.out.format("  subdevices available: %d%n",
                info.subdevicesAvailable());
            System.out.print("  sync:");
            for (byte b : info.sync())
                System.out.format(" %02x", Byte.toUnsignedInt(b));
            System.out.println();

            PCM.Params params = new PCM.Params();
            pcm.refine(params);
            dump(params);
        } finally {
            pcm.close();
        }
    }

    static void dump(PCM.Params params) {
        System.out.format("  parameter flags: %s%n", params.flags());
        System.out.format("  access: %s%n", params.access());
        System.out.format("  format: %s%n", params.format());
        System.out.format("  subformat: %s%n", params.subformat());
        for (PCM.IntervalIndex ival : PCM.IntervalIndex.values())
            System.out.format("  %s: %s%n",
                ival.name().toLowerCase().replace('_', ' '),
                params.interval(ival));
        System.out.format("  request masks: 0x%x%n", params.requestedMask());
        System.out.format("  changed masks: 0x%x%n", params.changedMask());
        System.out.format("  info: 0x%x%n", params.info());
        System.out.format("  most significant bits: 0x%x%n", params.msBits());
        System.out.format("  rate numerator: %d%n", params.rateNumerator());
        System.out.format("  rate denominator: %d%n", params.rateDenominator());
        System.out.format("  FIFO size: %d%n", params.fifoSize());
    }

    static void dump(PCM.SoftwareParams params) {
        System.out.format("  timestamp mode: %s%n", params.timestampMode());
        System.out.format("  frame limit for wakeup: %s%n",
            params.frameLimitForWakeup());
        System.out.format("  start threshold: %s%n", params.startThreshold());
        System.out.format("  stop threshold: %s%n", params.stopThreshold());
        System.out.format("  silence threshold: %s%n", params.silenceThreshold());
        System.out.format("  silence size: %s%n", params.silenceSize());
        System.out.format("  boundary: %s%n", params.boundary());
        System.out.format("  version: 0x%x%n", params.version());
        System.out.format("  timestamp type: %s%n", params.timestampType());
    }

    private ShowCard() { } // No instances.
}
