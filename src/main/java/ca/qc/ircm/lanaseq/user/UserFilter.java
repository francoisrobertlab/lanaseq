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

package ca.qc.ircm.lanaseq.user;

import static ca.qc.ircm.lanaseq.text.Strings.comparable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Filters users.
 */
public class UserFilter implements Predicate<User> {
  public String emailContains;
  public String nameContains;
  public Boolean active;

  @Override
  public boolean test(User user) {
    boolean test = true;
    if (emailContains != null) {
      test &= comparable(replaceNull(user.getEmail())).contains(comparable(emailContains));
    }
    if (nameContains != null) {
      test &= comparable(replaceNull(user.getName())).contains(comparable(nameContains));
    }
    if (active != null) {
      test &= active == user.isActive();
    }
    return test;
  }

  private String replaceNull(String value) {
    return Objects.toString(value, "");
  }
}
