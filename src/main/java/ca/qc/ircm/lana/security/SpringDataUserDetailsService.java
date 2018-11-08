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
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final int LOCK_ATTEMPS = 5;
  private static final Duration LOCK_DURATION = Duration.ofMinutes(2);
  @Inject
  private UserRepository userRepository;

  protected SpringDataUserDetailsService() {
  }

  protected SpringDataUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email).orElse(null);
    logger.debug("user {} signin-in, locked: {}, database user: {}", email,
        user != null ? accountLocked(user) : "false", user);
    if (null == user) {
      throw new UsernameNotFoundException("No user with email: " + email);
    } else {
      return new org.springframework.security.core.userdetails.User(user.getEmail(),
          user.getHashedPassword(), user.isActive(), true, !user.isExpiredPassword(),
          !accountLocked(user),
          Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())));
    }
  }

  private boolean accountLocked(User user) {
    return user.getSignAttempts() % LOCK_ATTEMPS == 0 && user.getLastSignAttempt() != null
        && user.getLastSignAttempt().plusMillis(LOCK_DURATION.toMillis()).isAfter(Instant.now());
  }
}