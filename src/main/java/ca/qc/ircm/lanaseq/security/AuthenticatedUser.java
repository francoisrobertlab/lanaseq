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

import ca.qc.ircm.lanaseq.Data;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * An authenticated user.
 */
public class AuthenticatedUser extends User implements Data {
  private static final long serialVersionUID = -5167464958438112402L;
  private final Long id;

  /**
   * Construct the <code>UserWithId</code> with the details required by authentication.
   *
   * @param user
   *          user
   * @param accountNonExpired
   *          set to <code>true</code> if the account has not expired
   * @param credentialsNonExpired
   *          set to <code>true</code> if the credentials have not expired
   * @param accountNonLocked
   *          set to <code>true</code> if the account is not locked
   * @param authorities
   *          the authorities that should be granted to the caller if they presented the correct
   *          username and password and the user is enabled. Not null.
   *
   * @throws IllegalArgumentException
   *           if a <code>null</code> value was passed either as a parameter or as an element in the
   *           <code>GrantedAuthority</code> collection
   */
  public AuthenticatedUser(ca.qc.ircm.lanaseq.user.User user, boolean accountNonExpired,
      boolean credentialsNonExpired, boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities) {
    super(user.getEmail(), user.getHashedPassword(), user.isActive(), accountNonExpired,
        credentialsNonExpired, accountNonLocked, authorities);
    id = user.getId();
  }

  /**
   * Construct the <code>UserWithId</code> with the details required by authentication.
   *
   * @param user
   *          user
   * @param authorities
   *          the authorities that should be granted to the caller if they presented the correct
   *          username and password and the user is enabled. Not null.
   *
   * @throws IllegalArgumentException
   *           if a <code>null</code> value was passed either as a parameter or as an element in the
   *           <code>GrantedAuthority</code> collection
   */
  public AuthenticatedUser(ca.qc.ircm.lanaseq.user.User user,
      Collection<? extends GrantedAuthority> authorities) {
    super(user.getEmail(), user.getHashedPassword(), authorities);
    id = user.getId();
  }

  @Override
  public boolean equals(Object rhs) {
    return super.equals(rhs);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public Long getId() {
    return id;
  }
}
