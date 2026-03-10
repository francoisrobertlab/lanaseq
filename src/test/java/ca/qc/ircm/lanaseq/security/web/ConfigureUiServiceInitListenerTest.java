package ca.qc.ircm.lanaseq.security.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.PasswordView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.internal.AfterNavigationHandler;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link ConfigureUiServiceInitListener}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ConfigureUiServiceInitListenerTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(ConfigureUiServiceInitListener.class);
  private static final Logger logger = LoggerFactory.getLogger(
      ConfigureUiServiceInitListenerTest.class);
  @Mock
  private AfterNavigationListener navigationListener;
  @Mock
  private AfterNavigationEvent afterNavigationEvent;
  @Captor
  private ArgumentCaptor<AfterNavigationEvent> afterNavigationEventCaptor;
  @Mock
  private Location location;
  private final Locale locale = Locale.ENGLISH;
  private final User user = new User(1L, "myuser");

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    UI.getCurrent().addAfterNavigationListener(navigationListener);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    when(location.getPath()).thenReturn("");
  }

  private void doAfterNavigation() {
    List<AfterNavigationHandler> handlers = UI.getCurrent().getInternals()
        .getListeners(AfterNavigationHandler.class);
    handlers.getFirst().afterNavigation(afterNavigationEvent);
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void afterNavigation_ForceChangePassword() {
    doAfterNavigation();

    verify(navigationListener).afterNavigation(afterNavigationEventCaptor.capture());
    AfterNavigationEvent afterNavigationEvent = afterNavigationEventCaptor.getValue();
    assertEquals(PasswordView.VIEW_NAME, afterNavigationEvent.getLocation().getPath());
  }

  @Test
  public void afterNavigation_ForceChangePasswordAlreadyOnView() {
    when(location.getPath()).thenReturn(PasswordView.VIEW_NAME);

    doAfterNavigation();

    verify(navigationListener, never()).afterNavigation(any());
  }

  @Test
  public void afterNavigation_NotForceChangePassword() {
    doAfterNavigation();

    verify(navigationListener, never()).afterNavigation(any());
  }
}
