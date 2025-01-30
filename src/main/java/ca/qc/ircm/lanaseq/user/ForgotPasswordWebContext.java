package ca.qc.ircm.lanaseq.user;

import java.util.Locale;

/**
 * Web context for forgot password.
 */
public interface ForgotPasswordWebContext {

  /**
   * Returns URL that leads to change forgotten password function. This URL must begin with with a
   * <code>/</code> and must begin with the context path, if applicable.
   *
   * @param forgotPassword forgot password request created for user
   * @param locale         adapt URL to specified locale
   * @return URL that leads to change forgotten password function
   */
  String getChangeForgottenPasswordUrl(ForgotPassword forgotPassword, Locale locale);
}
