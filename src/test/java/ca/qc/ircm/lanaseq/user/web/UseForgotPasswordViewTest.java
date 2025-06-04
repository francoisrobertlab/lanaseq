package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.FRENCH;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.HEADER;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.ID;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.INVALID;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.MESSAGE;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SAVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.ForgotPassword;
import ca.qc.ircm.lanaseq.user.ForgotPasswordService;
import ca.qc.ircm.lanaseq.web.ErrorNotification;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link UseForgotPasswordView}.
 */
@ServiceTestAnnotations
@WithAnonymousUser
public class UseForgotPasswordViewTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(UseForgotPasswordView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private UseForgotPasswordView view;
  @MockitoBean
  private ForgotPasswordService service;
  @Mock
  private ForgotPassword forgotPassword;
  private final Locale locale = ENGLISH;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(service.get(anyLong(), any())).thenReturn(Optional.of(forgotPassword));
    UI.getCurrent().setLocale(locale);
    String parameter = "2/b";
    view = navigate(UseForgotPasswordView.class, parameter);
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(HEADER, view.header.getId().orElse(""));
    assertEquals(MESSAGE, view.message.getId().orElse(""));
    assertEquals(SAVE, view.save.getId().orElse(""));
    assertTrue(view.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
  }

  @Test
  public void labels() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.header.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MESSAGE), view.message.getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + SAVE), view.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), view.save.getIcon());
  }

  @Test
  public void localeChange() {
    Locale locale = FRENCH;
    UI.getCurrent().setLocale(locale);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.header.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MESSAGE), view.message.getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + SAVE), view.save.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TITLE,
        view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME)), view.getPageTitle());
  }

  @Test
  public void save_Invalid() {
    view.form = mock(PasswordsForm.class);

    view.save();

    verify(view.form).isValid();
    verify(service, never()).updatePassword(any(), any());
  }

  @Test
  public void save() {
    String password = "test_password";
    view.form = mock(PasswordsForm.class);
    when(view.form.isValid()).thenReturn(true);
    when(view.form.getPassword()).thenReturn(password);

    view.save();

    verify(view.form).isValid();
    verify(service).updatePassword(eq(forgotPassword), eq(password));
    assertTrue($(SigninView.class).exists());
    Notification notification = $(Notification.class).first();
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAVED), test(notification).getText());
  }

  @Test
  public void setParameter() {
    String parameter = "34925/feafet23ts";
    view = navigate(UseForgotPasswordView.class, parameter);
    verify(service, atLeastOnce()).get(34925L, "feafet23ts");
    assertTrue(view.save.isEnabled());
    assertTrue(view.form.isEnabled());
  }

  @Test
  public void setParameter_IdNotNumber() {
    String parameter = "A434GS";
    view = navigate(UseForgotPasswordView.class, parameter);
    verify(service, times(2)).get(anyLong(), any());
    Notification notification = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, notification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + INVALID),
        ((ErrorNotification) notification).getText());
    assertFalse(view.save.isEnabled());
    assertFalse(view.form.isEnabled());
  }

  @Test
  public void setParameter_MissingConfirm() {
    view.form = mock(PasswordsForm.class);
    String parameter = "34925";
    view = navigate(UseForgotPasswordView.class, parameter);
    verify(service, times(2)).get(anyLong(), any());
    Notification notification = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, notification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + INVALID),
        ((ErrorNotification) notification).getText());
    assertFalse(view.save.isEnabled());
    assertFalse(view.form.isEnabled());
  }

  @Test
  public void setParameter_NullForgotPassword() {
    when(service.get(any(Long.class), any())).thenReturn(Optional.empty());
    String parameter = "34925/feafet23ts";
    view = navigate(UseForgotPasswordView.class, parameter);
    verify(service, atLeastOnce()).get(34925L, "feafet23ts");
    Notification notification = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, notification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + INVALID),
        ((ErrorNotification) notification).getText());
    assertFalse(view.save.isEnabled());
    assertFalse(view.form.isEnabled());
  }

  @Test
  public void setParameter_Null() {
    view = navigate(UseForgotPasswordView.class);
    verify(service, times(2)).get(anyLong(), any());
    Notification notification = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, notification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + INVALID),
        ((ErrorNotification) notification).getText());
    assertFalse(view.save.isEnabled());
    assertFalse(view.form.isEnabled());
  }
}
