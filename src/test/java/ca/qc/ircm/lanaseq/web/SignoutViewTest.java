package ca.qc.ircm.lanaseq.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.testbench.unit.SpringUIUnitTest;
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
public class SignoutViewTest extends SpringUIUnitTest {
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
    assertThrows(IllegalStateException.class, () -> {
      view.beforeEnter(event);
    });
    assertThrows(IllegalStateException.class, () -> {
      VaadinServletRequest.getCurrent().getWrappedSession(false).getAttributeNames();
    });

    assertTrue(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream()
        .anyMatch(i -> i.getInvocation().getExpression().contains("window.open($0, $1)")
            && i.getInvocation().getParameters().size() > 0
            && i.getInvocation().getParameters().get(0).equals("/")));
  }
}
