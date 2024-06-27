package ca.qc.ircm.lanaseq.security.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.sample.web.SamplesView;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.PasswordView;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.internal.AfterNavigationHandler;
import com.vaadin.flow.router.internal.BeforeEnterHandler;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link ConfigureUiServiceInitListener}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ConfigureUiServiceInitListenerTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(ConfigureUiServiceInitListener.class);
  private static final Logger logger =
      LoggerFactory.getLogger(ConfigureUiServiceInitListenerTest.class);
  @Mock
  private AfterNavigationListener navigationListener;
  @Mock
  private BeforeEnterEvent beforeEnterEvent;
  @Mock
  private AfterNavigationEvent afterNavigationEvent;
  @Captor
  private ArgumentCaptor<AfterNavigationEvent> afterNavigationEventCaptor;
  @Mock
  private Location location;
  private Locale locale = Locale.ENGLISH;
  private User user = new User(1L, "myuser");

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    UI.getCurrent().addAfterNavigationListener(navigationListener);
    when(beforeEnterEvent.getNavigationTarget()).thenAnswer(i -> SamplesView.class);
    when(beforeEnterEvent.getUI()).thenReturn(UI.getCurrent());
    when(beforeEnterEvent.getLocation()).thenReturn(location);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    when(location.getPath()).thenReturn("");
  }

  private void doBeforeEnter() {
    List<BeforeEnterHandler> handlers =
        UI.getCurrent().getInternals().getListeners(BeforeEnterHandler.class);
    handlers.get(0).beforeEnter(beforeEnterEvent);
  }

  private void doAfterNavigation() {
    List<AfterNavigationHandler> handlers =
        UI.getCurrent().getInternals().getListeners(AfterNavigationHandler.class);
    handlers.get(0).afterNavigation(afterNavigationEvent);
  }

  @Test
  public void beforeEnter_Authorized() {
    doBeforeEnter();

    assertTrue($(DatasetsView.class).exists());
  }

  @Test
  public void beforeEnter_NotAuthorized() {
    when(beforeEnterEvent.getNavigationTarget()).thenAnswer(i -> UsersView.class);

    doBeforeEnter();

    String message =
        UI.getCurrent().getTranslation(MESSAGE_PREFIX + AccessDeniedException.class.getSimpleName(),
            "jonh.smith@ircm.qc.ca", UsersView.class.getSimpleName());
    verify(beforeEnterEvent).rerouteToError(any(AccessDeniedException.class), eq(message));
  }

  @Test
  @WithAnonymousUser
  public void beforeEnter_NotAuthorizedAnonymous() {
    doBeforeEnter();

    verify(navigationListener).afterNavigation(afterNavigationEventCaptor.capture());
    AfterNavigationEvent afterNavigationEvent = afterNavigationEventCaptor.getValue();
    assertEquals(SigninView.VIEW_NAME, afterNavigationEvent.getLocation().getPath());
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
