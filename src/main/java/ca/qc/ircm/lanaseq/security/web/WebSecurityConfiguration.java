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

package ca.qc.ircm.lanaseq.security.web;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;

import ca.qc.ircm.lanaseq.security.DaoAuthenticationProviderWithLdap;
import ca.qc.ircm.lanaseq.security.LdapConfiguration;
import ca.qc.ircm.lanaseq.security.LdapService;
import ca.qc.ircm.lanaseq.security.SecurityConfiguration;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordView;
import ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import ca.qc.ircm.lanaseq.web.MainView;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.server.HandlerHelper.RequestType;
import com.vaadin.flow.shared.ApplicationConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

/**
 * Security configuration.
 */
@EnableWebSecurity
@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
  public static final String SIGNIN_PROCESSING_URL = "/" + SigninView.VIEW_NAME;
  public static final String SIGNOUT_URL = "/signout";
  public static final String SWITCH_USER_URL = "/switchUser";
  public static final String SWITCH_USERNAME_PARAMETER = "username";
  public static final String SWITCH_USER_EXIT_URL = "/switchUser/exit";
  private static final String SIGNIN_FAILURE_URL_PATTERN =
      Pattern.quote(SIGNIN_PROCESSING_URL) + "\\?.*";
  private static final String SIGNIN_DEFAULT_FAILURE_URL =
      SIGNIN_PROCESSING_URL + "?" + SigninView.FAIL;
  private static final String SIGNIN_LOCKED_URL = SIGNIN_PROCESSING_URL + "?" + SigninView.LOCKED;
  private static final String SIGNIN_DISABLED_URL =
      SIGNIN_PROCESSING_URL + "?" + SigninView.DISABLED;
  private static final String SIGNIN_URL = SIGNIN_PROCESSING_URL;
  private static final String SIGNOUT_SUCCESS_URL = "/" + MainView.VIEW_NAME;
  private static final String SWITCH_USER_FAILURE_URL =
      "/" + UsersView.VIEW_NAME + "?" + UsersView.SWITCH_FAILED;
  private static final String SWITCH_USER_TRAGET_URL = "/" + MainView.VIEW_NAME;
  private static final String PASSWORD_ENCRYPTION = "bcrypt";
  @Autowired
  private UserDetailsService userDetailsService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private LdapService ldapService;
  @Autowired
  private SecurityConfiguration configuration;
  @Autowired
  private LdapConfiguration ldapConfiguration;

  /**
   * Returns password encoder that supports password upgrades.
   *
   * @return password encoder that supports password upgrades
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    PasswordEncoder defaultPasswordEncoder = new BCryptPasswordEncoder();
    encoders.put(PASSWORD_ENCRYPTION, defaultPasswordEncoder);

    DelegatingPasswordEncoder passworEncoder =
        new DelegatingPasswordEncoder(PASSWORD_ENCRYPTION, encoders);
    passworEncoder.setDefaultPasswordEncoderForMatches(defaultPasswordEncoder);

    return passworEncoder;
  }

  /**
   * Returns {@link DaoAuthenticationProviderWithLdap}.
   *
   * @return {@link DaoAuthenticationProviderWithLdap}
   */
  @Bean
  public DaoAuthenticationProviderWithLdap authenticationProvider() {
    DaoAuthenticationProviderWithLdap authenticationProvider =
        new DaoAuthenticationProviderWithLdap();
    authenticationProvider.setUserDetailsService(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    authenticationProvider.setUserRepository(userRepository);
    authenticationProvider.setLdapService(ldapService);
    authenticationProvider.setLdapConfiguration(ldapConfiguration);
    authenticationProvider.setSecurityConfiguration(configuration);
    return authenticationProvider;
  }

  /**
   * Returns {@link AuthenticationFailureHandler}.
   *
   * @return {@link AuthenticationFailureHandler}
   */
  @Bean
  public AuthenticationFailureHandler authenticationFailureHandler() {
    final Map<String, String> failureUrlMap = new HashMap<>();
    failureUrlMap.put(LockedException.class.getName(), SIGNIN_LOCKED_URL);
    failureUrlMap.put(DisabledException.class.getName(), SIGNIN_DISABLED_URL);
    ExceptionMappingAuthenticationFailureHandler authenticationFailureHandler =
        new ExceptionMappingAuthenticationFailureHandler();
    authenticationFailureHandler.setDefaultFailureUrl(SIGNIN_DEFAULT_FAILURE_URL);
    authenticationFailureHandler.setExceptionMappings(failureUrlMap);
    return authenticationFailureHandler;
  }

  /**
   * Returns {@link SwitchUserFilter}.
   *
   * @return {@link SwitchUserFilter}
   */
  @Bean
  public SwitchUserFilter switchUserFilter() {
    SwitchUserFilter filter = new SwitchUserFilter();
    filter.setUserDetailsService(userDetailsService());
    filter.setSwitchUserUrl(SWITCH_USER_URL);
    filter.setSwitchFailureUrl(SWITCH_USER_FAILURE_URL);
    filter.setTargetUrl(SWITCH_USER_TRAGET_URL);
    filter.setExitUserUrl(SWITCH_USER_EXIT_URL);
    filter.setUsernameParameter(SWITCH_USERNAME_PARAMETER);
    return filter;
  }

  /**
   * Registers our UserDetailsService and the password encoder to be used on login attempts.
   */
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    super.configure(auth);
    auth.authenticationProvider(authenticationProvider()).userDetailsService(userDetailsService);
  }

  /**
   * Require login to access internal pages and configure login form.
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Not using Spring CSRF here to be able to use plain HTML for the login page
    http.csrf().disable()

        // Register our CustomRequestCache, that saves unauthorized access attempts, so
        // the user is redirected after login.
        .requestCache().requestCache(new SkipVaadinRequestCache())

        // Restrict access to our application.
        .and().authorizeRequests()

        // Allow all flow internal requests.
        .requestMatchers(WebSecurityConfiguration::isVaadinInternalRequest).permitAll()
            .regexMatchers("/offline-stub.html", "/sw-runtime-resources-precache.js").permitAll()

        // Allow all login failure URLs.
        .regexMatchers(SIGNIN_FAILURE_URL_PATTERN).permitAll()

        // Allow test URLs.
        .regexMatchers("/testvaadinservice").permitAll()

        // Only admins can switch users.
        .antMatchers(SWITCH_USER_URL).hasAuthority(ADMIN).antMatchers(SWITCH_USER_EXIT_URL)
        .authenticated()

        // Allow anonymous views.
        .antMatchers("/" + ForgotPasswordView.VIEW_NAME,
            "/" + UseForgotPasswordView.VIEW_NAME + "/**")
        .permitAll()

        // Allow all requests by logged in users.
        .anyRequest().authenticated()

        // Configure the login page.
        .and().formLogin().loginPage(SIGNIN_URL).permitAll()
        .loginProcessingUrl(SIGNIN_PROCESSING_URL).failureHandler(authenticationFailureHandler())

        // Register the success handler that redirects users to the page they last tried
        // to access
        .successHandler(new SavedRequestAwareAuthenticationSuccessHandler())

        // Configure logout
        .and().logout().logoutUrl(SIGNOUT_URL).logoutSuccessUrl(SIGNOUT_SUCCESS_URL)

        // Remember me
        .and().rememberMe().alwaysRemember(true).key(configuration.getRememberMeKey());

    // Used for TestBench.
    try {
      Class<?> clazz = Class.forName("ca.qc.ircm.lanaseq.test.config.TestBenchSecurityFilter");
      http.addFilterBefore((Filter) clazz.getDeclaredConstructor().newInstance(),
          SecurityContextPersistenceFilter.class);
    } catch (ClassNotFoundException e) {
      // Ignore, not running unit tests.
    }
  }

  /**
   * Allows access to static resources, bypassing Spring security.
   */
  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers(
        // Vaadin Flow static resources
        "/VAADIN/**",

        // the standard favicon URI
        "/favicon.ico",

        // web application manifest
        "/manifest.json", "/sw.js", "/offline-page.html",

        // icons and images
        "/icons/**", "/images/**",

        // (development mode) static resources
        "/frontend/**",

        // (development mode) webjars
        "/webjars/**",

        // (development mode) H2 debugging console
        "/h2-console/**",

        // (production mode) static resources
        "/frontend-es5/**", "/frontend-es6/**");
  }

  static boolean isVaadinInternalRequest(HttpServletRequest request) {
    final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
    return parameterValue != null
        && Stream.of(RequestType.values()).anyMatch(r -> r.getIdentifier().equals(parameterValue));
  }
}
