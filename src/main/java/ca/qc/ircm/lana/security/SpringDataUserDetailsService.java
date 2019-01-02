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

package ca.qc.ircm.lana.security;

import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.FORCE_CHANGE_PASSWORD;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;
import static ca.qc.ircm.lana.user.UserRole.USER;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import javax.inject.Inject;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UserDetailsService}.
 */
@Service
public class SpringDataUserDetailsService implements UserDetailsService {
  @Inject
  private UserRepository userRepository;

  protected SpringDataUserDetailsService() {
  }

  protected SpringDataUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username).orElse(null);
    if (null == user) {
      throw new UsernameNotFoundException("No user with username: " + username);
    } else {
      Collection<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority(USER));
      if (user.isAdmin()) {
        authorities.add(new SimpleGrantedAuthority(ADMIN));
      }
      if (user.getLaboratory() != null && user.getLaboratory().getManagers().contains(user)) {
        authorities.add(new SimpleGrantedAuthority(MANAGER));
      }
      if (user.isExpiredPassword()) {
        authorities.add(new SimpleGrantedAuthority(FORCE_CHANGE_PASSWORD));
      }
      return new AuthenticatedUser(user, authorities);
    }
  }
}