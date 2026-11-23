// Auto-generated.  Do not edit.
package enyo.linux;

final class Mman {
    private Mman() {} // No instances.

    public final static int HUGETLB_FLAG_ENCODE_16GB;
    public final static int HUGETLB_FLAG_ENCODE_16KB;
    public final static int HUGETLB_FLAG_ENCODE_16MB;
    public final static int HUGETLB_FLAG_ENCODE_1GB;
    public final static int HUGETLB_FLAG_ENCODE_1MB;
    public final static int HUGETLB_FLAG_ENCODE_256MB;
    public final static int HUGETLB_FLAG_ENCODE_2GB;
    public final static int HUGETLB_FLAG_ENCODE_2MB;
    public final static int HUGETLB_FLAG_ENCODE_32MB;
    public final static int HUGETLB_FLAG_ENCODE_512KB;
    public final static int HUGETLB_FLAG_ENCODE_512MB;
    public final static int HUGETLB_FLAG_ENCODE_64KB;
    public final static int HUGETLB_FLAG_ENCODE_8MB;
    public final static int HUGETLB_FLAG_ENCODE_MASK;
    public final static int HUGETLB_FLAG_ENCODE_SHIFT;
    public final static int MAP_ANONYMOUS;
    public final static int MAP_DENYWRITE;
    public final static int MAP_DROPPABLE;
    public final static int MAP_EXECUTABLE;
    public final static int MAP_FILE;
    public final static int MAP_FIXED;
    public final static int MAP_FIXED_NOREPLACE;
    public final static int MAP_GROWSDOWN;
    public final static int MAP_HUGETLB;
    public final static int MAP_HUGE_16GB;
    public final static int MAP_HUGE_16KB;
    public final static int MAP_HUGE_16MB;
    public final static int MAP_HUGE_1GB;
    public final static int MAP_HUGE_1MB;
    public final static int MAP_HUGE_256MB;
    public final static int MAP_HUGE_2GB;
    public final static int MAP_HUGE_2MB;
    public final static int MAP_HUGE_32MB;
    public final static int MAP_HUGE_512KB;
    public final static int MAP_HUGE_512MB;
    public final static int MAP_HUGE_64KB;
    public final static int MAP_HUGE_8MB;
    public final static int MAP_HUGE_MASK;
    public final static int MAP_HUGE_SHIFT;
    public final static int MAP_LOCKED;
    public final static int MAP_NONBLOCK;
    public final static int MAP_NORESERVE;
    public final static int MAP_POPULATE;
    public final static int MAP_PRIVATE;
    public final static int MAP_SHARED;
    public final static int MAP_SHARED_VALIDATE;
    public final static int MAP_STACK;
    public final static int MAP_SYNC;
    public final static int MAP_TYPE;
    public final static int MAP_UNINITIALIZED;
    public final static int PROT_EXEC;
    public final static int PROT_GROWSDOWN;
    public final static int PROT_GROWSUP;
    public final static int PROT_NONE;
    public final static int PROT_READ;
    public final static int PROT_SEM;
    public final static int PROT_WRITE;

    static {
        String osArch = System.getProperty("os.arch");

        HUGETLB_FLAG_ENCODE_16GB = -2013265920;
        HUGETLB_FLAG_ENCODE_16KB = 939524096;
        HUGETLB_FLAG_ENCODE_16MB = 1610612736;
        HUGETLB_FLAG_ENCODE_1GB = 2013265920;
        HUGETLB_FLAG_ENCODE_1MB = 1342177280;
        HUGETLB_FLAG_ENCODE_256MB = 1879048192;
        HUGETLB_FLAG_ENCODE_2GB = 2080374784;
        HUGETLB_FLAG_ENCODE_2MB = 1409286144;
        HUGETLB_FLAG_ENCODE_32MB = 1677721600;
        HUGETLB_FLAG_ENCODE_512KB = 1275068416;
        HUGETLB_FLAG_ENCODE_512MB = 1946157056;
        HUGETLB_FLAG_ENCODE_64KB = 1073741824;
        HUGETLB_FLAG_ENCODE_8MB = 1543503872;
        HUGETLB_FLAG_ENCODE_MASK = 63;
        HUGETLB_FLAG_ENCODE_SHIFT = 26;
        MAP_ANONYMOUS = 32;
        MAP_DENYWRITE = 2048;
        MAP_DROPPABLE = 8;
        MAP_EXECUTABLE = 4096;
        MAP_FILE = 0;
        MAP_FIXED = 16;
        MAP_FIXED_NOREPLACE = 1048576;
        MAP_GROWSDOWN = 256;
        MAP_HUGETLB = 262144;
        MAP_HUGE_16GB = -2013265920;
        MAP_HUGE_16KB = 939524096;
        MAP_HUGE_16MB = 1610612736;
        MAP_HUGE_1GB = 2013265920;
        MAP_HUGE_1MB = 1342177280;
        MAP_HUGE_256MB = 1879048192;
        MAP_HUGE_2GB = 2080374784;
        MAP_HUGE_2MB = 1409286144;
        MAP_HUGE_32MB = 1677721600;
        MAP_HUGE_512KB = 1275068416;
        MAP_HUGE_512MB = 1946157056;
        MAP_HUGE_64KB = 1073741824;
        MAP_HUGE_8MB = 1543503872;
        MAP_HUGE_MASK = 63;
        MAP_HUGE_SHIFT = 26;
        MAP_NONBLOCK = 65536;
        MAP_POPULATE = 32768;
        MAP_PRIVATE = 2;
        MAP_SHARED = 1;
        MAP_SHARED_VALIDATE = 3;
        MAP_STACK = 131072;
        MAP_SYNC = 524288;
        MAP_TYPE = 15;
        MAP_UNINITIALIZED = 67108864;
        PROT_EXEC = 4;
        PROT_GROWSDOWN = 16777216;
        PROT_GROWSUP = 33554432;
        PROT_NONE = 0;
        PROT_READ = 1;
        PROT_SEM = 8;
        PROT_WRITE = 2;

        if (osArch.equals("aarch64")) {
            MAP_LOCKED = 8192;
            MAP_NORESERVE = 16384;
        } else if (osArch.equals("amd64")) {
            MAP_LOCKED = 8192;
            MAP_NORESERVE = 16384;
        } else if (osArch.equals("loongarch64")) {
            MAP_LOCKED = 8192;
            MAP_NORESERVE = 16384;
        } else if (osArch.equals("ppc64le")) {
            MAP_LOCKED = 128;
            MAP_NORESERVE = 64;
        } else if (osArch.equals("riscv64")) {
            MAP_LOCKED = 8192;
            MAP_NORESERVE = 16384;
        } else if (osArch.equals("s390x")) {
            MAP_LOCKED = 8192;
            MAP_NORESERVE = 16384;
        } else {
            throw new UnsupportedOperationException(osArch);
        }
    }
}
