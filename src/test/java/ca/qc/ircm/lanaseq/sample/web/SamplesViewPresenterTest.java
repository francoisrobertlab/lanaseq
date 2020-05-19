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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.DateRangeField;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class SamplesViewPresenterTest extends AbstractViewTestCase {
  private SamplesViewPresenter presenter;
  @Mock
  private SamplesView view;
  @Mock
  private SampleService service;
  @Mock
  private ProtocolService protocolService;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private DataProvider<Sample, ?> dataProvider;
  @Captor
  private ArgumentCaptor<Sample> datasetCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<SampleDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<ProtocolDialog>>> protocolSavedListenerCaptor;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private UserRepository userRepository;
  private List<Sample> samples;
  private User currentUser;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new SamplesViewPresenter(service, protocolService, authorizationService);
    view.header = new H2();
    view.samples = new Grid<>();
    view.samples.setSelectionMode(SelectionMode.MULTI);
    view.nameFilter = new TextField();
    view.protocolFilter = new TextField();
    view.dateFilter = mock(DateRangeField.class);
    view.ownerFilter = new TextField();
    view.add = new Button();
    view.dialog = mock(SampleDialog.class);
    view.protocolDialog = mock(ProtocolDialog.class);
    samples = repository.findAll();
    when(service.all()).thenReturn(samples);
    currentUser = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(currentUser);
    presenter.init(view);
  }

  @Test
  public void samples() {
    List<Sample> samples = items(view.samples);
    assertEquals(this.samples.size(), samples.size());
    for (Sample sample : this.samples) {
      assertTrue(sample.toString(), samples.contains(sample));
    }
    assertEquals(0, view.samples.getSelectedItems().size());
    samples.forEach(dataset -> view.samples.select(dataset));
    assertEquals(samples.size(), view.samples.getSelectedItems().size());
  }

  @Test
  public void ownerFilter_User() {
    assertEquals(currentUser.getEmail(), view.ownerFilter.getValue());
    verify(authorizationService).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void ownerFilter_ManagerOrAdmin() {
    view.ownerFilter.setValue("");
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    presenter.init(view);
    assertEquals("", view.ownerFilter.getValue());
    verify(authorizationService, times(2)).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void filterName() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterName("test");

    assertEquals("test", presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterName("");

    assertEquals(null, presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProtocol() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterProtocol("test");

    assertEquals("test", presenter.filter().protocolContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProtocol_Empty() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterProtocol("");

    assertEquals(null, presenter.filter().protocolContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate() {
    view.samples.setDataProvider(dataProvider);
    Range<LocalDate> range = Range.closed(LocalDate.now().minusDays(10), LocalDate.now());

    presenter.filterDate(range);

    assertEquals(range, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate_Empty() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterDate(null);

    assertEquals(null, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterOwner("test");

    assertEquals("test", presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterOwner("");

    assertEquals(null, presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void view() {
    Sample sample = new Sample();
    sample.setId(2L);
    Sample databaseSample = mock(Sample.class);
    when(service.get(any())).thenReturn(databaseSample);
    presenter.view(sample);
    verify(service).get(2L);
    verify(view.dialog).setSample(databaseSample);
    verify(view.dialog).open();
  }

  @Test
  public void view_Protocol() {
    Protocol protocol = new Protocol();
    protocol.setId(1L);
    Protocol databaseProtocol = mock(Protocol.class);
    when(protocolService.get(any())).thenReturn(databaseProtocol);
    presenter.view(protocol);
    verify(protocolService).get(1L);
    verify(view.protocolDialog).setProtocol(databaseProtocol);
    verify(view.protocolDialog).open();
  }

  @Test
  public void add() {
    presenter.add();
    verify(view.dialog).setSample(null);
    verify(view.dialog).open();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshSamplesOnSaved() {
    verify(view.dialog).addSavedListener(savedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<SampleDialog>> savedListener = savedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(service, times(2)).all();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshSamplesOnProtocolSaved() {
    verify(view.protocolDialog).addSavedListener(protocolSavedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<ProtocolDialog>> savedListener =
        protocolSavedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(service, times(2)).all();
  }
}