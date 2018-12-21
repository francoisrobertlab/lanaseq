package ca.qc.ircm.lana.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lana.user.web.SigninView;
import com.vaadin.flow.router.BeforeEnterEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class ViewLayoutTest {
  private ViewLayout view;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private BeforeEnterEvent event;

  @Before
  public void beforeTest() {
    view = new ViewLayout(authorizationService);
    when(event.getNavigationTarget()).thenAnswer(i -> ViewTest.class);
  }

  @Test
  public void beforeEnter_Authorized() {
    when(authorizationService.isAuthorized(any())).thenReturn(true);

    view.beforeEnter(event);

    verify(authorizationService).isAuthorized(ViewTest.class);
  }

  @Test
  public void beforeEnter_NotAuthorized() {
    view.beforeEnter(event);

    verify(authorizationService).isAuthorized(ViewTest.class);
    verify(authorizationService).isAnonymous();
    verify(event).rerouteToError(AccessDeniedException.class);
  }

  @Test
  public void beforeEnter_NotAuthorizedAnonymous() {
    when(authorizationService.isAnonymous()).thenReturn(true);

    view.beforeEnter(event);

    verify(event).rerouteTo(SigninView.class);
    verify(authorizationService).isAuthorized(ViewTest.class);
    verify(authorizationService).isAnonymous();
  }

  public static class ViewTest {
  }
}
