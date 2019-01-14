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

import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.UserProperties.NAME;
import static ca.qc.ircm.lana.user.web.UsersView.ADD;
import static ca.qc.ircm.lana.user.web.UsersView.HEADER;
import static ca.qc.ircm.lana.user.web.UsersView.USERS;
import static ca.qc.ircm.lana.user.web.UsersView.VIEW_NAME;
import static ca.qc.ircm.lana.web.WebConstants.ALL;
import static ca.qc.ircm.lana.web.WebConstants.APPLICATION_NAME;
import static ca.qc.ircm.lana.web.WebConstants.TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
public class UsersViewTest extends AbstractViewTestCase {
  private UsersView view;
  @Mock
  private UsersViewPresenter presenter;
  @Mock
  private UserDialog userDialog;
  @Mock
  private LaboratoryDialog laboratoryDialog;
  @Captor
  private ArgumentCaptor<ValueProvider<User, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Button, User>> buttonRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<User>> comparatorCaptor;
  @Inject
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources = new MessageResource(UsersView.class, locale);
  private MessageResource userResources = new MessageResource(User.class, locale);
  private MessageResource webResources = new MessageResource(WebConstants.class, locale);
  private List<User> users;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new UsersView(presenter, userDialog, laboratoryDialog);
    view.init();
    users = userRepository.findAll();
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    Element usersElement = view.users.getElement();
    view.users = mock(Grid.class);
    when(view.users.getElement()).thenReturn(usersElement);
    view.email = mock(Column.class);
    when(view.users.addColumn(any(ComponentRenderer.class), eq(EMAIL))).thenReturn(view.email);
    when(view.email.setKey(any())).thenReturn(view.email);
    when(view.email.setComparator(any(Comparator.class))).thenReturn(view.email);
    when(view.email.setHeader(any(String.class))).thenReturn(view.email);
    view.name = mock(Column.class);
    when(view.users.addColumn(any(ValueProvider.class), eq(NAME))).thenReturn(view.name);
    when(view.name.setKey(any())).thenReturn(view.name);
    when(view.name.setHeader(any(String.class))).thenReturn(view.name);
    view.laboratory = mock(Column.class);
    when(view.users.addColumn(any(ComponentRenderer.class), eq(LABORATORY)))
        .thenReturn(view.laboratory);
    when(view.laboratory.setKey(any())).thenReturn(view.laboratory);
    when(view.laboratory.setComparator(any(Comparator.class))).thenReturn(view.laboratory);
    when(view.laboratory.setHeader(any(String.class))).thenReturn(view.laboratory);
    HeaderRow filtersRow = mock(HeaderRow.class);
    when(view.users.appendHeaderRow()).thenReturn(filtersRow);
    HeaderCell emailFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.email)).thenReturn(emailFilterCell);
    HeaderCell nameFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.name)).thenReturn(nameFilterCell);
    HeaderCell laboratoryFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.laboratory)).thenReturn(laboratoryFilterCell);
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(view);
  }

  @Test
  public void styles() {
    assertTrue(view.getContent().getId().orElse("").equals(VIEW_NAME));
    assertTrue(view.header.getClassNames().contains(HEADER));
    assertTrue(view.users.getClassNames().contains(USERS));
    assertTrue(view.add.getClassNames().contains(ADD));
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
    verify(view.laboratory).setHeader(userResources.message(LABORATORY));
    verify(view.laboratory).setFooter(userResources.message(LABORATORY));
    assertEquals(webResources.message(ALL), view.emailFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.laboratoryFilter.getPlaceholder());
    assertEquals(resources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add);
  }

  @Test
  public void localeChange() {
    view = new UsersView(presenter, userDialog, laboratoryDialog);
    mockColumns();
    view.init();
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final MessageResource resources = new MessageResource(UsersView.class, locale);
    final MessageResource userResources = new MessageResource(User.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.email).setHeader(userResources.message(EMAIL));
    verify(view.email).setFooter(userResources.message(EMAIL));
    verify(view.name).setHeader(userResources.message(NAME));
    verify(view.name).setFooter(userResources.message(NAME));
    verify(view.laboratory).setHeader(userResources.message(LABORATORY));
    verify(view.laboratory).setFooter(userResources.message(LABORATORY));
    assertEquals(webResources.message(ALL), view.emailFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.laboratoryFilter.getPlaceholder());
    assertEquals(resources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add);
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void users_SelectionMode() {
    view = new UsersView(presenter, userDialog, laboratoryDialog);
    mockColumns();
    view.init();
    verify(view.users).setSelectionMode(SelectionMode.MULTI);
  }

  @Test
  public void users_Columns() {
    assertEquals(3, view.users.getColumns().size());
    assertNotNull(view.users.getColumnByKey(EMAIL));
    assertNotNull(view.users.getColumnByKey(NAME));
    assertNotNull(view.users.getColumnByKey(LABORATORY));
  }

  @Test
  public void users_ColumnsValueProvider() {
    view = new UsersView(presenter, userDialog, laboratoryDialog);
    mockColumns();
    view.init();
    verify(view.users).addColumn(buttonRendererCaptor.capture(), eq(EMAIL));
    ComponentRenderer<Button, User> buttonRenderer = buttonRendererCaptor.getValue();
    for (User user : users) {
      Button button = buttonRenderer.createComponent(user);
      assertTrue(button.getClassNames().contains(EMAIL));
      assertEquals(user.getEmail(), button.getText());
      clickButton(button);
      verify(presenter).view(user);
    }
    verify(view.email).setComparator(comparatorCaptor.capture());
    Comparator<User> comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(email("abc@site.com"), email("test@site.com")) < 0);
    assertTrue(comparator.compare(email("Abc@site.com"), email("test@site.com")) < 0);
    assertTrue(comparator.compare(email("test@abc.com"), email("test@site.com")) < 0);
    assertTrue(comparator.compare(email("test@site.com"), email("test@site.com")) == 0);
    assertTrue(comparator.compare(email("Test@site.com"), email("test@site.com")) == 0);
    assertTrue(comparator.compare(email("test@site.com"), email("abc@site.com")) > 0);
    assertTrue(comparator.compare(email("Test@site.com"), email("abc@site.com")) > 0);
    assertTrue(comparator.compare(email("test@site.com"), email("test@abc.com")) > 0);
    verify(view.users).addColumn(valueProviderCaptor.capture(), eq(NAME));
    ValueProvider<User, String> valueProvider = valueProviderCaptor.getValue();
    for (User user : users) {
      assertEquals(user.getName() != null ? user.getName() : "", valueProvider.apply(user));
    }
    verify(view.users).addColumn(buttonRendererCaptor.capture(), eq(LABORATORY));
    buttonRenderer = buttonRendererCaptor.getValue();
    for (User user : users) {
      Button button = buttonRenderer.createComponent(user);
      assertTrue(button.getClassNames().contains(LABORATORY));
      assertEquals(user.getLaboratory().getName(), button.getText());
      clickButton(button);
      verify(presenter, atLeastOnce()).viewLaboratory(user.getLaboratory());
    }
    verify(view.laboratory).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(lab("abc"), lab("test")) < 0);
    assertTrue(comparator.compare(lab("Abc"), lab("test")) < 0);
    assertTrue(comparator.compare(lab("élement"), lab("facteur")) < 0);
    assertTrue(comparator.compare(lab("test"), lab("test")) == 0);
    assertTrue(comparator.compare(lab("Test"), lab("test")) == 0);
    assertTrue(comparator.compare(lab("Expérienceà"), lab("experiencea")) == 0);
    assertTrue(comparator.compare(lab("experiencea"), lab("Expérienceà")) == 0);
    assertTrue(comparator.compare(lab("test"), lab("abc")) > 0);
    assertTrue(comparator.compare(lab("Test"), lab("abc")) > 0);
    assertTrue(comparator.compare(lab("facteur"), lab("élement")) > 0);
  }

  private User email(String email) {
    User user = new User();
    user.setEmail(email);
    return user;
  }

  private User lab(String name) {
    User user = new User();
    Laboratory laboratory = new Laboratory();
    laboratory.setName(name);
    user.setLaboratory(laboratory);
    return user;
  }

  @Test
  public void filterEmail() {
    view.emailFilter.setValue("test");

    verify(presenter).filterEmail("test");
  }

  @Test
  public void filterName() {
    view.nameFilter.setValue("test");

    verify(presenter).filterName("test");
  }

  @Test
  public void filterLaboratory() {
    view.laboratoryFilter.setValue("test");

    verify(presenter).filterLaboratory("test");
  }
}
