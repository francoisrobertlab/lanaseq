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

package ca.qc.ircm.lana.security;

import ca.qc.ircm.lana.user.User;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Service for authentifications.
 */
public interface AuthenticationService {
  /**
   * Sign user.
   *
   * @param email
   *          user's email
   * @param password
   *          user's password
   * @param rememberMe
   *          true if user is to be remembered between sessions
   * @throws AuthenticationException
   *           user cannot be authenticated
   */
  public void sign(String email, String password, boolean rememberMe)
      throws AuthenticationException;

  /**
   * Signout user.
   */
  public void signout();

  /**
   * Run application as another user.
   *
   * @param user
   *          other user
   */
  public void runAs(User user);

  /**
   * Stop running application as another user.
   *
   * @return id of assumed identity, if any
   */
  public Long stopRunAs();

  /**
   * Selects authentication information based on user's name and password.
   *
   * @param token
   *          authentication token
   * @return authentication information
   * @throws UnknownAccountException
   *           user is unknown
   * @throws InvalidAccountException
   *           user has not been validated yet
   * @throws DisabledAccountException
   *           user is disabled
   * @throws IncorrectCredentialsException
   *           user's password is invalid
   */
  public AuthenticationInfo getAuthenticationInfo(UsernamePasswordToken token);

  /**
   * Returns user's authorization information.
   *
   * @param principals
   *          user's principals
   * @return user's authorization information
   */
  public AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals);

  /**
   * Hashes password. This method should be used before inserting password into database.
   *
   * @param password
   *          password as entered by user
   * @return hashed password
   */
  public String encode(String password);
}
