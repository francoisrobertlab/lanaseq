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

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger logger = LoggerFactory.getLogger(SpringDataUserDetailsService.class);
  @Inject
  private UserRepository userRepository;
  @Inject
  private SecurityConfiguration securityConfiguration;

  protected SpringDataUserDetailsService() {
  }

  protected SpringDataUserDetailsService(UserRepository userRepository,
      SecurityConfiguration securityConfiguration) {
    this.userRepository = userRepository;
    this.securityConfiguration = securityConfiguration;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email).orElse(null);
    logger.debug("user {} signin-in, locked: {}, database user: {}", email,
        user != null ? accountLocked(user) : "false", user);
    if (null == user) {
      throw new UsernameNotFoundException("No user with email: " + email);
    } else {
      Collection<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
      if (user.isExpiredPassword()) {
        authorities
            .add(new SimpleGrantedAuthority(SecurityConfiguration.FORCE_CHANGE_PASSWORD_ROLE));
      }
      return new org.springframework.security.core.userdetails.User(user.getEmail(),
          user.getHashedPassword(), user.isActive(), true, true, !accountLocked(user), authorities);
    }
  }

  private boolean accountLocked(User user) {
    return user.getSignAttempts() % securityConfiguration.getLockAttemps() == 0
        && user.getLastSignAttempt() != null && user.getLastSignAttempt()
            .plusMillis(securityConfiguration.getLockDuration().toMillis()).isAfter(Instant.now());
  }
}