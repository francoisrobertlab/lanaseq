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

package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_FAILED;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_USERNAME;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_USER_FORM;
import static ca.qc.ircm.lanaseq.user.web.UsersView.USERS_REQUIRED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UsersViewPresenterTest extends AbstractViewTestCase {
  private UsersViewPresenter presenter;
  @Mock
  private UsersView view;
  @Mock
  private UserService userService;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private DataProvider<User, ?> dataProvider;
  @Captor
  private ArgumentCaptor<User> userCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<UserDialog>>> userSavedListenerCaptor;
  @Autowired
  private UserRepository userRepository;
  private List<User> users;
  private User currentUser;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(UsersView.class, locale);

  /**
   * Before test.
   */
  @Before
  @SuppressWarnings("unchecked")
  public void beforeTest() {
    presenter = new UsersViewPresenter(userService, authorizationService);
    view.header = new H2();
    view.users = new Grid<>();
    view.users.setSelectionMode(SelectionMode.MULTI);
    view.active = mock(Column.class);
    view.error = new Div();
    view.add = new Button();
    view.switchUser = new Button();
    view.switchUserForm = new Html("<form></form>");
    view.userDialog = mock(UserDialog.class);
    users = userRepository.findAll();
    when(userService.all()).thenReturn(users);
    currentUser = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(currentUser);
    presenter.init(view);
    presenter.localeChange(locale);
  }

  @Test
  public void users_User() {
    verify(userService).all();
    List<User> users = items(view.users);
    assertEquals(this.users.size(), users.size());
    for (User user : this.users) {
      assertTrue(user.toString(), users.contains(user));
    }
    assertEquals(0, view.users.getSelectedItems().size());
    users.forEach(user -> view.users.select(user));
    assertEquals(users.size(), view.users.getSelectedItems().size());
    verify(view.active).setVisible(false);
    assertFalse(view.add.isVisible());
    assertFalse(view.switchUser.isVisible());
    assertFalse(view.switchUserForm.isVisible());
  }

  @Test
  public void users_Manager() {
    when(authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)).thenReturn(true);
    presenter.init(view);
    verify(userService, times(2)).all();
    List<User> users = items(view.users);
    assertEquals(this.users.size(), users.size());
    for (User user : this.users) {
      assertTrue(user.toString(), users.contains(user));
    }
    assertEquals(0, view.users.getSelectedItems().size());
    users.forEach(user -> view.users.select(user));
    assertEquals(users.size(), view.users.getSelectedItems().size());
    verify(view.active).setVisible(true);
    assertTrue(view.add.isVisible());
    assertFalse(view.switchUser.isVisible());
    assertFalse(view.switchUserForm.isVisible());
  }

  @Test
  public void users_Admin() {
    when(authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)).thenReturn(true);
    when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
    presenter.init(view);
    verify(userService, times(2)).all();
    List<User> users = items(view.users);
    assertEquals(this.users.size(), users.size());
    for (User user : this.users) {
      assertTrue(user.toString(), users.contains(user));
    }
    assertEquals(0, view.users.getSelectedItems().size());
    users.forEach(user -> view.users.select(user));
    assertEquals(users.size(), view.users.getSelectedItems().size());
    verify(view.active).setVisible(true);
    assertTrue(view.add.isVisible());
    assertTrue(view.switchUser.isVisible());
    assertTrue(view.switchUserForm.isVisible());
  }

  @Test
  public void filterEmail() {
    view.users.setDataProvider(dataProvider);

    presenter.filterEmail("test");

    assertEquals("test", presenter.filter().emailContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterEmail_Empty() {
    view.users.setDataProvider(dataProvider);

    presenter.filterEmail("");

    assertEquals(null, presenter.filter().emailContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName() {
    view.users.setDataProvider(dataProvider);

    presenter.filterName("test");

    assertEquals("test", presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    view.users.setDataProvider(dataProvider);

    presenter.filterName("");

    assertEquals(null, presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterActive_False() {
    view.users.setDataProvider(dataProvider);

    presenter.filterActive(false);

    assertEquals(false, presenter.filter().active);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterActive_True() {
    view.users.setDataProvider(dataProvider);

    presenter.filterActive(true);

    assertEquals(true, presenter.filter().active);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterActive_Null() {
    view.users.setDataProvider(dataProvider);

    presenter.filterActive(null);

    assertEquals(null, presenter.filter().active);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void error() {
    assertFalse(view.error.isVisible());
  }

  @Test
  public void view() {
    User user = new User();
    user.setId(2L);
    User databaseUser = userRepository.findById(2L).orElse(null);
    when(userService.get(any())).thenReturn(databaseUser);
    presenter.view(user);
    verify(userService).get(2L);
    verify(view.userDialog).setUser(databaseUser);
    verify(view.userDialog).open();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshDatasetsOnUserSaved() {
    verify(view.userDialog).addSavedListener(userSavedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<UserDialog>> savedListener =
        userSavedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(userService, times(2)).all();
  }

  @Test
  public void toggleActive_Active() {
    User user = userRepository.findById(3L).orElse(null);
    presenter.toggleActive(user);
    verify(userService).save(user, null);
    assertFalse(user.isActive());
  }

  @Test
  public void toggleActive_Inactive() {
    User user = userRepository.findById(7L).orElse(null);
    presenter.toggleActive(user);
    verify(userService).save(user, null);
    assertTrue(user.isActive());
  }

  @Test
  public void switchUser() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    view.users.select(user);
    presenter.switchUser();
    assertFalse(view.error.isVisible());
    verify(page).executeJs("document.getElementById(\"" + SWITCH_USERNAME + "\").value = \""
        + user.getEmail() + "\"; document.getElementById(\"" + SWITCH_USER_FORM + "\").submit()");
  }

  @Test
  public void switchUser_EmptySelection() throws Throwable {
    presenter.switchUser();
    assertEquals(resources.message(USERS_REQUIRED), view.error.getText());
    assertTrue(view.error.isVisible());
    verify(page, never()).executeJs(any());
  }

  @Test
  public void permissions_ErrorThenView() {
    presenter.switchUser();
    presenter.view(users.get(1));
    assertFalse(view.error.isVisible());
  }

  @Test
  public void add() {
    presenter.add();
    verify(view.userDialog).setUser(userCaptor.capture());
    User user = userCaptor.getValue();
    assertNull(user.getId());
    assertNull(user.getEmail());
    assertNull(user.getName());
    verify(view.userDialog).open();
  }

  @Test
  public void showError_NoError() {
    Map<String, List<String>> parameters = new HashMap<>();
    presenter.showError(parameters);
    verify(view, never()).showNotification(any());
  }

  @Test
  public void showError_SwitchFailed() {
    Map<String, List<String>> parameters = new HashMap<>();
    parameters.put(SWITCH_FAILED, Collections.emptyList());
    presenter.showError(parameters);
    verify(view).showNotification(resources.message(SWITCH_FAILED));
  }
}
