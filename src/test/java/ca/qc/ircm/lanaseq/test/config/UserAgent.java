package ca.qc.ircm.lanaseq.test.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use specified user agent.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserAgent {

  String FIREFOX_WINDOWS_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:77.0) Gecko/20100101 Firefox/77.0";
  String FIREFOX_LINUX_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:77.0) Gecko/20100101 Firefox/77.0";
  String FIREFOX_MACOSX_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:77.0) Gecko/20100101 Firefox/77.0";

  /**
   * User agent of browser.
   */
  String value() default "";
}
