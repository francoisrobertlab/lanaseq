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
import static ca.qc.ircm.lana.user.web.UsersView.ADD;
import static ca.qc.ircm.lana.user.web.UsersView.HEADER;
import static ca.qc.ircm.lana.user.web.UsersView.USERS;
import static ca.qc.ircm.lana.user.web.UsersView.VIEW;
import static ca.qc.ircm.lana.user.web.UsersView.VIEW_NAME;
import static ca.qc.ircm.lana.web.WebConstants.APPLICATION_NAME;
import static ca.qc.ircm.lana.web.WebConstants.TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
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
  @Captor
  private ArgumentCaptor<ValueProvider<User, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<ValueProvider<User, Button>> buttonProviderCaptor;
  @Inject
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources = new MessageResource(UsersView.class, locale);
  private MessageResource userResources = new MessageResource(User.class, locale);
  private MessageResource generalResources = new MessageResource(WebConstants.class, locale);
  private List<User> users;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new UsersView(presenter, userDialog);
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
    when(view.email.setHeader(any(String.class))).thenReturn(view.email);
    view.laboratory = mock(Column.class);
    when(view.users.addColumn(any(ValueProvider.class), eq(LABORATORY)))
        .thenReturn(view.laboratory);
    when(view.laboratory.setKey(any())).thenReturn(view.laboratory);
    when(view.laboratory.setHeader(any(String.class))).thenReturn(view.laboratory);
    view.view = mock(Column.class);
    when(view.users.addComponentColumn(any(ValueProvider.class))).thenReturn(view.view);
    when(view.view.setKey(any())).thenReturn(view.view);
    when(view.view.setHeader(any(String.class))).thenReturn(view.view);
  }

  @Test
  public void presenter_Init() {
    view.onAttach(mock(AttachEvent.class));
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
    verify(view.laboratory).setHeader(userResources.message(LABORATORY));
    verify(view.laboratory).setFooter(userResources.message(LABORATORY));
    verify(view.view).setHeader(resources.message(VIEW));
    verify(view.view).setFooter(resources.message(VIEW));
    assertEquals(resources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add);
  }

  @Test
  public void localeChange() {
    view = new UsersView(presenter, userDialog);
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
    verify(view.laboratory).setHeader(userResources.message(LABORATORY));
    verify(view.laboratory).setFooter(userResources.message(LABORATORY));
    verify(view.view).setHeader(resources.message(VIEW));
    verify(view.view).setFooter(resources.message(VIEW));
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, generalResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void users_SelectionMode() {
    view = new UsersView(presenter, userDialog);
    mockColumns();
    view.init();
    verify(view.users).setSelectionMode(SelectionMode.MULTI);
  }

  @Test
  public void users_Columns() {
    assertEquals(3, view.users.getColumns().size());
    assertNotNull(view.users.getColumnByKey(EMAIL));
    assertNotNull(view.users.getColumnByKey(LABORATORY));
    assertNotNull(view.users.getColumnByKey(VIEW));
  }

  @Test
  public void users_ColumnsValueProvider() {
    view = new UsersView(presenter, userDialog);
    mockColumns();
    view.init();
    verify(view.users).addColumn(valueProviderCaptor.capture(), eq(EMAIL));
    ValueProvider<User, String> valueProvider = valueProviderCaptor.getValue();
    for (User user : users) {
      assertEquals(user.getEmail(), valueProvider.apply(user));
    }
    verify(view.users).addColumn(valueProviderCaptor.capture(), eq(LABORATORY));
    valueProvider = valueProviderCaptor.getValue();
    for (User user : users) {
      assertEquals(user.getLaboratory() != null ? user.getLaboratory().getName() : "",
          valueProvider.apply(user));
    }
    verify(view.users).addComponentColumn(buttonProviderCaptor.capture());
    ValueProvider<User, Button> buttonProvider = buttonProviderCaptor.getValue();
    for (User user : users) {
      Button button = buttonProvider.apply(user);
      assertTrue(button.getClassNames().contains(VIEW));
      validateIcon(VaadinIcon.EYE.create(), button);
      clickButton(button);
      verify(presenter).view(user);
    }
  }

  @Test
  public void getLocale() {
    assertEquals(locale, view.getLocale());
  }

  @Test
  public void getLocale_French() {
    Locale locale = Locale.FRENCH;
    when(ui.getLocale()).thenReturn(locale);
    assertEquals(locale, view.getLocale());
  }
}
