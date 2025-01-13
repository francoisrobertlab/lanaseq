package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.user.web.PasswordView.HEADER;
import static ca.qc.ircm.lanaseq.user.web.PasswordView.ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link PasswordView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class PasswordViewTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(PasswordView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private PasswordView view;
  @MockitoBean
  private UserService service;
  @Autowired
  private AuthenticatedUser authenticatedUser;
  private Locale locale = Locale.ENGLISH;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    view = navigate(PasswordView.class);
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(HEADER, view.header.getId().orElse(""));
    assertEquals(SAVE, view.save.getId().orElse(""));
    assertTrue(view.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    validateIcon(VaadinIcon.CHECK.create(), view.save.getIcon());
  }

  @Test
  public void labels() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.header.getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + SAVE), view.save.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.header.getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + SAVE), view.save.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TITLE,
        view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME)), view.getPageTitle());
  }

  @Test
  public void passwords_Required() {
    assertTrue(view.passwords.isRequired());
  }

  @Test
  public void save_PasswordValidationFailed() {
    view.passwords = mock(PasswordsForm.class);
    @SuppressWarnings("unchecked")
    BinderValidationStatus<Passwords> passwordsValidationStatus =
        mock(BinderValidationStatus.class);
    when(view.passwords.validate()).thenReturn(passwordsValidationStatus);
    when(passwordsValidationStatus.isOk()).thenReturn(false);

    view.save();

    verify(service, never()).save(any());
    assertTrue($(PasswordView.class).exists());
  }

  @Test
  public void save() {
    view.passwords = mock(PasswordsForm.class);
    @SuppressWarnings("unchecked")
    BinderValidationStatus<Passwords> passwordsValidationStatus =
        mock(BinderValidationStatus.class);
    when(view.passwords.validate()).thenReturn(passwordsValidationStatus);
    when(passwordsValidationStatus.isOk()).thenReturn(true);
    String password = "test_password";
    when(view.passwords.getPassword()).thenReturn(password);

    view.save();

    verify(service).save(password);
    assertTrue($(DatasetsView.class).exists());
  }
}
