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

package ca.qc.ircm.lanaseq.security;

import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Implements common methods to use for {@link PermissionEvaluator} implementations.
 */
public abstract class AbstractPermissionEvaluator implements PermissionEvaluator {
  @Autowired
  private UserRepository userRepository;

  protected UserDetails getUserDetails(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      return (UserDetails) authentication.getPrincipal();
    } else {
      return null;
    }
  }

  protected User getUser(Authentication authentication) {
    UserDetails user = getUserDetails(authentication);
    if (user instanceof AuthenticatedUser) {
      Long userId = ((AuthenticatedUser) user).getId();
      if (userId == null) {
        return null;
      }

      return userRepository.findById(userId).orElse(null);
    } else {
      return null;
    }
  }

  protected Permission resolvePermission(Object permission) {
    if (permission instanceof Permission) {
      return (Permission) permission;
    }
    try {
      return Enum.valueOf(Permission.class, Objects.toString(permission).toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Permission " + permission + " does not exists in " + Permission.class.getSimpleName());
    }
  }
}
