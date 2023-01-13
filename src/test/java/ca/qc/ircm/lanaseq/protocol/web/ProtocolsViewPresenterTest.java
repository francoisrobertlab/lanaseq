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

package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS_MORE_THAN_ONE;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS_REQUIRED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.google.common.collect.Range;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link ProtocolsViewPresenter}.
 */
@ServiceTestAnnotations
@WithMockUser
public class ProtocolsViewPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private ProtocolsViewPresenter presenter;
  @Mock
  private ProtocolsView view;
  @MockBean
  private ProtocolService protocolService;
  @MockBean
  private AuthenticatedUser authenticatedUser;
  @Mock
  private DataProvider<Protocol, ?> dataProvider;
  @Captor
  private ArgumentCaptor<Protocol> protocolCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<ProtocolDialog>>> savedListenerCaptor;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private UserRepository userRepository;
  private List<Protocol> protocols;
  private User currentUser;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ProtocolsView.class, locale);

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    view.header = new H2();
    view.protocols = new Grid<>();
    view.protocols.setSelectionMode(SelectionMode.MULTI);
    view.nameFilter = new TextField();
    view.ownerFilter = new TextField();
    view.error = new Div();
    view.add = new Button();
    view.history = new Button();
    view.dialog = mock(ProtocolDialog.class);
    view.historyDialog = mock(ProtocolHistoryDialog.class);
    protocols = protocolRepository.findAll();
    when(protocolService.all()).thenReturn(protocols);
    currentUser = userRepository.findById(3L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(currentUser));
    presenter.init(view);
    presenter.localeChange(locale);
  }

  @Test
  public void protocols() {
    List<Protocol> protocols = items(view.protocols);
    assertEquals(this.protocols.size(), protocols.size());
    for (Protocol protocol : this.protocols) {
      assertTrue(protocols.contains(protocol), () -> protocol.toString());
    }
    assertEquals(0, view.protocols.getSelectedItems().size());
    protocols.forEach(protocol -> view.protocols.select(protocol));
    assertEquals(protocols.size(), view.protocols.getSelectedItems().size());
  }

  @Test
  public void visible_User() {
    assertFalse(view.history.isVisible());
  }

  @Test
  public void visible_Manager() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    presenter.init(view);
    assertTrue(view.history.isVisible());
  }

  @Test
  public void visible_Admin() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    presenter.init(view);
    assertTrue(view.history.isVisible());
  }

  @Test
  public void ownerFilter_User() {
    assertEquals(currentUser.getEmail(), view.ownerFilter.getValue());
    verify(authenticatedUser).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void ownerFilter_ManagerOrAdmin() {
    view.ownerFilter.setValue("");
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);

    presenter.init(view);

    assertEquals("", view.ownerFilter.getValue());
    verify(authenticatedUser, atLeastOnce()).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void filterName() {
    view.protocols.setDataProvider(dataProvider);

    presenter.filterName("test");

    assertEquals("test", presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    view.protocols.setDataProvider(dataProvider);

    presenter.filterName("");

    assertEquals(null, presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate() {
    view.protocols.setDataProvider(dataProvider);
    Range<LocalDate> range =
        Range.closed(LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));

    presenter.filterDate(range);

    assertEquals(range, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate_Null() {
    view.protocols.setDataProvider(dataProvider);

    presenter.filterDate(null);

    assertEquals(null, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner() {
    view.protocols.setDataProvider(dataProvider);

    presenter.filterOwner("test");

    assertEquals("test", presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    view.protocols.setDataProvider(dataProvider);

    presenter.filterOwner("");

    assertEquals(null, presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void edit() {
    Protocol protocol = new Protocol();
    protocol.setId(2L);
    Protocol databaseProtocol = new Protocol();
    when(protocolService.get(any())).thenReturn(Optional.of(databaseProtocol));
    presenter.edit(protocol);
    verify(protocolService).get(2L);
    verify(view.dialog).setProtocol(databaseProtocol);
    verify(view.dialog).open();
  }

  @Test
  public void history() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    Protocol protocol = protocols.get(0);
    view.protocols.select(protocol);
    Protocol databaseProtocol = new Protocol();
    when(protocolService.get(any())).thenReturn(Optional.of(databaseProtocol));
    presenter.history();
    assertFalse(view.error.isVisible());
    verify(view.historyDialog).setProtocol(databaseProtocol);
    verify(view.historyDialog).open();
  }

  @Test
  public void history_NoRole() {
    Protocol protocol = protocols.get(0);
    view.protocols.select(protocol);
    presenter.history();
    assertFalse(view.error.isVisible());
    verify(view.historyDialog, never()).setProtocol(any());
    verify(view.historyDialog, never()).open();
  }

  @Test
  public void history_NoSelection() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    presenter.history();
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(PROTOCOLS_REQUIRED), view.error.getText());
    verify(view.historyDialog, never()).setProtocol(any());
    verify(view.historyDialog, never()).open();
  }

  @Test
  public void history_MoreThanOneProtocolSelected() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    view.protocols.select(protocols.get(0));
    view.protocols.select(protocols.get(1));
    presenter.history();
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(PROTOCOLS_MORE_THAN_ONE), view.error.getText());
    verify(view.historyDialog, never()).setProtocol(any());
    verify(view.historyDialog, never()).open();
  }

  @Test
  public void history_Protocol_NoRole() {
    Protocol protocol = new Protocol();
    protocol.setId(2L);
    presenter.history(protocol);
    verify(view.historyDialog, never()).setProtocol(any());
    verify(view.historyDialog, never()).open();
  }

  @Test
  public void history_Protocol_WithRole() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    Protocol protocol = new Protocol();
    protocol.setId(2L);
    Protocol databaseProtocol = new Protocol();
    when(protocolService.get(any())).thenReturn(Optional.of(databaseProtocol));
    presenter.history(protocol);
    verify(authenticatedUser).hasAnyRole(UserRole.MANAGER, UserRole.ADMIN);
    verify(protocolService).get(2L);
    verify(view.historyDialog).setProtocol(databaseProtocol);
    verify(view.historyDialog).open();
  }

  @Test
  public void add() {
    presenter.add();
    verify(view.dialog).setProtocol(protocolCaptor.capture());
    Protocol protocol = protocolCaptor.getValue();
    assertNull(protocol.getId());
    assertNull(protocol.getName());
    verify(view.dialog).open();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshProtocolsOnSaved() {
    verify(view.dialog).addSavedListener(savedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<ProtocolDialog>> savedListener =
        savedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(protocolService, times(2)).all();
  }
}
