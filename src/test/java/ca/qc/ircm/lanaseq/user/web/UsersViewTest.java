package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.user.UserProperties.ACTIVE;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.SwitchUserService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.ErrorNotification;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.ListDataProvider;
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
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link UsersView}.
 */
@ServiceTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UsersViewTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(UsersView.class);
  private static final String USER_PREFIX = messagePrefix(User.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private UsersView view;
  @MockitoBean
  private UserService service;
  @MockitoBean
  private SwitchUserService switchUserService;
  @Captor
  private ArgumentCaptor<User> userCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<UserDialog>>> userSavedListenerCaptor;
  @Autowired
  private UserRepository repository;
  @Mock
  private ListDataProvider<User> userDataProvider;
  private Locale locale = Locale.ENGLISH;
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
    assertEquals(USERS, view.users.getId().orElse(""));
    assertEquals(ADD, view.add.getId().orElse(""));
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
    assertEquals(EDIT, view.edit.getId().orElse(""));
    validateIcon(VaadinIcon.EDIT.create(), view.edit.getIcon());
    assertEquals(SWITCH_USER, view.switchUser.getId().orElse(""));
    validateIcon(VaadinIcon.BUG.create(), view.switchUser.getIcon());
  }

  @Test
  public void labels() {
    HeaderRow headerRow = view.users.getHeaderRows().get(0);
    FooterRow footerRow = view.users.getFooterRows().get(0);
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), headerRow.getCell(view.email).getText());
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), footerRow.getCell(view.email).getText());
    assertEquals(view.getTranslation(USER_PREFIX + NAME), headerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(USER_PREFIX + NAME), footerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(USER_PREFIX + ACTIVE),
        headerRow.getCell(view.active).getText());
    assertEquals(view.getTranslation(USER_PREFIX + ACTIVE),
        footerRow.getCell(view.active).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.emailFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.nameFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL),
        view.activeFilter.getItemLabelGenerator().apply(Optional.empty()));
    assertEquals(view.getTranslation(USER_PREFIX + property(ACTIVE, false)),
        view.activeFilter.getItemLabelGenerator().apply(Optional.of(false)));
    assertEquals(view.getTranslation(USER_PREFIX + property(ACTIVE, true)),
        view.activeFilter.getItemLabelGenerator().apply(Optional.of(true)));
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ADD), view.add.getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + EDIT), view.edit.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SWITCH_USER), view.switchUser.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    HeaderRow headerRow = view.users.getHeaderRows().get(0);
    FooterRow footerRow = view.users.getFooterRows().get(0);
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), headerRow.getCell(view.email).getText());
    assertEquals(view.getTranslation(USER_PREFIX + EMAIL), footerRow.getCell(view.email).getText());
    assertEquals(view.getTranslation(USER_PREFIX + NAME), headerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(USER_PREFIX + NAME), footerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(USER_PREFIX + ACTIVE),
        headerRow.getCell(view.active).getText());
    assertEquals(view.getTranslation(USER_PREFIX + ACTIVE),
        footerRow.getCell(view.active).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.emailFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.nameFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL),
        view.activeFilter.getItemLabelGenerator().apply(Optional.empty()));
    assertEquals(view.getTranslation(USER_PREFIX + property(ACTIVE, false)),
        view.activeFilter.getItemLabelGenerator().apply(Optional.of(false)));
    assertEquals(view.getTranslation(USER_PREFIX + property(ACTIVE, true)),
        view.activeFilter.getItemLabelGenerator().apply(Optional.of(true)));
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ADD), view.add.getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + EDIT), view.edit.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SWITCH_USER), view.switchUser.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TITLE,
        view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME)), view.getPageTitle());
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void users_Manager() {
    verify(service).all();
    List<User> users = items(view.users);
    assertEquals(this.users.size(), users.size());
    for (User user : this.users) {
      assertTrue(users.contains(user), user::toString);
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
      assertTrue(users.contains(user), user::toString);
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
    assertEquals(3, view.users.getColumns().size());
    assertNotNull(view.users.getColumnByKey(EMAIL));
    assertTrue(view.users.getColumnByKey(EMAIL).isSortable());
    assertNotNull(view.users.getColumnByKey(NAME));
    assertTrue(view.users.getColumnByKey(NAME).isSortable());
    assertNotNull(view.users.getColumnByKey(ACTIVE));
    assertTrue(view.users.getColumnByKey(ACTIVE).isSortable());
  }

  @Test
  public void users_ColumnsValueProvider() {
    when(service.get(anyLong())).then(i -> repository.findById(i.getArgument(0)));
    for (int i = 0; i < users.size(); i++) {
      User user = users.get(i);
      assertEquals(user.getEmail(),
          test(view.users).getCellText(i, view.users.getColumns().indexOf(view.email)));
      assertEquals(user.getName(),
          test(view.users).getCellText(i, view.users.getColumns().indexOf(view.name)));
      Button activeButton = (Button) test(view.users).getCellComponent(i, view.active.getKey());
      assertTrue(activeButton.hasClassName(ACTIVE));
      assertTrue(
          activeButton.hasThemeName(user.isActive() ? ButtonVariant.LUMO_SUCCESS.getVariantName()
              : ButtonVariant.LUMO_ERROR.getVariantName()));
      assertEquals(view.getTranslation(USER_PREFIX + property(ACTIVE, user.isActive())),
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
      assertEquals(view.getTranslation(USER_PREFIX + property(ACTIVE, user.isActive())),
          activeButton.getText());
      validateIcon(user.isActive() ? VaadinIcon.EYE.create() : VaadinIcon.EYE_SLASH.create(),
          activeButton.getIcon());
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
    when(service.get(anyLong())).thenReturn(Optional.of(user));

    doubleClickItem(view.users, user);

    verify(service).get(user.getId());
    UserDialog dialog = $(UserDialog.class).first();
    assertEquals(user.getId(), dialog.getUserId());
    assertTrue(dialog.isOpened());
  }

  @Test
  public void refreshDatasetsOnUserSaved() {
    User user = mock(User.class);
    when(service.get(anyLong())).thenReturn(Optional.of(user));
    view.edit(user);
    UserDialog dialog = $(UserDialog.class).first();
    dialog.fireSavedEvent();
    verify(service, times(2)).all();
  }

  @Test
  public void toggleActive_Active() {
    User user = repository.findById(3L).orElseThrow();
    view.toggleActive(user);
    verify(service).save(user, null);
    assertFalse(user.isActive());
  }

  @Test
  public void toggleActive_Inactive() {
    User user = repository.findById(7L).orElseThrow();
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
    view.users.setItems(userDataProvider);

    view.emailFilter.setValue("test");

    assertEquals("test", view.filter().emailContains);
    verify(view.users.getDataProvider()).refreshAll();
  }

  @Test
  public void filterEmail_value() {
    assertEquals(9, view.users.getListDataView().getItems().count());

    view.emailFilter.setValue("an");

    assertEquals(3, view.users.getListDataView().getItems().count());
  }

  @Test
  public void filterEmail_Empty() {
    view.users.setItems(userDataProvider);

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
    view.users.setItems(userDataProvider);

    view.nameFilter.setValue("test");

    assertEquals("test", view.filter().nameContains);
    verify(view.users.getDataProvider()).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    view.users.setItems(userDataProvider);

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
    assertTrue(values.contains(Optional.<Boolean>empty()));
    assertTrue(values.contains(Optional.of(false)));
    assertTrue(values.contains(Optional.of(true)));
  }

  @Test
  public void filterActive_False() {
    view.users.setItems(userDataProvider);

    view.activeFilter.setValue(Optional.of(false));

    assertEquals(false, view.filter().active);
    verify(view.users.getDataProvider()).refreshAll();
  }

  @Test
  public void filterActive_True() {
    view.users.setItems(userDataProvider);

    view.activeFilter.setValue(Optional.of(true));

    assertEquals(true, view.filter().active);
    verify(view.users.getDataProvider()).refreshAll();
  }

  @Test
  public void filterActive_Empty() {
    view.users.setItems(userDataProvider);

    view.activeFilter.setValue(Optional.of(false));
    view.activeFilter.setValue(Optional.empty());

    assertNull(view.filter().active);
    verify(view.users.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void add() {
    clickButton(view.add);

    assertEquals(1, $(UserDialog.class).all().size());
    UserDialog dialog = $(UserDialog.class).first();
    assertEquals(0, dialog.getUserId());
  }

  @Test
  public void edit_Enabled() {
    assertFalse(view.edit.isEnabled());
    User user = repository.findById(3L).orElseThrow();
    view.users.select(user);
    assertTrue(view.edit.isEnabled());
    view.users.deselectAll();
    assertFalse(view.edit.isEnabled());
  }

  @Test
  public void edit() {
    User user = repository.findById(3L).orElseThrow();
    when(service.get(anyLong())).thenReturn(Optional.of(user));
    view.users.select(user);
    test(view.edit).click();
    assertEquals(1, $(UserDialog.class).all().size());
    UserDialog dialog = $(UserDialog.class).first();
    assertEquals(user.getId(), dialog.getUserId());
  }

  @Test
  public void edit_EmptySelection() {
    view.edit();
    Notification error = $(Notification.class).first();
    assertTrue(error instanceof ErrorNotification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + USERS_REQUIRED),
        ((ErrorNotification) error).getText());
    verify(switchUserService, never()).switchUser(any(), any());
    assertTrue($(UsersView.class).exists());
  }

  @Test
  public void switchUser_Enabled() {
    assertFalse(view.switchUser.isEnabled());
    User user = repository.findById(3L).orElseThrow();
    view.users.select(user);
    assertTrue(view.switchUser.isEnabled());
    view.users.deselectAll();
    assertFalse(view.switchUser.isEnabled());
  }

  @Test
  public void switchUser() {
    User user = repository.findById(3L).orElseThrow();
    view.users.select(user);
    view.switchUser.click();
    verify(switchUserService).switchUser(user, VaadinServletRequest.getCurrent());
    assertTrue(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream()
        .anyMatch(i -> ("if ($1 == '_self') this.stopApplication(); window.open($0, $1)")
            .equals(i.getInvocation().getExpression())
            && "/".equals(i.getInvocation().getParameters().get(0))
            && "_self".equals(i.getInvocation().getParameters().get(1))));
  }

  @Test
  public void switchUser_EmptySelection() {
    view.switchUser();
    Notification error = $(Notification.class).first();
    assertTrue(error instanceof ErrorNotification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + USERS_REQUIRED),
        ((ErrorNotification) error).getText());
    verify(switchUserService, never()).switchUser(any(), any());
    assertTrue($(UsersView.class).exists());
  }
}
