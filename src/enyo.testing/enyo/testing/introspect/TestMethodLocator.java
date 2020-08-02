package enyo.testing.introspect;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Module, class name, method name, and signature of a test method.
 *
 * Objects of this class uniquely identifies a test method.
 */
public final class TestMethodLocator
    implements Comparable<TestMethodLocator>, Serializable {
    /**
     * Creates a new test method locator.
     * @param moduleName the module name if present, otherwise this
     *   test resides in the unnamed module
     * @param className the class name containing the test method
     * @param methodName the name of the test mode
     * @param hasTestingArgument if true, the test method expects an
     *   argument of type {@link enyo.testing.Testing}
     */
    public TestMethodLocator(Optional<String> moduleName, String className,
        String methodName, boolean hasTestingArgument) {
        this.moduleName = Objects.requireNonNull(moduleName, "moduleName");
        this.className = Objects.requireNonNull(className, "className");
        this.methodName = Objects.requireNonNull(methodName, "methodName");
        this.hasTestingArgument = hasTestingArgument;

        if (moduleName.map(String::isEmpty).orElse(false))
            throw new IllegalArgumentException("Empty module name");
        if (className.isEmpty())
            throw new IllegalArgumentException("Empty class name");
        if (methodName.isEmpty())
            throw new IllegalArgumentException("Empty method name");
        if (className.indexOf('/') >= 0)
            throw new IllegalArgumentException("className: " + className);
    }

    /**
     * Returns the name of the module which contains this test method.
     * @return a Java module name, if present
     * @see Module#getName()
     */
    public Optional<String> moduleName() {
        return moduleName;
    }

    /**
     * Returns the name of the class which contains this test method.
     * @return a source-level Java class name
     * @see Class#getName()
     */
    public String className() {
        return className;
    }

    /**
     * Returns the name of this test method.
     * @return a Java method name
     */
    public String methodName() {
        return methodName;
    }

    /**
     * Indicates whether this test method has a testing argument.
     * @return true if this test method expects a {@link enyo.testing.Testing}
     *   argument
     */
    public boolean hasTestingArgument() {
        return hasTestingArgument;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass())
            return false;
        TestMethodLocator obj = (TestMethodLocator) o;
        return moduleName.equals(obj.moduleName)
            && className.equals(obj.className)
            && methodName.equals(obj.methodName)
            && hasTestingArgument == obj.hasTestingArgument;
    }

    @Override
    public int hashCode() {
        int hash = moduleName.hashCode();
        hash = hash * 17 + className.hashCode();
        hash = hash * 1009 + methodName.hashCode();
        hash ^= Boolean.hashCode(hasTestingArgument);
        return hash;
    }

    @Override
    public String toString() {
        String signature;
        if (hasTestingArgument)
            signature = "(enyo.testing.Testing)";
        else
            signature = "()";
        String modulePrefix = moduleName
            .map(name -> name + '/')
            .orElse("");
        return modulePrefix + className + "::" + methodName + signature;
    }

    @Override
    public int compareTo(TestMethodLocator other) {
        int ret;
        // Missing module sorts first.
        if (moduleName.isPresent()) {
            if (other.moduleName.isPresent())
                ret = moduleName.get().compareTo(other.moduleName.get());
            else
                ret = 1;
        } else {
            if (other.moduleName.isPresent())
                ret = -1;
            else
                ret = 0;
        }
        if (ret != 0)
            return ret;
        ret = className.compareTo(other.className);
        if (ret != 0)
            return ret;
        ret = methodName.compareTo(other.methodName);
        if (ret != 0)
            return ret;
        return Boolean.compare(hasTestingArgument, other.hasTestingArgument);
    }

    /**
     * The module name.
     * @see #moduleName()
     */
    private final Optional<String> moduleName;

    /**
     * The class name.
     * @see #className()
     */
    private final String className;

    /**
     * The method name.
     * @see #methodName()
     */
    private final String methodName;

    /**
     * Whether this test has a {@link enyo.testing.Testing} argument.
     * @see #hasTestingArgument()
     */
    private final boolean hasTestingArgument;

    private static final long serialVersionUID = 0x3602a424f417b9f0L;
}
