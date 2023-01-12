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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.sample.web.SamplesView;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserAuthority;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.PasswordView;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.internal.AfterNavigationHandler;
import com.vaadin.flow.router.internal.BeforeEnterHandler;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link ConfigureUiServiceInitListener}.
 */
@ServiceTestAnnotations
@WithMockUser
public class ConfigureUiServiceInitListenerTest extends AbstractKaribuTestCase {
  private static final Logger logger =
      LoggerFactory.getLogger(ConfigureUiServiceInitListenerTest.class);
  @Autowired
  private ConfigureUiServiceInitListener configurer;
  @MockBean
  private AuthorizationService authorizationService;
  @Mock
  private AfterNavigationListener navigationListener;
  @Mock
  private BeforeEnterEvent beforeEnterEvent;
  @Mock
  private AfterNavigationEvent afterNavigationEvent;
  @Mock
  private Location location;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ConfigureUiServiceInitListener.class, locale);
  private User user = new User(1L, "myuser");

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(authorizationService.getCurrentUser()).thenReturn(Optional.of(user));
    when(authorizationService.isAuthorized(AccessDeniedView.class)).thenReturn(true);
    ui.setLocale(locale);
    ui.addAfterNavigationListener(navigationListener);
    when(beforeEnterEvent.getNavigationTarget()).thenAnswer(i -> SamplesView.class);
    when(beforeEnterEvent.getUI()).thenReturn(ui);
    when(beforeEnterEvent.getLocation()).thenReturn(location);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    when(location.getPath()).thenReturn("");
  }

  private void doBeforeEnter() {
    List<BeforeEnterHandler> handlers = ui.getInternals().getListeners(BeforeEnterHandler.class);
    handlers.get(0).beforeEnter(beforeEnterEvent);
  }

  private void doAfterNavigation() {
    List<AfterNavigationHandler> handlers =
        ui.getInternals().getListeners(AfterNavigationHandler.class);
    handlers.get(0).afterNavigation(afterNavigationEvent);
  }

  @Test
  public void beforeEnter_Authorized() {
    when(authorizationService.isAuthorized(any())).thenReturn(true);

    doBeforeEnter();

    verify(authorizationService, atLeastOnce()).isAuthorized(SamplesView.class);
  }

  @Test
  public void beforeEnter_NotAuthorized() {
    doBeforeEnter();

    verify(authorizationService, atLeastOnce()).isAuthorized(SamplesView.class);
    verify(authorizationService, atLeastOnce()).isAnonymous();
    String message = resources.message(AccessDeniedException.class.getSimpleName(), user.getEmail(),
        SamplesView.class.getSimpleName());
    verify(beforeEnterEvent).rerouteToError(any(AccessDeniedException.class), eq(message));
  }

  @Test
  public void beforeEnter_NotAuthorizedAnonymous() {
    when(authorizationService.isAnonymous()).thenReturn(true);

    doBeforeEnter();

    verify(authorizationService, atLeastOnce()).isAuthorized(SamplesView.class);
    verify(authorizationService, atLeastOnce()).isAnonymous();
  }

  @Test
  public void afterNavigation_ForceChangePassword() {
    when(authorizationService.hasRole(UserAuthority.FORCE_CHANGE_PASSWORD)).thenReturn(true);

    doAfterNavigation();

    verify(authorizationService, atLeastOnce()).hasRole(UserAuthority.FORCE_CHANGE_PASSWORD);
    verify(navigationListener).afterNavigation(any());
    assertCurrentView(PasswordView.class);
  }

  @Test
  public void afterNavigation_ForceChangePasswordAlreadyOnView() {
    when(authorizationService.isAuthorized(any())).thenReturn(true);
    when(authorizationService.hasRole(UserAuthority.FORCE_CHANGE_PASSWORD)).thenReturn(true);
    when(location.getPath()).thenReturn(PasswordView.VIEW_NAME);

    doAfterNavigation();

    verify(authorizationService, atLeastOnce()).hasRole(UserAuthority.FORCE_CHANGE_PASSWORD);
    verify(navigationListener, never()).afterNavigation(any());
  }

  @Test
  public void afterNavigation_NotForceChangePassword() {
    when(authorizationService.isAuthorized(any())).thenReturn(true);

    doAfterNavigation();

    verify(authorizationService, atLeastOnce()).hasRole(UserAuthority.FORCE_CHANGE_PASSWORD);
    verify(navigationListener, never()).afterNavigation(any());
  }
}
