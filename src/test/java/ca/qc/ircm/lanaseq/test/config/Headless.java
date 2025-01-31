package ca.qc.ircm.lanaseq.test.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use headless mode for test, if {@link #value()} is true.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Headless {

  /**
   * Returns true if headless mode should be used, if available.
   *
   * @return true if headless mode should be used, if available
   */
  boolean value() default true;
}
