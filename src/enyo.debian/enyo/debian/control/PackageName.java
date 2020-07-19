package enyo.debian.control;

import java.util.Objects;

/**
 * Name of a source or binary package.
 *
 * <p>Package names are immutable.  The comparison order is
 * lexicographic, based on the name.  Upper-case letters and non-ASCII
 * characters are not permitted in package names, so it does not
 * matter whether the comparison is case-sensitive or not.
 */
public final class PackageName implements Comparable<PackageName> {
    /**
     * Returns a package name for the specified string.
     *
     * @param name the package name
     * @return a Debian package name for the specified string
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException if {@code name} is empty or
     *   contains prohibited characters
     */
    public static PackageName of(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Empty package name");
        if (name.length() < 2)
            throw new IllegalArgumentException(
                "Package name too short: " + name);
        boolean valid = isLetterOrDigit(name.charAt(0));
        for (int i = 0; i < name.length(); ++i) {
            char ch = name.charAt(i);
            if (!(isLetterOrDigit(ch)
                    || ch == '+' || ch == '-' || ch == '.')) {
                valid = false;
                break;
            }
        }
        if (!valid)
            throw new IllegalArgumentException(
                "Illegal character in package name: " + name);
        return new PackageName(name);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == PackageName.class
            && name.equals(((PackageName) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns the string with the name.
     *
     * @return a non-empty string containing the name.
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(PackageName other) {
        return name.compareTo(other.name);
    }

    private final String name;

    private PackageName(String name) {
        this.name = name;
    }

    static boolean isLetterOrDigit(char ch) {
        return ('a' <= ch && ch <= 'z') || ('0' <= ch && ch <= '9');
    }
}
