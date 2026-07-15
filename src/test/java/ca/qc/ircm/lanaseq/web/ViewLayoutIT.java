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
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ViewLayout}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ViewLayoutIT extends SpringBrowserlessTest {

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    navigate(VIEW_NAME, SigninView.class);
  }

  @Test
  public void datasets() {
    navigate(SamplesView.class);
    ViewLayout view = find(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.datasets.getLabel());
    assertTrue(find(DatasetsView.class).exists());
  }

  @Test
  public void samples() {
    navigate(DatasetsView.class);
    ViewLayout view = find(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.samples.getLabel());
    assertTrue(find(SamplesView.class).exists());
  }

  @Test
  public void protocols() {
    navigate(DatasetsView.class);
    ViewLayout view = find(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.protocols.getLabel());
    assertTrue(find(ProtocolsView.class).exists());
  }

  @Test
  public void publicFiles() {
    navigate(DatasetsView.class);
    ViewLayout view = find(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.publicFiles.getLabel());
    assertTrue(find(PublicFilesView.class).exists());
  }

  @Test
  public void jobs() {
    navigate(DatasetsView.class);
    ViewLayout view = find(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.jobs.getLabel());
    assertTrue(find(JobsView.class).exists());
  }

  @Test
  public void profile() {
    navigate(DatasetsView.class);
    ViewLayout view = find(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.profile.getLabel());
    assertTrue(find(ProfileView.class).exists());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void users() {
    navigate(DatasetsView.class);
    ViewLayout view = find(ViewLayout.class).first();
    test(view.sideNav).clickItem(view.users.getLabel());
    assertTrue(find(UsersView.class).exists());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  @Disabled("Done in SwitchUserIT.exitSwitchUser")
  public void exitSwitchUser() {
    navigate(UsersView.class);
    @SuppressWarnings("unchecked") Grid<User> usersGrid = (Grid<User>) find(Grid.class).first();
    test(usersGrid).select(2);
    find(Button.class).id(SWITCH_USER).click();
    ViewLayout view = find(ViewLayout.class).first();
    assertTrue(test(view.exitSwitchUser).isUsable());
    assertFalse(view.users.isVisible());
    test(view.sideNav).clickItem(view.exitSwitchUser.getLabel());
    assertTrue(find(DatasetsView.class).exists());
    view = find(ViewLayout.class).first();
    assertFalse(view.exitSwitchUser.isVisible());
    assertTrue(test(view.users).isUsable());
  }
}
