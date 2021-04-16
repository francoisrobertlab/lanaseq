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
import java.util.Optional;
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

  protected Optional<UserDetails> getUserDetails(Authentication authentication) {
    return Optional.ofNullable(authentication)
        .filter(au -> au.getPrincipal() instanceof UserDetails)
        .map(au -> (UserDetails) au.getPrincipal());
  }

  protected Optional<User> getUser(Authentication authentication) {
    return getUserDetails(authentication).filter(ud -> ud instanceof AuthenticatedUser)
        .map(ud -> (AuthenticatedUser) ud).map(au -> au.getId())
        .map(id -> userRepository.findById(id).orElse(null));
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
