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

package ca.qc.ircm.lana.user.web;

import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.user.UserService;
import ca.qc.ircm.lana.web.SaveEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.H2;
import java.util.List;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UsersViewPresenterTest extends AbstractViewTestCase {
  private UsersViewPresenter presenter;
  @Mock
  private UsersView view;
  @Mock
  private UserService userService;
  @Captor
  private ArgumentCaptor<User> userCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SaveEvent<UserWithPassword>>> saveListenerCaptor;
  @Inject
  private UserRepository userRepository;
  private List<User> users;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new UsersViewPresenter(userService);
    view.header = new H2();
    view.users = new Grid<>();
    view.users.setSelectionMode(SelectionMode.MULTI);
    view.userDialog = mock(UserDialog.class);
    users = userRepository.findAll();
    when(userService.all()).thenReturn(users);
  }

  @Test
  public void users() {
    presenter.init(view);
    List<User> users = items(view.users);
    assertEquals(this.users.size(), users.size());
    for (User user : this.users) {
      assertTrue(user.toString(), users.contains(user));
    }
    assertEquals(0, view.users.getSelectedItems().size());
    users.forEach(user -> view.users.select(user));
    assertEquals(users.size(), view.users.getSelectedItems().size());
  }

  @Test
  public void view() {
    presenter.init(view);
    User user = new User();
    user.setId(2L);
    User databaseUser = new User();
    when(userService.get(any())).thenReturn(databaseUser);
    presenter.view(user);
    verify(userService).get(2L);
    verify(view.userDialog).setUser(databaseUser);
    verify(view.userDialog).open();
  }

  @Test
  public void add() {
    presenter.init(view);
    presenter.add();
    verify(view.userDialog).setUser(userCaptor.capture());
    User user = userCaptor.getValue();
    assertNull(user.getId());
    assertNull(user.getEmail());
    assertNull(user.getName());
    assertNull(user.getLaboratory());
    verify(view.userDialog).open();
  }
}
