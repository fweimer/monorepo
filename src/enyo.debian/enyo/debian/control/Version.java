package enyo.debian.control;

/**
 * A debian package version.
 *
 * Versions for different strings can compare equal.  Nevertheless,
 * {@link #toString()} returns the original (non-normalized) string.
 */
public final class Version implements Comparable<Version> {
    /**
     * Converts a string to the corresponding Debian package version.
     * @param version the version string
     * @return the string a as a Debian package version
     * @throws IllegalArgumentException if {@code version} is not a valid
     *   Debian package version
     * @throws NullPointerException if {@code version} is null
     */
    public static Version of(String version) {
        if (version.isEmpty())
            throw new IllegalArgumentException("Empty version");

        int firstColon = version.indexOf(':');
        for (int i = 0; i < firstColon; ++i) {
            char ch = version.charAt(i);
            if (ch < '0' || ch > '9')
                throw new IllegalArgumentException(
                    "Invalid character in epoch: " + version);
        }

        // Check both upstream and Debian version in a single pass.
        // The position of the boundary between the two components
        // does not affect the validity of the version string.
        int upstreamVersionStart = firstColon + 1; // 0 if colon not found.
        int upstreamVersionEnd = version.length();
        boolean hasDebianVersion = false;
        for (int i = upstreamVersionStart; i < version.length(); ++i) {
            char ch = version.charAt(i);
            if (ch == '-') {
                upstreamVersionEnd = i;
                hasDebianVersion = true;
            }
            else if (!(isLetterOrDigit(ch)
                    || ch == '+' || ch == '.' || ch == '~' || ch == ':'))
                throw new IllegalArgumentException(
                    "Invalid character in version: " + version);
        }

        // Policy is ambiguous whether this is allowed.
        if (firstColon == 0)
            throw new IllegalArgumentException("Empty epoch: " + version);
        if (upstreamVersionStart >= upstreamVersionEnd)
            throw new IllegalArgumentException(
                "Empty upstream version: " + version);
        if (hasDebianVersion && upstreamVersionEnd == version.length() - 1)
            throw new IllegalArgumentException(
                "Empty Debian version: " + version);

        return new Version(version, upstreamVersionEnd);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != Version.class)
            return false;
        Version other = (Version) o;
        return compareVersions(version, upstreamVersionEnd,
            other.version, other.upstreamVersionEnd) == 0;
    }

    @Override
    public int hashCode() {
        int value = hashCode;
        if (value == 0)
            value = hashCode(version, upstreamVersionEnd);
        return value;
    }

    @Override
    public int compareTo(Version other) {
        return compareVersions(version, upstreamVersionEnd,
            other.version, other.upstreamVersionEnd);
    }

    @Override
    public String toString() {
        return version;
    }

    private final String version;
    private final int upstreamVersionEnd;
    private int hashCode;

    private Version(String version, int upstreamVersionEnd) {
        this.version = version;
        this.upstreamVersionEnd = upstreamVersionEnd;
    }

    static boolean isLetterOrDigit(char ch) {
        return ('A' <= ch && ch <= 'Z')
            || ('a' <= ch && ch <= 'z')
            || ('0' <= ch && ch <= '9');
    }

    private static int compareVersions(String left, int leftUpstreamEnd,
        String right, int rightUpstreamEnd) {
        int leftUpstreamVersion;
        int rightUpstreamVersion;

        // Compare the epoch.
        {
            int leftEpochStart = findEpochStart(left);
            int leftEpochEnd = findEpochEnd(left, leftEpochStart);
            int leftEpochLength = epochLength(leftEpochStart, leftEpochEnd);
            int rightEpochStart = findEpochStart(right);
            int rightEpochEnd = findEpochEnd(right, rightEpochStart);
            int rightEpochLength = epochLength(rightEpochStart, rightEpochEnd);
            if (leftEpochLength != rightEpochLength)
                // Different number of digits.
                return leftEpochLength - rightEpochLength;
            // Same number of digits.  Check if the value is different.
            for (int i = 0; i < leftEpochLength; ++i) {
                char leftCh = left.charAt(leftEpochStart + i);
                char rightCh = right.charAt(rightEpochStart + i);
                if (leftCh != rightCh)
                    return leftCh - rightCh;
            }
            // This turns -1 into 0, as required.
            leftUpstreamVersion = leftEpochEnd + 1;
            rightUpstreamVersion = rightEpochEnd + 1;
        }

        // Compare both components separately.  This matters because
        // trailing zero handling differs at the upstream/Debian
        // version boundary.
        int ret = compareVersionComponent(
            left, leftUpstreamVersion, leftUpstreamEnd,
            right, rightUpstreamVersion, rightUpstreamEnd);
        if (ret != 0)
            return ret;
        return compareVersionComponent(
            left, leftUpstreamEnd + 1, left.length(),
            right, rightUpstreamEnd + 1, right.length());
    }

    /**
     * Skips over leading zeros to find the actual epoch.
     */
    private static int findEpochStart(String version) {
        for (int i = 0; i < version.length(); ++i) {
            char ch = version.charAt(i);
            if (('1' <= ch && ch <= '9') || ch == ':')
                return i;
            else if (ch != '0')
                // Non-digit before colon.  No epoch.
                return -1;
        }
        return -1;
    }

    private static int findEpochEnd(String version, int start) {
        if (start >= 0)
            for (int i = start; i < version.length(); ++i) {
                char ch = version.charAt(i);
                if (isDigit(ch))
                    continue;

                // Stop searching at the first : or non-digit.
                if (ch == ':')
                    return i;
                else
                    return -1;
            }
        return -1;
    }

    private static int epochLength(int start, int end) {
        if (end < 0)
            return 0;
        else
            return end - start;
    }

    private static int compareVersionComponent(
            String left, int leftFromIndex, int leftToIndex,
            String right, int rightFromIndex, int rightToIndex) {
        int state = 0;
        int leftIndex = leftFromIndex;
        int rightIndex = rightFromIndex;

        // Outer loop: alternating between non-numeric and numeric
        // sequences.
        while (true) {
            // Non-numeric loop.
            char lastCh = '\0'; // Used for look-behind at end.
            while (leftIndex < leftToIndex && rightIndex < rightToIndex) {
                char leftCh = left.charAt(leftIndex);
                char rightCh = right.charAt(rightIndex);

                if (isDigit(leftCh)) {
                    if (isDigit(rightCh))
                        // Switch to the second loop.
                        break;
                    else {
                        // Left non-numeric component is shorter.
                        if (rightCh == '~')
                            return 1;
                        else
                            return -1;
                    }
                } else if (isDigit(rightCh)) {
                    // Left non-numeric component is longer.
                    if (leftCh == '~')
                        return -1;
                    else
                        return 1;
                } else if (leftCh != rightCh)
                    // Difference in the lexicographic ordering.
                    return weight(leftCh) - weight(rightCh);

                lastCh = leftCh;
                ++leftIndex;
                ++rightIndex;
            }
            if (leftIndex >= leftToIndex) {
                if (rightIndex >= rightToIndex)
                    // Non-numeric component of equal length.
                    return 0;
                else {
                    // Right component is longer.
                    return trailingCheck(right, rightIndex, rightToIndex);
                }
            } else if (rightIndex >= rightToIndex)
                // Left component is longer.
                return -trailingCheck(left, leftIndex, leftToIndex);
            // Both sides have more numeric characters.

            // Numeric component.
            leftIndex = skipZeroes(left, leftIndex, leftToIndex);
            rightIndex = skipZeroes(right, rightIndex, rightToIndex);
            int leftNumericEnd =
                findNumericEnd(left, leftIndex, leftToIndex);
            int leftNumericLength = leftNumericEnd - leftIndex;
            int rightNumericEnd =
                findNumericEnd(right, rightIndex, rightToIndex);
            int rightNumericLength = rightNumericEnd - rightIndex;
            if (leftNumericLength != rightNumericLength)
                // Different number of digits.
                return leftNumericLength - rightNumericLength;
            // Same number of digits.  Check if the value is different.
            for (int i = 0; i < leftNumericLength; ++i) {
                char leftCh = left.charAt(leftIndex + i);
                char rightCh = right.charAt(rightIndex + i);
                if (leftCh != rightCh)
                    return leftCh - rightCh;
            }

            // The numbers are the same.  Continue processing at the
            // next (non-numeric) component.
            leftIndex = leftNumericEnd;
            rightIndex = rightNumericEnd;
        }
    }

    private static boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    private static int weight(char ch) {
        if (ch == '~')
            // The tilde sorts before anything.
            return -1;
        else if (('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z'))
            // Letters sort before everything.
            return ch;
        else
            // Everything else is shifted after the letters.
            return ch + 127;
    }

    private static int trailingCheck(String version, int index, int toIndex) {
        // This is a bit of a gray area.  Policy suggests that
        // trailing zeroes followed by '~' mask the effect of '~', but
        // APT behaves differently.
        while (index < toIndex) {
            char ch = version.charAt(index);
            ++index;
            if (ch == '~')
                return 1;
            else if (ch != '0')
                {
                return -1;
                }
        }
        return 0;
    }

    private static int skipZeroes(String version, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; ++i)
            if (version.charAt(i) != '0')
                return i;
        return toIndex;
    }

    private static int findNumericEnd(
        String version, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; ++i)
            if (!isDigit(version.charAt(i)))
                return i;
        return toIndex;
    }

    private static int hashCode(String version, int upstreamVersionEnd) {
        // 32-bit FNV-style hash.
        int value = 0x811c9dc5;

        // Process the epoch.
        int firstColon = version.indexOf(':');
        if (firstColon >= 0) {
            // Zeroes do not alter the epoch.
            int i = 0;
            while (version.charAt(i) == '0')
                ++i;
            // If there are no non-zero digits, do not hash anything.
            if (i < firstColon)
                // Hash the ':' as well, so that the boundary matters.
                while (i <= firstColon)
                    value = (value * version.charAt(i++)) ^ 0x1000193;
        }

        // Hash the upstream version.
        int upstreamVersionStart = firstColon + 1; // 0 if colon not found.
        value = computeVersionHash(value, version,
            upstreamVersionStart, upstreamVersionEnd);

        // Separator.
        value = (value * '-') ^ 0x1000193;

        // Hash the Debian version.
        value = computeVersionHash(value, version,
            upstreamVersionEnd + 1, version.length());

        return value;
    }

    private static int computeVersionHash(int value, String version,
        int fromIndex, int toIndex) {
        // Leading zeros in numeric components are not significant.
        // Trailing zeroes after non-zeroes are not significant,
        // either.
        //
        // state 0: ignore zeroes, before numeric component.
        // state 1: in numeric component.
        // state 2: in non-numeric component.
        int state = 0;
        for (int i = fromIndex; i < toIndex; ++i) {
            char ch = version.charAt(i);
            if (ch == '0') {
                if (state == 0)
                    // Ignore the zero digit.
                    continue;
                else if (state == 2) {
                    state = 0;
                    continue;
                }
            } else if ('1' <= ch && ch <= '9') {
                if (state != 1) {
                    // Pretend that we saw a zero digit.
                    value = (value * '0') ^ 0x1000193;
                    state = 1;
                }
            } else {
                if (state == 0)
                    // Pretend that we saw a zero digit.
                    value = (value * '0') ^ 0x1000193;
                state = 2;
            }
            value = (value * ch) ^ 0x1000193;
        }

        return value;
    }
}
