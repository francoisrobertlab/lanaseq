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

package ca.qc.ircm.lana.test.config;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.lana.security.UserDetailsServiceImpl;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.user.UserRole;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UserDetailsServiceImplTest {
  private UserDetailsServiceImpl userDetailsService;
  @Inject
  private UserRepository userRepository;

  @Before
  public void beforeTest() {
    userDetailsService = new UserDetailsServiceImpl(userRepository);
  }

  @Test
  public void loadUserByUsername() {
    UserDetails userDetails = userDetailsService.loadUserByUsername("francois.robert@ircm.qc.ca");

    assertEquals("francois.robert@ircm.qc.ca", userDetails.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, userDetails.getPassword());
    assertEquals(true, userDetails.isEnabled());
    assertEquals(true, userDetails.isAccountNonExpired());
    assertEquals(true, userDetails.isAccountNonLocked());
    assertEquals(true, userDetails.isCredentialsNonExpired());
    List<? extends GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
    assertEquals(1, authorities.size());
    assertEquals(UserRole.BIOLOGIST.name(), authorities.get(0).getAuthority());
  }

  @Test
  public void loadUserByUsername_Inactive() {
    UserDetails userDetails = userDetailsService.loadUserByUsername("inactive.user@ircm.qc.ca");

    assertEquals("inactive.user@ircm.qc.ca", userDetails.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, userDetails.getPassword());
    assertEquals(false, userDetails.isEnabled());
    assertEquals(true, userDetails.isAccountNonExpired());
    assertEquals(true, userDetails.isAccountNonLocked());
    assertEquals(true, userDetails.isCredentialsNonExpired());
    List<? extends GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
    assertEquals(1, authorities.size());
    assertEquals(UserRole.BIOLOGIST.name(), authorities.get(0).getAuthority());
  }
}
