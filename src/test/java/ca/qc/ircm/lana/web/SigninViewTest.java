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

package ca.qc.ircm.lana.web;

import static ca.qc.ircm.lana.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lana.Constants.TITLE;
import static ca.qc.ircm.lana.text.Strings.property;
import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.HASHED_PASSWORD;
import static ca.qc.ircm.lana.web.SigninView.ADDITIONAL_INFORMATION;
import static ca.qc.ircm.lana.web.SigninView.DESCRIPTION;
import static ca.qc.ircm.lana.web.SigninView.DISABLED;
import static ca.qc.ircm.lana.web.SigninView.FAIL;
import static ca.qc.ircm.lana.web.SigninView.FORGOT_PASSWORD;
import static ca.qc.ircm.lana.web.SigninView.FORM_TITLE;
import static ca.qc.ircm.lana.web.SigninView.HEADER;
import static ca.qc.ircm.lana.web.SigninView.LOCKED;
import static ca.qc.ircm.lana.web.SigninView.SIGNIN;
import static ca.qc.ircm.lana.web.SigninView.VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.AppResources;
import ca.qc.ircm.lana.Constants;
import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.web.ForgotPasswordView;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class SigninViewTest extends AbstractViewTestCase {
  private SigninView view;
  @MockBean
  private AuthorizationService authorizationService;
  @Mock
  private AfterNavigationEvent afterNavigationEvent;
  @Mock
  private BeforeEnterEvent beforeEnterEvent;
  @Mock
  private Location location;
  @Mock
  private QueryParameters queryParameters;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SigninView.class, locale);
  private AppResources userResources = new AppResources(User.class, locale);
  private AppResources generalResources = new AppResources(Constants.class, locale);
  private Map<String, List<String>> parameters = new HashMap<>();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new SigninView(authorizationService);
    view.init();
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    when(location.getQueryParameters()).thenReturn(queryParameters);
    when(queryParameters.getParameters()).thenReturn(parameters);
  }

  @Test
  public void styles() {
    assertTrue(view.getId().orElse("").equals(VIEW_NAME));
    assertTrue(view.isForgotPasswordButtonVisible());
  }

  @Test
  public void labels() {
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.i18n.getHeader().getTitle());
    assertEquals(resources.message(DESCRIPTION), view.i18n.getHeader().getDescription());
    assertEquals(resources.message(ADDITIONAL_INFORMATION), view.i18n.getAdditionalInformation());
    assertEquals(resources.message(FORM_TITLE), view.i18n.getForm().getTitle());
    assertEquals(userResources.message(EMAIL), view.i18n.getForm().getUsername());
    assertEquals(userResources.message(HASHED_PASSWORD), view.i18n.getForm().getPassword());
    assertEquals(resources.message(SIGNIN), view.i18n.getForm().getSubmit());
    assertEquals(resources.message(FORGOT_PASSWORD), view.i18n.getForm().getForgotPassword());
    assertEquals(resources.message(property(FAIL, TITLE)), view.i18n.getErrorMessage().getTitle());
    assertEquals(resources.message(FAIL), view.i18n.getErrorMessage().getMessage());
  }

  @Test
  public void labels_Fail() {
    parameters.put(FAIL, null);
    view.afterNavigation(afterNavigationEvent);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.i18n.getHeader().getTitle());
    assertEquals(resources.message(DESCRIPTION), view.i18n.getHeader().getDescription());
    assertEquals(resources.message(FORM_TITLE), view.i18n.getForm().getTitle());
    assertEquals(resources.message(ADDITIONAL_INFORMATION), view.i18n.getAdditionalInformation());
    assertEquals(userResources.message(EMAIL), view.i18n.getForm().getUsername());
    assertEquals(userResources.message(HASHED_PASSWORD), view.i18n.getForm().getPassword());
    assertEquals(resources.message(SIGNIN), view.i18n.getForm().getSubmit());
    assertEquals(resources.message(FORGOT_PASSWORD), view.i18n.getForm().getForgotPassword());
    assertEquals(resources.message(property(FAIL, TITLE)), view.i18n.getErrorMessage().getTitle());
    assertEquals(resources.message(FAIL), view.i18n.getErrorMessage().getMessage());
  }

  @Test
  public void labels_Disabled() {
    parameters.put(DISABLED, null);
    view.afterNavigation(afterNavigationEvent);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.i18n.getHeader().getTitle());
    assertEquals(resources.message(DESCRIPTION), view.i18n.getHeader().getDescription());
    assertEquals(resources.message(ADDITIONAL_INFORMATION), view.i18n.getAdditionalInformation());
    assertEquals(resources.message(FORM_TITLE), view.i18n.getForm().getTitle());
    assertEquals(userResources.message(EMAIL), view.i18n.getForm().getUsername());
    assertEquals(userResources.message(HASHED_PASSWORD), view.i18n.getForm().getPassword());
    assertEquals(resources.message(SIGNIN), view.i18n.getForm().getSubmit());
    assertEquals(resources.message(FORGOT_PASSWORD), view.i18n.getForm().getForgotPassword());
    assertEquals(resources.message(property(DISABLED, TITLE)),
        view.i18n.getErrorMessage().getTitle());
    assertEquals(resources.message(DISABLED), view.i18n.getErrorMessage().getMessage());
  }

  @Test
  public void labels_Locked() {
    parameters.put(LOCKED, null);
    view.afterNavigation(afterNavigationEvent);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.i18n.getHeader().getTitle());
    assertEquals(resources.message(DESCRIPTION), view.i18n.getHeader().getDescription());
    assertEquals(resources.message(ADDITIONAL_INFORMATION), view.i18n.getAdditionalInformation());
    assertEquals(resources.message(FORM_TITLE), view.i18n.getForm().getTitle());
    assertEquals(userResources.message(EMAIL), view.i18n.getForm().getUsername());
    assertEquals(userResources.message(HASHED_PASSWORD), view.i18n.getForm().getPassword());
    assertEquals(resources.message(SIGNIN), view.i18n.getForm().getSubmit());
    assertEquals(resources.message(FORGOT_PASSWORD), view.i18n.getForm().getForgotPassword());
    assertEquals(resources.message(property(LOCKED, TITLE)),
        view.i18n.getErrorMessage().getTitle());
    assertEquals(resources.message(LOCKED), view.i18n.getErrorMessage().getMessage());
  }

  @Test
  public void localeChange() {
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(SigninView.class, locale);
    final AppResources userResources = new AppResources(User.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.i18n.getHeader().getTitle());
    assertEquals(resources.message(DESCRIPTION), view.i18n.getHeader().getDescription());
    assertEquals(resources.message(ADDITIONAL_INFORMATION), view.i18n.getAdditionalInformation());
    assertEquals(resources.message(FORM_TITLE), view.i18n.getForm().getTitle());
    assertEquals(userResources.message(EMAIL), view.i18n.getForm().getUsername());
    assertEquals(userResources.message(HASHED_PASSWORD), view.i18n.getForm().getPassword());
    assertEquals(resources.message(FORGOT_PASSWORD), view.i18n.getForm().getForgotPassword());
    assertEquals(resources.message(SIGNIN), view.i18n.getForm().getSubmit());
    assertEquals(resources.message(property(FAIL, TITLE)), view.i18n.getErrorMessage().getTitle());
    assertEquals(resources.message(FAIL), view.i18n.getErrorMessage().getMessage());
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, generalResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void beforeEnter_Anonymous() {
    when(authorizationService.isAnonymous()).thenReturn(true);
    view.beforeEnter(beforeEnterEvent);
    verifyNoInteractions(beforeEnterEvent);
  }

  @Test
  public void beforeEnter_User() {
    when(authorizationService.isAnonymous()).thenReturn(false);
    view.beforeEnter(beforeEnterEvent);
    verify(beforeEnterEvent).forwardTo(MainView.class);
  }

  @Test
  public void afterNavigationEvent_NoError() {
    parameters.put(FAIL, null);

    view.afterNavigation(afterNavigationEvent);

    assertTrue(view.isError());
  }

  @Test
  public void afterNavigationEvent_Fail() {
    parameters.put(FAIL, null);

    view.afterNavigation(afterNavigationEvent);

    assertTrue(view.isError());
  }

  @Test
  public void afterNavigationEvent_Disabled() {
    parameters.put(DISABLED, null);

    view.afterNavigation(afterNavigationEvent);

    assertTrue(view.isError());
  }

  @Test
  public void afterNavigationEvent_ExcessiveAttempts() {
    parameters.put(LOCKED, null);

    view.afterNavigation(afterNavigationEvent);

    assertTrue(view.isError());
  }

  @Test
  public void forgotPassword() {
    view.fireForgotPasswordEvent();
    verify(ui).navigate(ForgotPasswordView.class);
  }
}
