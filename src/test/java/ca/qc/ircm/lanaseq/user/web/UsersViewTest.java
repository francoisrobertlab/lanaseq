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
import static ca.qc.ircm.lanaseq.security.web.WebSecurityConfiguration.SWITCH_USERNAME_PARAMETER;
import static ca.qc.ircm.lanaseq.security.web.WebSecurityConfiguration.SWITCH_USER_URL;
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
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_FAILED;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_USER;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_USERNAME;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_USER_FORM;
import static ca.qc.ircm.lanaseq.user.web.UsersView.USERS;
import static ca.qc.ircm.lanaseq.user.web.UsersView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.Location;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
 * Tests for {@link UsersView}.
 */
@ServiceTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UsersViewTest extends AbstractKaribuTestCase {
  private UsersView view;
  @Mock
  private UsersViewPresenter presenter;
  @MockBean
  private ObjectFactory<UserDialog> dialogFactory;
  @Captor
  private ArgumentCaptor<ValueProvider<User, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Button, User>> buttonRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<User>> comparatorCaptor;
  @Autowired
  private UserRepository userRepository;
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
    ui.setLocale(locale);
    view = new UsersView(presenter, dialogFactory);
    view.init();
    users = userRepository.findAll();
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    Element usersElement = view.users.getElement();
    view.users = mock(Grid.class);
    when(view.users.getElement()).thenReturn(usersElement);
    view.email = mock(Column.class);
    when(view.users.addColumn(any(ValueProvider.class), eq(EMAIL))).thenReturn(view.email);
    when(view.email.setKey(any())).thenReturn(view.email);
    when(view.email.setComparator(any(Comparator.class))).thenReturn(view.email);
    when(view.email.setHeader(any(String.class))).thenReturn(view.email);
    when(view.email.setSortable(anyBoolean())).thenReturn(view.email);
    when(view.email.setFlexGrow(anyInt())).thenReturn(view.email);
    view.name = mock(Column.class);
    when(view.users.addColumn(any(ValueProvider.class), eq(NAME))).thenReturn(view.name);
    when(view.name.setKey(any())).thenReturn(view.name);
    when(view.name.setComparator(any(Comparator.class))).thenReturn(view.name);
    when(view.name.setHeader(any(String.class))).thenReturn(view.name);
    when(view.name.setSortable(anyBoolean())).thenReturn(view.name);
    when(view.name.setFlexGrow(anyInt())).thenReturn(view.name);
    view.active = mock(Column.class);
    when(view.users.addColumn(any(ComponentRenderer.class), eq(ACTIVE))).thenReturn(view.active);
    when(view.active.setKey(any())).thenReturn(view.active);
    when(view.active.setComparator(any(Comparator.class))).thenReturn(view.active);
    when(view.active.setHeader(any(String.class))).thenReturn(view.active);
    when(view.active.setSortable(anyBoolean())).thenReturn(view.active);
    when(view.active.setFlexGrow(anyInt())).thenReturn(view.active);
    view.edit = mock(Column.class);
    when(view.users.addColumn(any(ComponentRenderer.class), eq(EDIT))).thenReturn(view.edit);
    when(view.edit.setKey(any())).thenReturn(view.edit);
    when(view.edit.setComparator(any(Comparator.class))).thenReturn(view.edit);
    when(view.edit.setHeader(any(String.class))).thenReturn(view.edit);
    when(view.edit.setSortable(anyBoolean())).thenReturn(view.edit);
    when(view.edit.setFlexGrow(anyInt())).thenReturn(view.edit);
    HeaderRow filtersRow = mock(HeaderRow.class);
    when(view.users.appendHeaderRow()).thenReturn(filtersRow);
    HeaderCell emailFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.email)).thenReturn(emailFilterCell);
    HeaderCell nameFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.name)).thenReturn(nameFilterCell);
    HeaderCell activeFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.active)).thenReturn(activeFilterCell);
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(view);
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(HEADER, view.header.getId().orElse(""));
    assertEquals(USERS, view.users.getId().orElse(""));
    assertEquals(ERROR_TEXT, view.error.getId().orElse(""));
    assertEquals(ADD, view.add.getId().orElse(""));
    assertEquals(SWITCH_USER, view.switchUser.getId().orElse(""));
    assertEquals(SWITCH_USER_FORM, view.switchUserForm.getId().orElse(""));
    assertEquals(SWITCH_USER_URL, view.switchUserForm.getElement().getAttribute("action"));
    assertEquals("post", view.switchUserForm.getElement().getAttribute("method"));
    assertEquals("none", view.switchUserForm.getElement().getStyle().get("display"));
    assertEquals(1, view.switchUserForm.getElement().getChildCount());
    assertEquals(view.switchUsername,
        view.switchUserForm.getElement().getChild(0).getComponent().get());
    assertEquals(SWITCH_USERNAME, view.switchUsername.getId().orElse(""));
    assertEquals(SWITCH_USERNAME_PARAMETER, view.switchUsername.getElement().getAttribute("name"));
  }

  @Test
  public void labels() {
    mockColumns();
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.email).setHeader(userResources.message(EMAIL));
    verify(view.email).setFooter(userResources.message(EMAIL));
    verify(view.name).setHeader(userResources.message(NAME));
    verify(view.name).setFooter(userResources.message(NAME));
    verify(view.active).setHeader(userResources.message(ACTIVE));
    verify(view.active).setFooter(userResources.message(ACTIVE));
    verify(view.edit).setHeader(webResources.message(EDIT));
    verify(view.edit).setFooter(webResources.message(EDIT));
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
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    mockColumns();
    view.init();
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(UsersView.class, locale);
    final AppResources userResources = new AppResources(User.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.email).setHeader(userResources.message(EMAIL));
    verify(view.email).setFooter(userResources.message(EMAIL));
    verify(view.name).setHeader(userResources.message(NAME));
    verify(view.name).setFooter(userResources.message(NAME));
    verify(view.active).setHeader(userResources.message(ACTIVE));
    verify(view.active).setFooter(userResources.message(ACTIVE));
    verify(view.edit).setHeader(webResources.message(EDIT));
    verify(view.edit).setFooter(webResources.message(EDIT));
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
    verify(presenter).localeChange(locale);
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
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
    doAnswer(i -> {
      User user = i.getArgument(0);
      user.setActive(!user.isActive());
      return null;
    }).when(presenter).toggleActive(any());
    mockColumns();
    view.init();
    verify(view.users).addColumn(valueProviderCaptor.capture(), eq(EMAIL));
    ValueProvider<User, String> valueProvider = valueProviderCaptor.getValue();
    for (User user : users) {
      assertEquals(user.getEmail() != null ? user.getEmail() : "", valueProvider.apply(user));
    }
    verify(view.email).setComparator(comparatorCaptor.capture());
    Comparator<User> comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (User user : users) {
      assertEquals(user.getEmail(),
          ((NormalizedComparator<User>) comparator).getConverter().apply(user));
    }
    verify(view.users).addColumn(valueProviderCaptor.capture(), eq(NAME));
    valueProvider = valueProviderCaptor.getValue();
    for (User user : users) {
      assertEquals(user.getName() != null ? user.getName() : "", valueProvider.apply(user));
    }
    verify(view.name).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (User user : users) {
      assertEquals(user.getName(),
          ((NormalizedComparator<User>) comparator).getConverter().apply(user));
    }
    verify(view.users).addColumn(buttonRendererCaptor.capture(), eq(ACTIVE));
    ComponentRenderer<Button, User> buttonRenderer = buttonRendererCaptor.getValue();
    for (User user : users) {
      Button button = buttonRenderer.createComponent(user);
      assertTrue(button.hasClassName(ACTIVE));
      assertTrue(button.hasThemeName(user.isActive() ? ButtonVariant.LUMO_SUCCESS.getVariantName()
          : ButtonVariant.LUMO_ERROR.getVariantName()));
      assertEquals(userResources.message(property(ACTIVE, user.isActive())), button.getText());
      validateIcon(user.isActive() ? VaadinIcon.EYE.create() : VaadinIcon.EYE_SLASH.create(),
          button.getIcon());
      boolean previousActive = user.isActive();
      clickButton(button);
      verify(presenter, atLeastOnce()).toggleActive(user);
      assertEquals(!previousActive, user.isActive());
      assertTrue(button.hasThemeName(user.isActive() ? ButtonVariant.LUMO_SUCCESS.getVariantName()
          : ButtonVariant.LUMO_ERROR.getVariantName()));
      assertEquals(userResources.message(property(ACTIVE, user.isActive())), button.getText());
      validateIcon(user.isActive() ? VaadinIcon.EYE.create() : VaadinIcon.EYE_SLASH.create(),
          button.getIcon());
    }
    verify(view.active).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(active(false), active(true)) < 0);
    assertTrue(comparator.compare(active(false), active(false)) == 0);
    assertTrue(comparator.compare(active(true), active(true)) == 0);
    assertTrue(comparator.compare(active(true), active(false)) > 0);
    verify(view.users).addColumn(buttonRendererCaptor.capture(), eq(EDIT));
    buttonRenderer = buttonRendererCaptor.getValue();
    for (User user : users) {
      Button button = buttonRenderer.createComponent(user);
      assertTrue(button.hasClassName(EDIT));
      assertTrue(button.hasThemeName(ButtonVariant.LUMO_ICON.getVariantName()));
      validateIcon(VaadinIcon.EDIT.create(), button.getIcon());
      clickButton(button);
      verify(presenter).view(user);
    }
  }

  @Test
  public void view() {
    User user = users.get(0);
    doubleClickItem(view.users, user);

    verify(presenter).view(user);
  }

  private User active(boolean active) {
    User user = new User();
    user.setActive(active);
    return user;
  }

  @Test
  public void emailFilter() {
    assertEquals("", view.emailFilter.getValue());
    assertEquals(ValueChangeMode.EAGER, view.emailFilter.getValueChangeMode());
  }

  @Test
  public void filterEmail() {
    view.emailFilter.setValue("test");

    verify(presenter).filterEmail("test");
  }

  @Test
  public void nameFilter() {
    assertEquals("", view.nameFilter.getValue());
    assertEquals(ValueChangeMode.EAGER, view.nameFilter.getValueChangeMode());
  }

  @Test
  public void filterName() {
    view.nameFilter.setValue("test");

    verify(presenter).filterName("test");
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
    view.activeFilter.setValue(Optional.of(false));

    verify(presenter).filterActive(false);
  }

  @Test
  public void filterActive_True() {
    view.activeFilter.setValue(Optional.of(true));

    verify(presenter).filterActive(true);
  }

  @Test
  public void add() {
    clickButton(view.add);
    verify(presenter).add();
  }

  @Test
  public void switchUser() {
    clickButton(view.switchUser);
    verify(presenter).switchUser();
  }

  @Test
  public void afterNavigation() {
    AfterNavigationEvent event = mock(AfterNavigationEvent.class);
    Location location = new Location(VIEW_NAME + "?" + SWITCH_FAILED);
    when(event.getLocation()).thenReturn(location);

    view.afterNavigation(event);

    verify(presenter).showError(location.getQueryParameters().getParameters());
  }
}
