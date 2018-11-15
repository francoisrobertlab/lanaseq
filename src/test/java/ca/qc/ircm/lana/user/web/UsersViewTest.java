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

import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.web.UsersView.HEADER;
import static ca.qc.ircm.lana.user.web.UsersView.USERS;
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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
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
  @Captor
  private ArgumentCaptor<ValueProvider<User, String>> valueProviderCaptor;
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
    view = new UsersView(presenter);
    users = userRepository.findAll();
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    view.users = mock(Grid.class);
    view.email = mock(Column.class);
    when(view.users.addColumn(any(ValueProvider.class), eq(EMAIL))).thenReturn(view.email);
    when(view.email.setKey(any())).thenReturn(view.email);
    when(view.email.setHeader(any(String.class))).thenReturn(view.email);
    view.laboratory = mock(Column.class);
    when(view.users.addColumn(any(ValueProvider.class), eq(LABORATORY)))
        .thenReturn(view.laboratory);
    when(view.laboratory.setKey(any())).thenReturn(view.laboratory);
    when(view.laboratory.setHeader(any(String.class))).thenReturn(view.laboratory);
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
  }

  @Test
  public void labels() {
    mockColumns();
    view.initUsers();
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.email).setHeader(userResources.message(EMAIL));
    verify(view.email).setFooter(userResources.message(EMAIL));
    verify(view.laboratory).setHeader(userResources.message(LABORATORY));
    verify(view.laboratory).setFooter(userResources.message(LABORATORY));
  }

  @Test
  public void localeChange() {
    mockColumns();
    view.initUsers();
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
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, generalResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void users_SelectionMode() {
    mockColumns();
    view.initUsers();
    verify(view.users).setSelectionMode(SelectionMode.MULTI);
  }

  @Test
  public void users_Columns() {
    view.initUsers();
    assertEquals(2, view.users.getColumns().size());
    assertNotNull(view.users.getColumnByKey(EMAIL));
    assertNotNull(view.users.getColumnByKey(LABORATORY));
  }

  @Test
  public void users_ColumnsValueProvider() {
    mockColumns();
    view.initUsers();
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
