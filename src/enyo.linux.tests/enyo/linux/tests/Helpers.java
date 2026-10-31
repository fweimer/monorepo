package enyo.linux.tests;

import enyo.linux.SyscallException;
import enyo.testing.Check;

class Helpers {
    static final Check<SyscallException> errno(int errno) {
        return e -> e.errno() == errno ? null :
            new Check.Failure("SyscallException errno value does not match",
                errno, e.errno(), e.toString());
    }
}
