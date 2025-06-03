package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.FRENCH;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.WarningNotification.CLOSE;
import static ca.qc.ircm.lanaseq.web.WarningNotification.STYLE;
import static ca.qc.ircm.lanaseq.web.WarningNotification.WARNING;
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
 * Tests for {@link WarningNotification}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class WarningNotificationTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(ErrorNotification.class);
  private WarningNotification warningNotification;
  private Locale locale = ENGLISH;
  private final String warningText = "warning text";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    navigate(DatasetsView.class);
    warningNotification = new WarningNotification(warningText);
  }

  @Test
  public void styles() {
    assertTrue(warningNotification.hasClassName(STYLE));
    assertEquals(0, warningNotification.getDuration());
    assertEquals(Notification.Position.MIDDLE, warningNotification.getPosition());
    assertTrue(warningNotification.hasThemeName(NotificationVariant.LUMO_WARNING.getVariantName()));
    assertTrue(warningNotification.warning.hasClassName(styleName(STYLE, WARNING)));
    assertTrue(warningNotification.close.hasClassName(styleName(STYLE, CLOSE)));
    assertTrue(warningNotification.close.hasThemeName(
        ButtonVariant.LUMO_TERTIARY_INLINE.getVariantName()));
    validateIcon(VaadinIcon.CLOSE.create(), warningNotification.close.getIcon());
  }

  @Test
  public void labels() {
    assertEquals(warningNotification.getTranslation(MESSAGE_PREFIX + CLOSE),
        warningNotification.close.getAriaLabel().orElse(""));
  }

  @Test
  public void labels_French() {
    locale = FRENCH;
    UI.getCurrent().setLocale(locale);
    warningNotification = new WarningNotification(warningText);
    assertEquals(warningNotification.getTranslation(MESSAGE_PREFIX + CLOSE),
        warningNotification.close.getAriaLabel().orElse(""));
  }

  @Test
  public void getText() {
    assertEquals(warningText, warningNotification.getText());
  }

  @Test
  public void setText() {
    assertEquals(warningText, warningNotification.getText());
    String errorText = RandomStringUtils.insecure().nextAlphanumeric(20);
    warningNotification.setText(errorText);
    assertEquals(errorText, warningNotification.getText());
  }

  @Test
  public void close() {
    warningNotification.open();
    assertTrue(warningNotification.isOpened());
    warningNotification.close();
    assertFalse(warningNotification.isOpened());
  }
}
