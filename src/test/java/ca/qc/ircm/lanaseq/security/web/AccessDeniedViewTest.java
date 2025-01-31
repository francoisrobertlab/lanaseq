package ca.qc.ircm.lanaseq.security.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.security.web.AccessDeniedView.HEADER;
import static ca.qc.ircm.lanaseq.security.web.AccessDeniedView.HOME;
import static ca.qc.ircm.lanaseq.security.web.AccessDeniedView.MESSAGE;
import static ca.qc.ircm.lanaseq.security.web.AccessDeniedView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import com.vaadin.flow.component.UI;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link AccessDeniedView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AccessDeniedViewTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(AccessDeniedView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private AccessDeniedView view;
  private final Locale locale = Locale.ENGLISH;

  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    assertThrows(IllegalArgumentException.class, () -> navigate(UsersView.class));
    view = $(AccessDeniedView.class).first();
  }

  @Test
  public void styles() {
    assertEquals(VIEW_NAME, view.getContent().getId().orElse(""));
    assertTrue(view.header.hasClassName(HEADER));
    assertTrue(view.message.hasClassName(MESSAGE));
    assertTrue(view.home.hasClassName(HOME));
  }

  @Test
  public void labels() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.header.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MESSAGE), view.message.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HOME), view.home.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HEADER), view.header.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MESSAGE), view.message.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HOME), view.home.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TITLE,
        view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME)), view.getPageTitle());
  }
}
