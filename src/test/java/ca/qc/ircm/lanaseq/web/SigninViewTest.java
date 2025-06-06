package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.HASHED_PASSWORD;
import static ca.qc.ircm.lanaseq.web.SigninView.ADDITIONAL_INFORMATION;
import static ca.qc.ircm.lanaseq.web.SigninView.DESCRIPTION;
import static ca.qc.ircm.lanaseq.web.SigninView.DISABLED;
import static ca.qc.ircm.lanaseq.web.SigninView.FAIL;
import static ca.qc.ircm.lanaseq.web.SigninView.FORGOT_PASSWORD;
import static ca.qc.ircm.lanaseq.web.SigninView.FORM_TITLE;
import static ca.qc.ircm.lanaseq.web.SigninView.HEADER;
import static ca.qc.ircm.lanaseq.web.SigninView.ID;
import static ca.qc.ircm.lanaseq.web.SigninView.LOCKED;
import static ca.qc.ircm.lanaseq.web.SigninView.SIGNIN;
import static ca.qc.ircm.lanaseq.web.SigninView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.SecurityConfiguration;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;

/**
 * Tests for {@link SigninView}.
 */
@NonTransactionalTestAnnotations
@WithAnonymousUser
public class SigninViewTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(SigninView.class);
  private static final String USER_PREFIX = messagePrefix(User.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private SigninView view;
  @Autowired
  private SecurityConfiguration configuration;
  @Mock
  private AfterNavigationEvent afterNavigationEvent;
  @Mock
  private BeforeEnterEvent beforeEnterEvent;
  @Mock
  private Location location;
  @Mock
  private QueryParameters queryParameters;
  private final Locale locale = Locale.ENGLISH;
  private final Map<String, List<String>> parameters = new HashMap<>();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    when(location.getQueryParameters()).thenReturn(queryParameters);
    when(queryParameters.getParameters()).thenReturn(parameters);
    UI.getCurrent().setLocale(locale);
    view = navigate(SigninView.class);
  }

  @Test
  public void init() {
    assertEquals(VIEW_NAME, view.getAction());
    assertTrue(view.isOpened());
    assertTrue(view.isForgotPasswordButtonVisible());
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertTrue(view.isForgotPasswordButtonVisible());
  }

  @Test
  public void labels() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.i18n.getHeader().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DESCRIPTION),
        view.i18n.getHeader().getDescription());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ADDITIONAL_INFORMATION),
        view.i18n.getAdditionalInformation());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORM_TITLE), view.i18n.getForm().getTitle());
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), view.i18n.getForm().getUsername());
    assertEquals(view.getTranslation(USER_PREFIX + HASHED_PASSWORD),
        view.i18n.getForm().getPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SIGNIN), view.i18n.getForm().getSubmit());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORGOT_PASSWORD),
        view.i18n.getForm().getForgotPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + property(FAIL, TITLE)),
        view.i18n.getErrorMessage().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FAIL),
        view.i18n.getErrorMessage().getMessage());
    assertFalse(view.isError());
  }

  @Test
  public void labels_Error() {
    parameters.put("error", null);
    view.afterNavigation(afterNavigationEvent);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.i18n.getHeader().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DESCRIPTION),
        view.i18n.getHeader().getDescription());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ADDITIONAL_INFORMATION),
        view.i18n.getAdditionalInformation());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORM_TITLE), view.i18n.getForm().getTitle());
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), view.i18n.getForm().getUsername());
    assertEquals(view.getTranslation(USER_PREFIX + HASHED_PASSWORD),
        view.i18n.getForm().getPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SIGNIN), view.i18n.getForm().getSubmit());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORGOT_PASSWORD),
        view.i18n.getForm().getForgotPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + property(FAIL, TITLE)),
        view.i18n.getErrorMessage().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FAIL),
        view.i18n.getErrorMessage().getMessage());
  }

  @Test
  public void labels_Fail() {
    parameters.put(FAIL, null);
    view.afterNavigation(afterNavigationEvent);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.i18n.getHeader().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DESCRIPTION),
        view.i18n.getHeader().getDescription());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORM_TITLE), view.i18n.getForm().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ADDITIONAL_INFORMATION),
        view.i18n.getAdditionalInformation());
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), view.i18n.getForm().getUsername());
    assertEquals(view.getTranslation(USER_PREFIX + HASHED_PASSWORD),
        view.i18n.getForm().getPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SIGNIN), view.i18n.getForm().getSubmit());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORGOT_PASSWORD),
        view.i18n.getForm().getForgotPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + property(FAIL, TITLE)),
        view.i18n.getErrorMessage().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FAIL),
        view.i18n.getErrorMessage().getMessage());
  }

  @Test
  public void labels_Disabled() {
    parameters.put(DISABLED, null);
    view.afterNavigation(afterNavigationEvent);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.i18n.getHeader().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DESCRIPTION),
        view.i18n.getHeader().getDescription());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ADDITIONAL_INFORMATION),
        view.i18n.getAdditionalInformation());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORM_TITLE), view.i18n.getForm().getTitle());
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), view.i18n.getForm().getUsername());
    assertEquals(view.getTranslation(USER_PREFIX + HASHED_PASSWORD),
        view.i18n.getForm().getPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SIGNIN), view.i18n.getForm().getSubmit());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORGOT_PASSWORD),
        view.i18n.getForm().getForgotPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + property(DISABLED, TITLE)),
        view.i18n.getErrorMessage().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DISABLED),
        view.i18n.getErrorMessage().getMessage());
  }

  @Test
  public void labels_Locked() {
    parameters.put(LOCKED, null);
    view.afterNavigation(afterNavigationEvent);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.i18n.getHeader().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DESCRIPTION),
        view.i18n.getHeader().getDescription());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ADDITIONAL_INFORMATION),
        view.i18n.getAdditionalInformation());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORM_TITLE), view.i18n.getForm().getTitle());
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), view.i18n.getForm().getUsername());
    assertEquals(view.getTranslation(USER_PREFIX + HASHED_PASSWORD),
        view.i18n.getForm().getPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SIGNIN), view.i18n.getForm().getSubmit());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORGOT_PASSWORD),
        view.i18n.getForm().getForgotPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + property(LOCKED, TITLE)),
        view.i18n.getErrorMessage().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + LOCKED,
        configuration.lockDuration().getSeconds() / 60), view.i18n.getErrorMessage().getMessage());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.i18n.getHeader().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DESCRIPTION),
        view.i18n.getHeader().getDescription());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ADDITIONAL_INFORMATION),
        view.i18n.getAdditionalInformation());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORM_TITLE), view.i18n.getForm().getTitle());
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), view.i18n.getForm().getUsername());
    assertEquals(view.getTranslation(USER_PREFIX + HASHED_PASSWORD),
        view.i18n.getForm().getPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FORGOT_PASSWORD),
        view.i18n.getForm().getForgotPassword());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SIGNIN), view.i18n.getForm().getSubmit());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + property(FAIL, TITLE)),
        view.i18n.getErrorMessage().getTitle());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FAIL),
        view.i18n.getErrorMessage().getMessage());
  }

  @Test
  public void getPageTitle() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TITLE,
        view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME)), view.getPageTitle());
  }

  @Test
  public void afterNavigationEvent_NoError() {
    parameters.put(FAIL, null);

    view.afterNavigation(afterNavigationEvent);

    assertTrue(view.isError());
  }

  @Test
  public void afterNavigationEvent_Error() {
    parameters.put("error", null);

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
    assertTrue($(ForgotPasswordView.class).exists());
  }
}
