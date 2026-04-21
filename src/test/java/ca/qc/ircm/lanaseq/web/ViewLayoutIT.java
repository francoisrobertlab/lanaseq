package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_USER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.files.web.PublicFilesView;
import ca.qc.ircm.lanaseq.jobs.web.JobsView;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolsView;
import ca.qc.ircm.lanaseq.sample.web.SamplesView;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.ProfileView;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ViewLayout}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ViewLayoutIT extends SpringUIUnitTest {

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    navigate(VIEW_NAME, SigninView.class);
  }

  @Test
  public void datasets() {
    navigate(SamplesView.class);
    ViewLayout view = $(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.datasets.getLabel());
    assertTrue($(DatasetsView.class).exists());
  }

  @Test
  public void samples() {
    navigate(DatasetsView.class);
    ViewLayout view = $(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.samples.getLabel());
    assertTrue($(SamplesView.class).exists());
  }

  @Test
  public void protocols() {
    navigate(DatasetsView.class);
    ViewLayout view = $(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.protocols.getLabel());
    assertTrue($(ProtocolsView.class).exists());
  }

  @Test
  public void publicFiles() {
    navigate(DatasetsView.class);
    ViewLayout view = $(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.publicFiles.getLabel());
    assertTrue($(PublicFilesView.class).exists());
  }

  @Test
  public void jobs() {
    navigate(DatasetsView.class);
    ViewLayout view = $(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.jobs.getLabel());
    assertTrue($(JobsView.class).exists());
  }

  @Test
  public void profile() {
    navigate(DatasetsView.class);
    ViewLayout view = $(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.profile.getLabel());
    assertTrue($(ProfileView.class).exists());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void users() {
    navigate(DatasetsView.class);
    ViewLayout view = $(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.users.getLabel());
    assertTrue($(UsersView.class).exists());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  @Disabled("Done in SwitchUserIT.exitSwitchUser")
  public void exitSwitchUser() {
    navigate(UsersView.class);
    @SuppressWarnings("unchecked") Grid<User> usersGrid = (Grid<User>) $(Grid.class).first();
    test(usersGrid).select(2);
    $(Button.class).id(SWITCH_USER).click();
    ViewLayout view = $(ViewLayout.class).first();
    assertTrue(test(view.exitSwitchUser).isUsable());
    assertFalse(view.users.isVisible());
    test(view.sideNav).clickItem(view.exitSwitchUser.getLabel());
    assertTrue($(DatasetsView.class).exists());
    view = $(ViewLayout.class).first();
    assertFalse(view.exitSwitchUser.isVisible());
    assertTrue(test(view.users).isUsable());
  }
}
