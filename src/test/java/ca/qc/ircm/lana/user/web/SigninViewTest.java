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

package ca.qc.ircm.lana.user.web;

import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.HASHED_PASSWORD;
import static ca.qc.ircm.lana.user.web.SigninView.DO_SIGNIN;
import static ca.qc.ircm.lana.user.web.SigninView.FAIL;
import static ca.qc.ircm.lana.user.web.SigninView.HEADER;
import static ca.qc.ircm.lana.user.web.SigninView.PASSWORD;
import static ca.qc.ircm.lana.user.web.SigninView.SIGNIN;
import static ca.qc.ircm.lana.user.web.SigninView.USERNAME;
import static ca.qc.ircm.lana.user.web.SigninView.VIEW_NAME;
import static ca.qc.ircm.lana.web.WebConstants.APPLICATION_NAME;
import static ca.qc.ircm.lana.web.WebConstants.TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.security.web.WebSecurityConfiguration;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.server.InitialPageSettings;
import java.util.Locale;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class SigninViewTest extends AbstractViewTestCase {
  private SigninView view;
  @Mock
  private SigninViewPresenter presenter;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources = new MessageResource(SigninView.class, locale);
  private MessageResource userResources = new MessageResource(User.class, locale);
  private MessageResource generalResources = new MessageResource(WebConstants.class, locale);

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new SigninView(presenter);
    view.init();
  }

  @Test
  public void presenter_Init() {
    view.onAttach(mock(AttachEvent.class));
    verify(presenter).init(view);
  }

  @Test
  public void styles() {
    assertTrue(view.getContent().getId().orElse("").equals(VIEW_NAME));
    assertTrue(view.header.getClassNames().contains(HEADER));
    assertEquals("iron-form", view.ironForm.getTag());
    assertEquals("ironform", view.ironForm.getAttribute("id"));
    assertEquals("", view.ironForm.getAttribute("allow-redirect"));
    assertEquals("form", view.form.getTag());
    assertEquals("post", view.form.getAttribute("method"));
    assertEquals(WebSecurityConfiguration.SIGNIN_PROCESSING_URL, view.form.getAttribute("action"));
    assertTrue(view.email.getClassNames().contains(EMAIL));
    assertEquals(USERNAME, view.email.getElement().getAttribute("name"));
    assertTrue(view.password.getClassNames().contains(HASHED_PASSWORD));
    assertEquals(PASSWORD, view.password.getElement().getAttribute("name"));
    assertTrue(view.signin.getClassNames().contains(SIGNIN));
    assertEquals(DO_SIGNIN, view.doSignin.getId().orElse(""));
    assertEquals("none", view.doSignin.getStyle().get("display"));
    assertTrue(view.error.getClassNames().contains(FAIL));
  }

  @Test
  public void labels() {
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals(userResources.message(EMAIL), view.email.getLabel());
    assertEquals(userResources.message(HASHED_PASSWORD), view.password.getLabel());
    assertEquals(resources.message(SIGNIN), view.signin.getText());
    assertEquals("", view.error.getText());
  }

  @Test
  public void configurePage() {
    InitialPageSettings settings = mock(InitialPageSettings.class);
    view.configurePage(settings);
    verify(settings).addInlineWithContents(InitialPageSettings.Position.PREPEND,
        "window.customElements=window.customElements||{};"
            + "window.customElements.forcePolyfill=true;" + "window.ShadyDOM={force:true};",
        InitialPageSettings.WrapMode.JAVASCRIPT);
  }

  @Test
  public void localeChange() {
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final MessageResource resources = new MessageResource(SigninView.class, locale);
    final MessageResource userResources = new MessageResource(User.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals(userResources.message(EMAIL), view.email.getLabel());
    assertEquals(userResources.message(HASHED_PASSWORD), view.password.getLabel());
    assertEquals(resources.message(SIGNIN), view.signin.getText());
    assertEquals("", view.error.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, generalResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void attach() {
    view = new SigninViewForTest(presenter);
    view.onAttach(mock(AttachEvent.class));
    verify(page).executeJavaScript("document.getElementById('" + DO_SIGNIN
        + "').addEventListener('click', () => document.getElementById('ironform').submit());");
  }

  @Test
  public void getLocale() {
    assertEquals(locale, view.getLocale());
  }

  @Test
  public void getLocale_French() {
    Locale locale = Locale.FRENCH;
    when(ui.getLocale()).thenReturn(locale);
    assertEquals(locale, view.getLocale());
  }

  @SuppressWarnings("serial")
  private class SigninViewForTest extends SigninView {
    public SigninViewForTest(SigninViewPresenter presenter) {
      super(presenter);
    }

    @Override
    public Optional<UI> getUI() {
      return Optional.of(ui);
    }
  }
}
