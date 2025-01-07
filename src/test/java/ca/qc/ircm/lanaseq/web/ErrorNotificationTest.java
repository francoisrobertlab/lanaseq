package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.FRENCH;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.ErrorNotification.CLOSE;
import static ca.qc.ircm.lanaseq.web.ErrorNotification.ERROR;
import static ca.qc.ircm.lanaseq.web.ErrorNotification.STYLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Locale;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link ErrorNotification}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ErrorNotificationTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(ErrorNotification.class);
  private ErrorNotification errorNotification;
  private Locale locale = ENGLISH;
  private String errorText = "error text";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    navigate(DatasetsView.class);
    errorNotification = new ErrorNotification(errorText);
  }

  @Test
  public void styles() {
    assertTrue(errorNotification.hasClassName(STYLE));
    assertEquals(0, errorNotification.getDuration());
    assertEquals(Notification.Position.MIDDLE, errorNotification.getPosition());
    assertTrue(errorNotification.hasThemeName(NotificationVariant.LUMO_ERROR.getVariantName()));
    assertTrue(errorNotification.error.hasClassName(styleName(STYLE, ERROR)));
    assertTrue(errorNotification.close.hasClassName(styleName(STYLE, CLOSE)));
    assertTrue(
        errorNotification.close.hasThemeName(ButtonVariant.LUMO_TERTIARY_INLINE.getVariantName()));
    validateIcon(VaadinIcon.CLOSE.create(), errorNotification.close.getIcon());
  }

  @Test
  public void labels() {
    assertEquals(errorNotification.getTranslation(MESSAGE_PREFIX + CLOSE),
        errorNotification.close.getAriaLabel().orElse(""));
  }

  @Test
  public void labels_French() {
    locale = FRENCH;
    UI.getCurrent().setLocale(locale);
    errorNotification = new ErrorNotification(errorText);
    assertEquals(errorNotification.getTranslation(MESSAGE_PREFIX + CLOSE),
        errorNotification.close.getAriaLabel().orElse(""));
  }

  @Test
  public void getText() {
    assertEquals(errorText, errorNotification.getText());
  }

  @Test
  public void setText() {
    assertEquals(errorText, errorNotification.getText());
    String errorText = RandomStringUtils.insecure().nextAlphanumeric(20);
    errorNotification.setText(errorText);
    assertEquals(errorText, errorNotification.getText());
  }

  @Test
  public void close() {
    errorNotification.open();
    assertTrue(errorNotification.isOpened());
    errorNotification.close();
    assertFalse(errorNotification.isOpened());
  }
}
