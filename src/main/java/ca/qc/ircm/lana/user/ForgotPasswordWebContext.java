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

package ca.qc.ircm.lana.user;

import java.util.Locale;

/**
 * Web context for forgot password.
 */
public interface ForgotPasswordWebContext {
  /**
   * Returns URL that leads to change forgotten password function. This URL must begin with with a
   * <code>/</code> and must begin with the context path, if applicable.
   *
   * @param forgotPassword
   *          forgot password request created for user
   * @param locale
   *          adapt URL to specified locale
   * @return URL that leads to change forgotten password function
   */
  public String getChangeForgottenPasswordUrl(ForgotPassword forgotPassword, Locale locale);
}
