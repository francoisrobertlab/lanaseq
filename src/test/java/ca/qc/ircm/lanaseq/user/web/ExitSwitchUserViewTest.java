package ca.qc.ircm.lanaseq.user.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.SwitchUserService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link ExitSwitchUserView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ExitSwitchUserViewTest extends SpringUIUnitTest {
  private ExitSwitchUserView view;
  @MockBean
  private SwitchUserService switchUserService;
  @Autowired
  private AuthenticatedUser authenticatedUser;
  @Mock
  private BeforeEnterEvent event;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    view = new ExitSwitchUserView(switchUserService, authenticatedUser);
  }

  @Test
  public void beforeEnter() {
    view.beforeEnter(event);

    verify(switchUserService).exitSwitchUser(VaadinServletRequest.getCurrent());
    assertTrue(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream()
        .anyMatch(i -> i.getInvocation().getExpression().contains("window.open($0, $1)")
            && i.getInvocation().getParameters().size() > 0
            && i.getInvocation().getParameters().get(0).equals("/")));
  }
}
