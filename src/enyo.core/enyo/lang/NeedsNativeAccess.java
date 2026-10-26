package enyo.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a module as requiring native access.
 *
 * This means that when a virtual machine is launched that uses this module,
 * the module should be named in an
 * {@code --enable-native-access=enyo.sqlite=} option.
 * @see Modules#needsNativeAccess(ModuleReference)
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.MODULE)
public @interface NeedsNativeAccess {
}
