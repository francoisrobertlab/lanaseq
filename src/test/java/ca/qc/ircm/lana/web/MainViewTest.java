package ca.qc.ircm.lana.web;

import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;
import static ca.qc.ircm.lana.user.UserRole.USER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lana.user.web.UsersView;
import com.vaadin.flow.router.BeforeEnterEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class MainViewTest {
  private MainView view;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private BeforeEnterEvent event;

  @Before
  public void beforeTest() {
    view = new MainView(authorizationService);
  }

  @Test
  public void beforeEnter_User() {
    when(authorizationService.hasRole(USER)).thenReturn(true);

    view.beforeEnter(event);

    //verify(event).rerouteTo(ExperimentsView.class);
  }

  @Test
  public void beforeEnter_Admin() {
    when(authorizationService.hasRole(USER)).thenReturn(true);
    when(authorizationService.hasRole(ADMIN)).thenReturn(true);

    view.beforeEnter(event);

    verify(event).rerouteTo(UsersView.class);
  }

  @Test
  public void beforeEnter_Manager() {
    when(authorizationService.hasRole(USER)).thenReturn(true);
    when(authorizationService.hasRole(MANAGER)).thenReturn(true);

    view.beforeEnter(event);

    verify(event).rerouteTo(UsersView.class);
  }

  @Test
  public void beforeEnter_NoRole() {
    view.beforeEnter(event);
  }
}
