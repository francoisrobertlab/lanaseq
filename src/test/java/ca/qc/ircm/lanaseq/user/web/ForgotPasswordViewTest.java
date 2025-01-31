package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.FRENCH;
import static ca.qc.ircm.lanaseq.Constants.INVALID_EMAIL;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.HEADER;
import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.ID;
import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.MESSAGE;
import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.SAVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.ForgotPassword;
import ca.qc.ircm.lanaseq.user.ForgotPasswordService;
import ca.qc.ircm.lanaseq.user.ForgotPasswordWebContext;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link ForgotPasswordView}.
 */
@ServiceTestAnnotations
@WithAnonymousUser
public class ForgotPasswordViewTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(ForgotPasswordView.class);
  private static final String USER_PREFIX = messagePrefix(User.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private ForgotPasswordView view;
  @MockitoBean
  private ForgotPasswordService service;
  @MockitoBean
  private UserService userService;
  @Captor
  private ArgumentCaptor<ForgotPasswordWebContext> webContextCaptor;
  private final Locale locale = ENGLISH;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    view = navigate(ForgotPasswordView.class);
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(HEADER, view.header.getId().orElse(""));
    assertEquals(MESSAGE, view.message.getId().orElse(""));
    assertEquals(EMAIL, view.email.getId().orElse(""));
    assertEquals(SAVE, view.save.getId().orElse(""));
    assertTrue(view.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
  }

  @Test
  public void labels() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.header.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MESSAGE), view.message.getText());
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), view.email.getLabel());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + SAVE), view.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), view.save.getIcon());
  }

  @Test
  public void localeChange() {
    Locale locale = FRENCH;
    UI.getCurrent().setLocale(locale);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.header.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MESSAGE), view.message.getText());
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), view.email.getLabel());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + SAVE), view.save.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TITLE,
        view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME)), view.getPageTitle());
  }

  @Test
  public void save_EmailEmtpy() {
    view.email.setValue("");

    view.save();

    BinderValidationStatus<User> status = view.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, view.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(view.getTranslation(CONSTANTS_PREFIX + REQUIRED)), error.getMessage());
    assertTrue($(ForgotPasswordView.class).exists());
    assertFalse($(Notification.class).exists());
  }

  @Test
  public void save_EmailInvalid() {
    view.email.setValue("test");

    view.save();

    BinderValidationStatus<User> status = view.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, view.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(view.getTranslation(CONSTANTS_PREFIX + INVALID_EMAIL)),
        error.getMessage());
    verify(service, never()).insert(any(), any());
    assertTrue($(ForgotPasswordView.class).exists());
    assertFalse($(Notification.class).exists());
  }

  @Test
  public void save_EmailNotExists() {
    String email = "test@ircm.qc.ca";
    view.email.setValue(email);

    view.save();

    verify(userService).exists(email);
    verify(service, never()).insert(any(), any());
    assertTrue($(SigninView.class).exists());
    Notification notification = $(Notification.class).first();
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAVED, email), test(notification).getText());
  }

  @Test
  public void save() {
    when(userService.exists(any())).thenReturn(true);
    String email = "test@ircm.qc.ca";
    view.email.setValue(email);
    ForgotPassword forgotPassword = new ForgotPassword();
    forgotPassword.setId(34925L);
    forgotPassword.setConfirmNumber("feafet23ts");

    view.save();

    verify(userService).exists(email);
    verify(service).insert(eq(email), webContextCaptor.capture());
    ForgotPasswordWebContext webContext = webContextCaptor.getValue();
    String url = webContext.getChangeForgottenPasswordUrl(forgotPassword, locale);
    assertEquals("/" + UseForgotPasswordView.VIEW_NAME + "/" + forgotPassword.getId()
        + UseForgotPasswordView.SEPARATOR + forgotPassword.getConfirmNumber(), url);
    assertTrue($(SigninView.class).exists());
    Notification notification = $(Notification.class).first();
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAVED, email), test(notification).getText());
  }
}
