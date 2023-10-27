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
import static ca.qc.ircm.lanaseq.user.web.UsersView.USERS_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.SwitchUserService;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.server.VaadinServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link UsersViewPresenter}.
 */
@ServiceTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UsersViewPresenterTest extends AbstractKaribuTestCase {
  private UsersViewPresenter presenter;
  @Mock
  private UsersView view;
  @Mock
  private SwitchUserService switchUserService;
  @Mock
  private UserService userService;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Autowired
  private ObjectFactory<UserDialog> dialogFactory;
  @MockBean
  private UserDialog dialog;
  @Mock
  private ListDataProvider<User> dataProvider;
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
  @BeforeEach
  @SuppressWarnings("unchecked")
  public void beforeTest() {
    presenter = new UsersViewPresenter(userService, switchUserService, authenticatedUser);
    view.header = new H2();
    view.users = new Grid<>();
    view.users.setSelectionMode(SelectionMode.MULTI);
    view.active = mock(Column.class);
    view.error = new Div();
    view.add = new Button();
    view.switchUser = new Button();
    view.dialogFactory = dialogFactory;
    users = userRepository.findAll();
    when(userService.all()).thenReturn(users);
    currentUser = userRepository.findById(2L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(currentUser));
    presenter.init(view);
    presenter.localeChange(locale);
  }

  @Test
  public void users_User() {
    verify(userService).all();
    List<User> users = items(view.users);
    assertEquals(this.users.size(), users.size());
    for (User user : this.users) {
      assertTrue(users.contains(user), () -> user.toString());
    }
    assertEquals(0, view.users.getSelectedItems().size());
    users.forEach(user -> view.users.select(user));
    assertEquals(users.size(), view.users.getSelectedItems().size());
    verify(view.active).setVisible(false);
    assertFalse(view.add.isVisible());
    assertFalse(view.switchUser.isVisible());
  }

  @Test
  public void users_Manager() {
    when(authenticatedUser.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)).thenReturn(true);
    presenter.init(view);
    verify(userService, times(2)).all();
    List<User> users = items(view.users);
    assertEquals(this.users.size(), users.size());
    for (User user : this.users) {
      assertTrue(users.contains(user), () -> user.toString());
    }
    assertEquals(0, view.users.getSelectedItems().size());
    users.forEach(user -> view.users.select(user));
    assertEquals(users.size(), view.users.getSelectedItems().size());
    verify(view.active).setVisible(true);
    assertTrue(view.add.isVisible());
    assertFalse(view.switchUser.isVisible());
  }

  @Test
  public void users_Admin() {
    when(authenticatedUser.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)).thenReturn(true);
    when(authenticatedUser.hasRole(UserRole.ADMIN)).thenReturn(true);
    presenter.init(view);
    verify(userService, times(2)).all();
    List<User> users = items(view.users);
    assertEquals(this.users.size(), users.size());
    for (User user : this.users) {
      assertTrue(users.contains(user), () -> user.toString());
    }
    assertEquals(0, view.users.getSelectedItems().size());
    users.forEach(user -> view.users.select(user));
    assertEquals(users.size(), view.users.getSelectedItems().size());
    verify(view.active).setVisible(true);
    assertTrue(view.add.isVisible());
    assertTrue(view.switchUser.isVisible());
  }

  @Test
  public void filterEmail() {
    view.users.setItems(dataProvider);

    presenter.filterEmail("test");

    assertEquals("test", presenter.filter().emailContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterEmail_Empty() {
    view.users.setItems(dataProvider);

    presenter.filterEmail("");

    assertEquals(null, presenter.filter().emailContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName() {
    view.users.setItems(dataProvider);

    presenter.filterName("test");

    assertEquals("test", presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    view.users.setItems(dataProvider);

    presenter.filterName("");

    assertEquals(null, presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterActive_False() {
    view.users.setItems(dataProvider);

    presenter.filterActive(false);

    assertEquals(false, presenter.filter().active);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterActive_True() {
    view.users.setItems(dataProvider);

    presenter.filterActive(true);

    assertEquals(true, presenter.filter().active);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterActive_Null() {
    view.users.setItems(dataProvider);

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
    when(userService.get(any())).thenReturn(Optional.of(databaseUser));
    presenter.view(user);
    verify(userService).get(2L);
    verify(dialog).setUser(databaseUser);
    verify(dialog).addSavedListener(any());
    verify(dialog).open();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshDatasetsOnUserSaved() {
    User user = mock(User.class);
    presenter.view(user);
    verify(dialog).addSavedListener(userSavedListenerCaptor.capture());
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
    UI.getCurrent().navigate(UsersView.class);
    User user = userRepository.findById(3L).orElse(null);
    view.users.select(user);
    presenter.switchUser();
    assertFalse(view.error.isVisible());
    verify(switchUserService).switchUser(user, VaadinServletRequest.getCurrent());
    assertTrue(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream()
        .anyMatch(i -> ("if ($1 == '_self') this.stopApplication(); window.open($0, $1)")
            .equals(i.getInvocation().getExpression())
            && "/".equals(i.getInvocation().getParameters().get(0))
            && "_self".equals(i.getInvocation().getParameters().get(1))));
  }

  @Test
  public void switchUser_EmptySelection() throws Throwable {
    UI.getCurrent().navigate(UsersView.class);
    presenter.switchUser();
    assertEquals(resources.message(USERS_REQUIRED), view.error.getText());
    assertTrue(view.error.isVisible());
    verify(switchUserService, never()).switchUser(any(), any());
    assertCurrentView(UsersView.class);
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
    verify(dialog).setUser(null);
    verify(dialog).addSavedListener(any());
    verify(dialog).open();
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
