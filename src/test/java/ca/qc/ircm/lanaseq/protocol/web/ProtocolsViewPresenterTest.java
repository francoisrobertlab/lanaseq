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

import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import java.time.LocalDate;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
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
  private AuthorizationService authorizationService;
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

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    view.header = new H2();
    view.protocols = new Grid<>();
    view.protocols.setSelectionMode(SelectionMode.MULTI);
    view.nameFilter = new TextField();
    view.ownerFilter = new TextField();
    view.add = new Button();
    view.dialog = mock(ProtocolDialog.class);
    protocols = protocolRepository.findAll();
    when(protocolService.all()).thenReturn(protocols);
    currentUser = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(currentUser);
  }

  @Test
  public void protocols() {
    presenter.init(view);
    List<Protocol> protocols = items(view.protocols);
    assertEquals(this.protocols.size(), protocols.size());
    for (Protocol protocol : this.protocols) {
      assertTrue(protocol.toString(), protocols.contains(protocol));
    }
    assertEquals(0, view.protocols.getSelectedItems().size());
    protocols.forEach(protocol -> view.protocols.select(protocol));
    assertEquals(protocols.size(), view.protocols.getSelectedItems().size());
  }

  @Test
  public void ownerFilter_User() {
    presenter.init(view);

    assertEquals(currentUser.getEmail(), view.ownerFilter.getValue());
    verify(authorizationService).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void ownerFilter_ManagerOrAdmin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);

    presenter.init(view);

    assertEquals("", view.ownerFilter.getValue());
    verify(authorizationService).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void filterName() {
    presenter.init(view);
    view.protocols.setDataProvider(dataProvider);

    presenter.filterName("test");

    assertEquals("test", presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    presenter.init(view);
    view.protocols.setDataProvider(dataProvider);

    presenter.filterName("");

    assertEquals(null, presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate() {
    presenter.init(view);
    view.protocols.setDataProvider(dataProvider);
    Range<LocalDate> range =
        Range.closed(LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));

    presenter.filterDate(range);

    assertEquals(range, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate_Null() {
    presenter.init(view);
    view.protocols.setDataProvider(dataProvider);

    presenter.filterDate(null);

    assertEquals(null, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner() {
    presenter.init(view);
    view.protocols.setDataProvider(dataProvider);

    presenter.filterOwner("test");

    assertEquals("test", presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    presenter.init(view);
    view.protocols.setDataProvider(dataProvider);

    presenter.filterOwner("");

    assertEquals(null, presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void view() {
    presenter.init(view);
    Protocol protocol = new Protocol();
    protocol.setId(2L);
    Protocol databaseProtocol = new Protocol();
    when(protocolService.get(any())).thenReturn(databaseProtocol);
    presenter.view(protocol);
    verify(protocolService).get(2L);
    verify(view.dialog).setProtocol(databaseProtocol);
    verify(view.dialog).open();
  }

  @Test
  public void add() {
    presenter.init(view);
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
    presenter.init(view);
    verify(view.dialog).addSavedListener(savedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<ProtocolDialog>> savedListener =
        savedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(protocolService, times(2)).all();
  }
}
