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

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.user.UserProperties.ACTIVE;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;
import static ca.qc.ircm.lanaseq.user.web.UsersView.HEADER;
import static ca.qc.ircm.lanaseq.user.web.UsersView.ID;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_USER;
import static ca.qc.ircm.lanaseq.user.web.UsersView.USERS;
import static ca.qc.ircm.lanaseq.user.web.UsersView.USERS_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.SwitchUserService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link UsersView}.
 */
@ServiceTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UsersViewTest extends SpringUIUnitTest {
  private UsersView view;
  @MockBean
  private UserService service;
  @MockBean
  private SwitchUserService switchUserService;
  @Captor
  private ArgumentCaptor<User> userCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<UserDialog>>> userSavedListenerCaptor;
  @Autowired
  private UserRepository repository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(UsersView.class, locale);
  private AppResources userResources = new AppResources(User.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<User> users;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    users = repository.findAll();
    when(service.all()).thenReturn(users);
    view = navigate(UsersView.class);
  }

  private User email(String email) {
    User user = new User();
    user.setEmail(email);
    return user;
  }

  private User name(String name) {
    User user = new User();
    user.setName(name);
    return user;
  }

  private User active(boolean active) {
    User user = new User();
    user.setActive(active);
    return user;
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(HEADER, view.header.getId().orElse(""));
    assertEquals(USERS, view.users.getId().orElse(""));
    assertEquals(ERROR_TEXT, view.error.getId().orElse(""));
    assertEquals(ADD, view.add.getId().orElse(""));
    assertEquals(SWITCH_USER, view.switchUser.getId().orElse(""));
  }

  @Test
  public void labels() {
    assertEquals(resources.message(HEADER), view.header.getText());
    HeaderRow headerRow = view.users.getHeaderRows().get(0);
    FooterRow footerRow = view.users.getFooterRows().get(0);
    assertEquals(userResources.message(EMAIL), headerRow.getCell(view.email).getText());
    assertEquals(userResources.message(EMAIL), footerRow.getCell(view.email).getText());
    assertEquals(userResources.message(NAME), headerRow.getCell(view.name).getText());
    assertEquals(userResources.message(NAME), footerRow.getCell(view.name).getText());
    assertEquals(userResources.message(ACTIVE), headerRow.getCell(view.active).getText());
    assertEquals(userResources.message(ACTIVE), footerRow.getCell(view.active).getText());
    assertEquals(webResources.message(EDIT), headerRow.getCell(view.edit).getText());
    assertEquals(webResources.message(EDIT), footerRow.getCell(view.edit).getText());
    assertEquals(webResources.message(ALL), view.emailFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL),
        view.activeFilter.getItemLabelGenerator().apply(Optional.empty()));
    assertEquals(userResources.message(property(ACTIVE, false)),
        view.activeFilter.getItemLabelGenerator().apply(Optional.of(false)));
    assertEquals(userResources.message(property(ACTIVE, true)),
        view.activeFilter.getItemLabelGenerator().apply(Optional.of(true)));
    assertEquals(webResources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
    assertEquals(resources.message(SWITCH_USER), view.switchUser.getText());
    validateIcon(VaadinIcon.BUG.create(), view.switchUser.getIcon());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(UsersView.class, locale);
    final AppResources userResources = new AppResources(User.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    UI.getCurrent().setLocale(locale);
    assertEquals(resources.message(HEADER), view.header.getText());
    HeaderRow headerRow = view.users.getHeaderRows().get(0);
    FooterRow footerRow = view.users.getFooterRows().get(0);
    assertEquals(userResources.message(EMAIL), headerRow.getCell(view.email).getText());
    assertEquals(userResources.message(EMAIL), footerRow.getCell(view.email).getText());
    assertEquals(userResources.message(NAME), headerRow.getCell(view.name).getText());
    assertEquals(userResources.message(NAME), footerRow.getCell(view.name).getText());
    assertEquals(userResources.message(ACTIVE), headerRow.getCell(view.active).getText());
    assertEquals(userResources.message(ACTIVE), footerRow.getCell(view.active).getText());
    assertEquals(webResources.message(EDIT), headerRow.getCell(view.edit).getText());
    assertEquals(webResources.message(EDIT), footerRow.getCell(view.edit).getText());
    assertEquals(webResources.message(ALL), view.emailFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL),
        view.activeFilter.getItemLabelGenerator().apply(Optional.empty()));
    assertEquals(userResources.message(property(ACTIVE, false)),
        view.activeFilter.getItemLabelGenerator().apply(Optional.of(false)));
    assertEquals(userResources.message(property(ACTIVE, true)),
        view.activeFilter.getItemLabelGenerator().apply(Optional.of(true)));
    assertEquals(webResources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
    assertEquals(resources.message(SWITCH_USER), view.switchUser.getText());
    validateIcon(VaadinIcon.BUG.create(), view.switchUser.getIcon());
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void users_Manager() {
    verify(service).all();
    List<User> users = items(view.users);
    assertEquals(this.users.size(), users.size());
    for (User user : this.users) {
      assertTrue(users.contains(user), () -> user.toString());
    }
    assertTrue(view.active.isVisible());
    assertTrue(view.add.isVisible());
    assertFalse(view.switchUser.isVisible());
  }

  @Test
  public void users_Admin() {
    verify(service).all();
    List<User> users = items(view.users);
    assertEquals(this.users.size(), users.size());
    for (User user : this.users) {
      assertTrue(users.contains(user), () -> user.toString());
    }
    assertTrue(view.active.isVisible());
    assertTrue(view.add.isVisible());
    assertTrue(view.switchUser.isVisible());
  }

  @Test
  public void users_SelectionMode() {
    assertTrue(view.users.getSelectionModel() instanceof SelectionModel.Single);
  }

  @Test
  public void users_Columns() {
    assertEquals(4, view.users.getColumns().size());
    assertNotNull(view.users.getColumnByKey(EMAIL));
    assertTrue(view.users.getColumnByKey(EMAIL).isSortable());
    assertNotNull(view.users.getColumnByKey(NAME));
    assertTrue(view.users.getColumnByKey(NAME).isSortable());
    assertNotNull(view.users.getColumnByKey(ACTIVE));
    assertTrue(view.users.getColumnByKey(ACTIVE).isSortable());
    assertNotNull(view.users.getColumnByKey(EDIT));
    assertFalse(view.users.getColumnByKey(EDIT).isSortable());
  }

  @Test
  public void users_ColumnsValueProvider() {
    when(service.get(any())).then(i -> repository.findById(i.getArgument(0)));
    for (int i = 0; i < users.size(); i++) {
      User user = users.get(i);
      assertEquals(user.getEmail() != null ? user.getEmail() : "",
          test(view.users).getCellText(i, view.users.getColumns().indexOf(view.email)));
      assertEquals(user.getName() != null ? user.getName() : "",
          test(view.users).getCellText(i, view.users.getColumns().indexOf(view.name)));
      Button activeButton = (Button) test(view.users).getCellComponent(i, view.active.getKey());
      assertTrue(activeButton.hasClassName(ACTIVE));
      assertTrue(
          activeButton.hasThemeName(user.isActive() ? ButtonVariant.LUMO_SUCCESS.getVariantName()
              : ButtonVariant.LUMO_ERROR.getVariantName()));
      assertEquals(userResources.message(property(ACTIVE, user.isActive())),
          activeButton.getText());
      validateIcon(user.isActive() ? VaadinIcon.EYE.create() : VaadinIcon.EYE_SLASH.create(),
          activeButton.getIcon());
      boolean previousActive = user.isActive();
      clickButton(activeButton);
      verify(service, atLeastOnce()).save(userCaptor.capture(), eq(null));
      assertEquals(!previousActive, userCaptor.getValue().isActive());
      assertTrue(
          activeButton.hasThemeName(user.isActive() ? ButtonVariant.LUMO_SUCCESS.getVariantName()
              : ButtonVariant.LUMO_ERROR.getVariantName()));
      assertEquals(userResources.message(property(ACTIVE, user.isActive())),
          activeButton.getText());
      validateIcon(user.isActive() ? VaadinIcon.EYE.create() : VaadinIcon.EYE_SLASH.create(),
          activeButton.getIcon());
      Button editButton = (Button) test(view.users).getCellComponent(i, view.edit.getKey());
      assertTrue(editButton.hasClassName(EDIT));
      assertTrue(editButton.hasThemeName(ButtonVariant.LUMO_ICON.getVariantName()));
      validateIcon(VaadinIcon.EDIT.create(), editButton.getIcon());
      clickButton(editButton);
      assertEquals(1, $(UserDialog.class).all().size());
      UserDialog dialog = $(UserDialog.class).first();
      assertEquals(user, dialog.getUser());
      dialog.close();
    }
  }

  @Test
  public void users_EmailColumnComparator() {
    Comparator<User> comparator = view.email.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(email("éê"), email("ee")));
    assertTrue(comparator.compare(email("a"), email("e")) < 0);
    assertTrue(comparator.compare(email("a"), email("é")) < 0);
    assertTrue(comparator.compare(email("e"), email("a")) > 0);
    assertTrue(comparator.compare(email("é"), email("a")) > 0);
  }

  @Test
  public void users_NameColumnComparator() {
    Comparator<User> comparator = view.name.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(name("éê"), name("ee")));
    assertTrue(comparator.compare(name("a"), name("e")) < 0);
    assertTrue(comparator.compare(name("a"), name("é")) < 0);
    assertTrue(comparator.compare(name("e"), name("a")) > 0);
    assertTrue(comparator.compare(name("é"), name("a")) > 0);
  }

  @Test
  public void users_ActiveColumnComparator() {
    Comparator<User> comparator = view.active.getComparator(SortDirection.ASCENDING);
    assertTrue(comparator.compare(active(false), active(true)) < 0);
    assertTrue(comparator.compare(active(false), active(false)) == 0);
    assertTrue(comparator.compare(active(true), active(true)) == 0);
    assertTrue(comparator.compare(active(true), active(false)) > 0);
  }

  @Test
  public void view() {
    User user = users.get(0);
    when(service.get(any())).thenReturn(Optional.of(user));

    doubleClickItem(view.users, user);

    verify(service).get(user.getId());
    UserDialog dialog = $(UserDialog.class).first();
    assertEquals(user, dialog.getUser());
    assertTrue(dialog.isOpened());
  }

  @Test
  public void refreshDatasetsOnUserSaved() {
    User user = mock(User.class);
    when(service.get(any())).thenReturn(Optional.of(user));
    view.view(user);
    UserDialog dialog = $(UserDialog.class).first();
    dialog.fireSavedEvent();
    verify(service, times(2)).all();
  }

  @Test
  public void toggleActive_Active() {
    User user = repository.findById(3L).orElse(null);
    view.toggleActive(user);
    verify(service).save(user, null);
    assertFalse(user.isActive());
  }

  @Test
  public void toggleActive_Inactive() {
    User user = repository.findById(7L).orElse(null);
    view.toggleActive(user);
    verify(service).save(user, null);
    assertTrue(user.isActive());
  }

  @Test
  public void emailFilter() {
    assertEquals("", view.emailFilter.getValue());
    assertEquals(ValueChangeMode.EAGER, view.emailFilter.getValueChangeMode());
  }

  @Test
  public void filterEmail() {
    view.users.setItems(mock(DataProvider.class));

    view.emailFilter.setValue("test");

    assertEquals("test", view.filter().emailContains);
    verify(view.users.getDataProvider()).refreshAll();
  }

  @Test
  public void filterEmail_Empty() {
    view.users.setItems(mock(DataProvider.class));

    view.emailFilter.setValue("test");
    view.emailFilter.setValue("");

    assertNull(view.filter().emailContains);
    verify(view.users.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void nameFilter() {
    assertEquals("", view.nameFilter.getValue());
    assertEquals(ValueChangeMode.EAGER, view.nameFilter.getValueChangeMode());
  }

  @Test
  public void filterName() {
    view.users.setItems(mock(DataProvider.class));

    view.nameFilter.setValue("test");

    assertEquals("test", view.filter().nameContains);
    verify(view.users.getDataProvider()).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    view.users.setItems(mock(DataProvider.class));

    view.nameFilter.setValue("test");
    view.nameFilter.setValue("");

    assertNull(view.filter().nameContains);
    verify(view.users.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void activeFilter() {
    view.onAttach(mock(AttachEvent.class));
    assertEquals(Optional.empty(), view.activeFilter.getValue());
    List<Optional<Boolean>> values = items(view.activeFilter);
    assertEquals(3, values.size());
    assertTrue(values.contains(Optional.empty()));
    assertTrue(values.contains(Optional.of(false)));
    assertTrue(values.contains(Optional.of(true)));
  }

  @Test
  public void filterActive_False() {
    view.users.setItems(mock(DataProvider.class));

    view.activeFilter.setValue(Optional.of(false));

    assertEquals(false, view.filter().active);
    verify(view.users.getDataProvider()).refreshAll();
  }

  @Test
  public void filterActive_True() {
    view.users.setItems(mock(DataProvider.class));

    view.activeFilter.setValue(Optional.of(true));

    assertEquals(true, view.filter().active);
    verify(view.users.getDataProvider()).refreshAll();
  }

  @Test
  public void filterActive_Empty() {
    view.users.setItems(mock(DataProvider.class));

    view.activeFilter.setValue(Optional.of(false));
    view.activeFilter.setValue(Optional.empty());

    assertNull(view.filter().active);
    verify(view.users.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void error() {
    assertFalse(view.error.isVisible());
  }

  @Test
  public void add() {
    clickButton(view.add);

    assertEquals(1, $(UserDialog.class).all().size());
    UserDialog dialog = $(UserDialog.class).first();
    assertNull(dialog.getUser().getId());
  }

  @Test
  public void switchUser() throws Throwable {
    User user = repository.findById(3L).orElse(null);
    view.users.select(user);
    view.switchUser();
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
    view.switchUser();
    assertEquals(resources.message(USERS_REQUIRED), view.error.getText());
    assertTrue(view.error.isVisible());
    verify(switchUserService, never()).switchUser(any(), any());
    assertTrue($(UsersView.class).exists());
  }

  @Test
  public void permissions_ErrorThenView() {
    view.switchUser();
    view.view(users.get(1));
    assertFalse(view.error.isVisible());
  }
}
