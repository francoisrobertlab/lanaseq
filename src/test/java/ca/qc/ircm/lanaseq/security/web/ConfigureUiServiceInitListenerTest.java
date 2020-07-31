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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserAuthority;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.PasswordView;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.shared.Registration;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class ConfigureUiServiceInitListenerTest extends AbstractViewTestCase {
  @Autowired
  private ConfigureUiServiceInitListener configurer;
  @MockBean
  private AuthorizationService authorizationService;
  @Mock
  private ServiceInitEvent serviceInitEvent;
  @Mock
  private VaadinService vaadinService;
  @Mock
  private UIInitEvent uiInitEvent;
  @Mock
  private Registration registration;
  @Mock
  private BeforeEnterEvent beforeEnterEvent;
  @Mock
  private AfterNavigationEvent afterNavigationEvent;
  @Mock
  private Location location;
  @Captor
  private ArgumentCaptor<BeforeEnterListener> beforeEnterListenerCaptor;
  @Captor
  private ArgumentCaptor<AfterNavigationListener> afterNavigationListenerCaptor;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ConfigureUiServiceInitListener.class, locale);
  private User user = new User(1L, "myuser");

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(authorizationService.getCurrentUser()).thenReturn(user);
    when(serviceInitEvent.getSource()).thenReturn(vaadinService);
    when(vaadinService.addUIInitListener(any())).then(i -> {
      UIInitListener listener = i.getArgument(0);
      listener.uiInit(uiInitEvent);
      return registration;
    });
    when(uiInitEvent.getUI()).thenReturn(ui);
    when(ui.getLocale()).thenReturn(locale);
    when(beforeEnterEvent.getNavigationTarget()).thenAnswer(i -> ViewTest.class);
    when(beforeEnterEvent.getUI()).thenReturn(ui);
    when(beforeEnterEvent.getLocation()).thenReturn(location);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    when(location.getPath()).thenReturn("");
  }

  private void doBeforeEnter() {
    configurer.serviceInit(serviceInitEvent);
    verify(vaadinService).addUIInitListener(any());
    verify(ui).addBeforeEnterListener(beforeEnterListenerCaptor.capture());
    BeforeEnterListener beforeEnterListener = beforeEnterListenerCaptor.getValue();
    beforeEnterListener.beforeEnter(beforeEnterEvent);
  }

  private void doAfterNavigation() {
    configurer.serviceInit(serviceInitEvent);
    verify(vaadinService).addUIInitListener(any());
    verify(ui).addAfterNavigationListener(afterNavigationListenerCaptor.capture());
    AfterNavigationListener afterNavigationListener = afterNavigationListenerCaptor.getValue();
    afterNavigationListener.afterNavigation(afterNavigationEvent);
  }

  @Test
  public void beforeEnter_Authorized() {
    when(authorizationService.isAuthorized(any())).thenReturn(true);

    doBeforeEnter();

    verify(authorizationService).isAuthorized(ViewTest.class);
  }

  @Test
  public void beforeEnter_NotAuthorized() {
    doBeforeEnter();

    verify(authorizationService).isAuthorized(ViewTest.class);
    verify(authorizationService).isAnonymous();
    String message = resources.message(AccessDeniedException.class.getSimpleName(), user.getEmail(),
        ViewTest.class.getSimpleName());
    verify(beforeEnterEvent).rerouteToError(any(AccessDeniedException.class), eq(message));
  }

  @Test
  public void beforeEnter_NotAuthorizedAnonymous() {
    when(authorizationService.isAnonymous()).thenReturn(true);

    doBeforeEnter();

    verify(ui).navigate(SigninView.class);
    verify(authorizationService).isAuthorized(ViewTest.class);
    verify(authorizationService).isAnonymous();
  }

  @Test
  public void afterNavigation_ForceChangePassword() {
    when(authorizationService.hasRole(UserAuthority.FORCE_CHANGE_PASSWORD)).thenReturn(true);

    doAfterNavigation();

    verify(authorizationService).hasRole(UserAuthority.FORCE_CHANGE_PASSWORD);
    verify(ui).navigate(PasswordView.class);
  }

  @Test
  public void afterNavigation_ForceChangePasswordAlreadyOnView() {
    when(authorizationService.hasRole(UserAuthority.FORCE_CHANGE_PASSWORD)).thenReturn(true);
    when(location.getPath()).thenReturn(PasswordView.VIEW_NAME);

    doAfterNavigation();

    verify(authorizationService).hasRole(UserAuthority.FORCE_CHANGE_PASSWORD);
    verify(ui, never()).navigate(PasswordView.class);
  }

  @Test
  public void afterNavigation_NotForceChangePassword() {
    doAfterNavigation();

    verify(authorizationService).hasRole(UserAuthority.FORCE_CHANGE_PASSWORD);
    verify(ui, never()).navigate(PasswordView.class);
  }

  public static class ViewTest {
  }
}
