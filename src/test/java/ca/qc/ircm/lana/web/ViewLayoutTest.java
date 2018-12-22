/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lana.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.web.SigninView;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.router.BeforeEnterEvent;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class ViewLayoutTest extends AbstractViewTestCase {
  private ViewLayout view;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private BeforeEnterEvent event;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources = new MessageResource(ViewLayout.class, locale);
  private User user = new User(1L, "myuser");

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new ViewLayout(authorizationService);
    when(event.getNavigationTarget()).thenAnswer(i -> ViewTest.class);
    when(authorizationService.currentUser()).thenReturn(user);
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
    String message = resources.message(AccessDeniedException.class.getSimpleName(), user.getEmail(),
        ViewTest.class.getSimpleName());
    verify(event).rerouteToError(any(AccessDeniedException.class), eq(message));
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
