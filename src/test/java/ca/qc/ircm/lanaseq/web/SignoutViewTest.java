package ca.qc.ircm.lanaseq.web;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.VaadinServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link SignoutView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SignoutViewTest extends SpringBrowserlessTest {

  private SignoutView view;
  @Autowired
  private AuthenticatedUser authenticatedUser;
  @Mock
  private BeforeEnterEvent event;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    view = new SignoutView(authenticatedUser);
  }

  @Test
  public void beforeEnter() {
    // Invalidated session.
    view.beforeEnter(event);
    assertNull(VaadinServletRequest.getCurrent().getWrappedSession(false));

    assertTrue(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream().anyMatch(
        i -> i.getInvocation().getExpression().contains("vaadin-redirect-pending")
            && !i.getInvocation().getParameters().isEmpty() && i.getInvocation().getParameters()
            .getFirst().equals("/" + SigninView.VIEW_NAME)));
  }
}
