package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.web.MainView.VIEW_NAME;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link MainView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class MainViewIT extends SpringUIUnitTest {

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    navigate(VIEW_NAME, SigninView.class);
  }

  @Test
  public void userRedirected() {
    navigate(VIEW_NAME, DatasetsView.class);
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void managerRedirected() {
    navigate(VIEW_NAME, DatasetsView.class);
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void adminRedirected() {
    navigate(VIEW_NAME, DatasetsView.class);
  }
}
