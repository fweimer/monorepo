package enyo.build;

import java.util.Objects;

/**
 * Module, class name, method name, and signature of a test method.
 */
final class TestMethodLocator implements Comparable<TestMethodLocator> {
    TestMethodLocator(String module, String className,
        String methodName, boolean hasTestingArgument) {
        this.module = Objects.requireNonNull(module, "module");
        this.className = Objects.requireNonNull(className, "className");
        this.methodName = Objects.requireNonNull(methodName, "methodName");
        this.hasTestingArgument = hasTestingArgument;

        if (className.indexOf('/') >= 0)
            throw new IllegalArgumentException("className: " + className);
    }

    /**
     * Returns the name of the module which contains this test method.
     * @return a Java module name
     * @see Module#name()
     */
    String module() {
        return module;
    }

    /**
     * Returns the name of the class which contains this test method.
     * @return a source-level Java class name
     * @see Class#getName()
     */
    String className() {
        return className;
    }

    /**
     * Returns the name of this test method.
     * @return a Java method name
     */
    String methodName() {
        return methodName;
    }

    /**
     * Indicates whether this test method has a testing argument.
     * @return true if this test method expects a {@link enyo.testing.Testing}
     *   argument
     */
    boolean hasTestingArgument() {
        return hasTestingArgument;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass())
            return false;
        TestMethodLocator obj = (TestMethodLocator) o;
        return module.equals(obj.module)
            && className.equals(obj.className)
            && methodName.equals(obj.methodName)
            && hasTestingArgument == obj.hasTestingArgument;
    }

    @Override
    public int hashCode() {
        int hash = module.hashCode();
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
        return module + '/' + className + "::" + methodName + signature;
    }

    @Override
    public int compareTo(TestMethodLocator other) {
        int ret = module.compareTo(other.module);
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

    private final String module;
    private final String className;
    private final String methodName;
    private final boolean hasTestingArgument;
}
