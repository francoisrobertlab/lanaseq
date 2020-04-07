/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lana;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * {@link ResourceBundle} that formats messages using {@link MessageFormat}.
 */
public class AppResources {
  /**
   * Resource bundle name.
   */
  private static final String BUNDLE = AppResources.class.getSimpleName();
  /**
   * Strip this key from class name, if it matches.
   */
  private static final String STRIP_KEY =
      Pattern.quote(AppResources.class.getPackage().getName() + ".");
  /**
   * Resource bundle.
   */
  private final ResourceBundle resources;
  /**
   * Keys prefix.
   */
  private final String prefix;

  public AppResources(String baseName, Locale locale) {
    prefix = baseName.replaceFirst(STRIP_KEY, "");
    resources = ResourceBundle.getBundle(BUNDLE, locale);
  }

  public AppResources(Class<?> baseClass, Locale locale) {
    prefix = baseClass.getName().replaceFirst(STRIP_KEY, "");
    resources = ResourceBundle.getBundle(BUNDLE, locale);
  }

  /**
   * Returns message from resource bundle with replacements.
   *
   * @param key
   *          message's key
   * @param replacements
   *          message's replacements
   * @return message
   */
  public String message(String key, Object... replacements) {
    return MessageFormat.format(resources.getString(prefix + "." + key), replacements);
  }
}
