package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ProtocolsView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolsViewIT extends SpringUIUnitTest {

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    navigate(VIEW_NAME, SigninView.class);
  }

  @Test
  public void edit() {
    ProtocolsView view = navigate(ProtocolsView.class);

    test(view.protocols).select(0);
    test(view.edit).click();

    assertTrue($(ProtocolDialog.class).exists());
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void history() {
    ProtocolsView view = navigate(ProtocolsView.class);

    test(view.protocols).select(2);
    test(view.history).click();

    assertTrue($(ProtocolHistoryDialog.class).exists());
  }

  @Test
  public void add() {
    ProtocolsView view = navigate(ProtocolsView.class);

    test(view.add).click();

    assertTrue($(ProtocolDialog.class).exists());
  }
}
