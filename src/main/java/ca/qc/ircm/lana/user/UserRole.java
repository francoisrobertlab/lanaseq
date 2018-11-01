/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
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

/**
 * User roles.
 */
public enum UserRole {
  ADMIN, BIOLOGIST;

  private static String[] ROLES_AS_STRING;
  static {
    UserRole[] roles = UserRole.values();
    ROLES_AS_STRING = new String[UserRole.values().length];
    for (int i = 0; i < roles.length; i++) {
      ROLES_AS_STRING[i] = roles[i].name();
    }
  }

  /**
   * Returns roles as a string array.
   *
   * @return roles as a string array
   */
  public static String[] roles() {
    return ROLES_AS_STRING.clone();
  }
}
