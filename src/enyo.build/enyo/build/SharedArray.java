package enyo.build;

import enyo.io.SharedIntArray;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Shared array used to communicate as part of the test link.
 */
final class SharedArray {
    /**
     * Creates a new shared array for test link communication.
     */
    SharedArray(Path path) throws IOException {
        sharedArray = SharedIntArray.open(path, Slot.values().length,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE);
    }

    /**
     * Elements of the shared array.
     */
    enum Slot {
        /**
         * Test stop requested.  Initially 0.  The controller sets
         * this slot to 1 to indicate that testing should exit
         * prematurely.
         */
        STOP_REQUESTED,
        ;
    };

    /**
     * Returns the value of the slot.
     */
    int get(Slot slot) {
        return sharedArray.get(slot.ordinal());
    }

    /**
     * Sets the slot to a new value.
     */
    void set(Slot slot, int value) {
        sharedArray.set(slot.ordinal(), value);
    }

    private final SharedIntArray sharedArray;
}
