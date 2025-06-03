package ca.qc.ircm.lanaseq;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.context.MessageSource;

/**
 * Constants.
 */
public class Constants {

  public static final Locale ENGLISH = Locale.CANADA;
  public static final Locale FRENCH = Locale.CANADA_FRENCH;
  public static final Locale DEFAULT_LOCALE = ENGLISH;
  public static final String APPLICATION_NAME = "application.name";
  public static final String TITLE = "title";
  public static final String ERROR_TEXT = "error-text";
  public static final String BORDER = "border";
  public static final String PLACEHOLDER = "placeholder";
  public static final String HELPER = "helper";
  public static final String FIELD_NOTIFICATION = "field.notification";
  public static final String REQUIRED = "required";
  public static final String INVALID = "invalid";
  public static final String INVALID_NUMBER = "invalidNumber";
  public static final String INVALID_INTEGER = "invalidInteger";
  public static final String INVALID_EMAIL = "invalidEmail";
  public static final String ALREADY_EXISTS = "alreadyExists";
  public static final String OUT_OF_RANGE = "outOfRange";
  public static final String SAVE = "save";
  public static final String CANCEL = "cancel";
  public static final String DELETE = "delete";
  public static final String ALL = "all";
  public static final String ADD = "add";
  public static final String EDIT = "edit";
  public static final String REMOVE = "remove";
  public static final String UPLOAD = "upload";
  public static final String DOWNLOAD = "download";
  public static final String CONFIRM = "confirm";
  public static final String REFRESH = "refresh";

  /**
   * Strip this key from class name, if it matches.
   */
  private static final String STRIP_KEY = Pattern.quote(
      Constants.class.getPackage().getName() + ".");

  /**
   * Returns all valid locales for program.
   *
   * @return all valid locales for program
   */
  public static List<Locale> getLocales() {
    List<Locale> locales = new ArrayList<>();
    locales.add(ENGLISH);
    locales.add(FRENCH);
    return locales;
  }

  /**
   * Key prefix to use to get messages from {@link MessageSource}.
   * <br>
   * Here is an example on how to use prefix.
   *
   * <pre>
   * private static final String MESSAGE_PREFIX = messagePrefix(Constants.class);
   * messageSource.getMessage(MESSAGE_PREFIX + "title", new Object[] {}, locale);
   * </pre>
   * </p>
   *
   * @param baseClass class to use to obtain prefix
   * @return key prefix to use to get messages from {@link MessageSource}
   */
  public static String messagePrefix(Class<?> baseClass) {
    return baseClass.getName().replaceFirst(STRIP_KEY, "") + ".";
  }
}
