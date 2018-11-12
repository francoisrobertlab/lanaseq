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

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom authenticating realm for Shiro.
 */
public class ShiroRealm extends org.apache.shiro.realm.AuthorizingRealm {
  @SuppressWarnings("unused")
  private Logger logger = LoggerFactory.getLogger(ShiroRealm.class);
  private final AuthenticationService authenticationService;

  /**
   * Creates Shiro's realm.
   *
   * @param authenticationService
   *          authentication service
   */
  public ShiroRealm(AuthenticationService authenticationService) {
    super(new AllowAllCredentialsMatcher());
    this.authenticationService = authenticationService;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
      throws AuthenticationException {
    UsernamePasswordToken upToken = (UsernamePasswordToken) token;
    return authenticationService.getAuthenticationInfo(upToken);
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    return authenticationService.getAuthorizationInfo(principals);
  }

  public AuthenticationService getAuthenticationService() {
    return authenticationService;
  }
}
