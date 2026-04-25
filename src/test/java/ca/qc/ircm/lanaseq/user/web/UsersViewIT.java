package ca.qc.ircm.lanaseq.user.web;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.security.web.AccessDeniedView;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.browserless.SpringBrowserlessTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link UsersView}.
 */
@ServiceTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UsersViewIT extends SpringBrowserlessTest {

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    navigate(UsersView.VIEW_NAME, SigninView.class);
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void security_User() {
    navigate(UsersView.VIEW_NAME, AccessDeniedView.class);
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void security_Manager() {
    navigate(UsersView.class);
  }

  @Test
  public void security_Admin() {
    navigate(UsersView.class);
  }

  @Test
  public void edit() {
    UsersView view = navigate(UsersView.class);

    test(view.users).select(0);
    test(view.edit).click();

    assertTrue($(UserDialog.class).exists());
  }

  @Test
  public void add() {
    UsersView view = navigate(UsersView.class);

    test(view.add).click();

    assertTrue($(UserDialog.class).exists());
  }
}
