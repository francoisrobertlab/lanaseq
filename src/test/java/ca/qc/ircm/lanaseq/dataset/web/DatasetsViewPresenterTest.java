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

package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS_REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGE_ERROR;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class DatasetsViewPresenterTest extends AbstractViewTestCase {
  @Autowired
  private DatasetsViewPresenter presenter;
  @Mock
  private DatasetsView view;
  @MockBean
  private DatasetService service;
  @MockBean
  private ProtocolService protocolService;
  @MockBean
  private SampleService sampleService;
  @MockBean
  private AuthorizationService authorizationService;
  @Mock
  private DataProvider<Dataset, ?> dataProvider;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Captor
  private ArgumentCaptor<List<Sample>> samplesCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<DatasetDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<ProtocolDialog>>> protocolSavedListenerCaptor;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private UserRepository userRepository;
  private List<Dataset> datasets;
  private User currentUser;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetsView.class, locale);

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    view.header = new H2();
    view.datasets = new Grid<>();
    view.datasets.setSelectionMode(SelectionMode.MULTI);
    view.ownerFilter = new TextField();
    view.error = new Div();
    view.add = new Button();
    view.datasetDialog = mock(DatasetDialog.class);
    view.protocolDialog = mock(ProtocolDialog.class);
    datasets = repository.findAll();
    when(service.all()).thenReturn(datasets);
    currentUser = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(currentUser);
    presenter.init(view);
  }

  @Test
  public void datasets() {
    List<Dataset> datasets = items(view.datasets);
    assertEquals(this.datasets.size(), datasets.size());
    for (Dataset dataset : this.datasets) {
      assertTrue(dataset.toString(), datasets.contains(dataset));
    }
    assertEquals(0, view.datasets.getSelectedItems().size());
    datasets.forEach(dataset -> view.datasets.select(dataset));
    assertEquals(datasets.size(), view.datasets.getSelectedItems().size());
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
    view.datasets.setDataProvider(dataProvider);

    presenter.filterName("test");

    assertEquals("test", presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    view.datasets.setDataProvider(dataProvider);

    presenter.filterName("");

    assertEquals(null, presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterTags() {
    view.datasets.setDataProvider(dataProvider);

    presenter.filterTags("test");

    assertEquals("test", presenter.filter().tagsContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterTags_Empty() {
    view.datasets.setDataProvider(dataProvider);

    presenter.filterTags("");

    assertEquals(null, presenter.filter().tagsContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProtocol() {
    view.datasets.setDataProvider(dataProvider);

    presenter.filterProtocol("test");

    assertEquals("test", presenter.filter().protocolContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProtocol_Empty() {
    view.datasets.setDataProvider(dataProvider);

    presenter.filterProtocol("");

    assertEquals(null, presenter.filter().protocolContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate() {
    view.datasets.setDataProvider(dataProvider);
    Range<LocalDate> range =
        Range.closed(LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));

    presenter.filterDate(range);

    assertEquals(range, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate_Null() {
    view.datasets.setDataProvider(dataProvider);

    presenter.filterDate(null);

    assertEquals(null, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner() {
    view.datasets.setDataProvider(dataProvider);

    presenter.filterOwner("test");

    assertEquals("test", presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    view.datasets.setDataProvider(dataProvider);

    presenter.filterOwner("");

    assertEquals(null, presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void error() {
    assertFalse(view.error.isVisible());
  }

  @Test
  public void view() {
    Dataset dataset = new Dataset();
    dataset.setId(2L);
    Dataset databaseDataset = new Dataset();
    when(service.get(any())).thenReturn(databaseDataset);
    presenter.view(dataset);
    verify(service).get(2L);
    verify(view.datasetDialog).setDataset(databaseDataset);
    verify(view.datasetDialog).open();
  }

  @Test
  public void view_Protocol() {
    Protocol protocol = new Protocol();
    protocol.setId(1L);
    Protocol databaseProtocol = new Protocol();
    when(protocolService.get(any())).thenReturn(databaseProtocol);
    presenter.view(protocol);
    verify(protocolService).get(1L);
    verify(view.protocolDialog).setProtocol(databaseProtocol);
    verify(view.protocolDialog).open();
  }

  @Test
  public void add() {
    presenter.add();
    verify(view.datasetDialog).setDataset(null);
    verify(view.datasetDialog).open();
  }

  @Test
  public void merge() {
    when(sampleService.isMergable(any())).thenReturn(true);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));
    presenter.merge(locale);
    assertFalse(view.error.isVisible());
    verify(sampleService).isMergable(samplesCaptor.capture());
    assertEquals(5, samplesCaptor.getValue().size());
    assertTrue(find(samplesCaptor.getValue(), 1L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 2L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 3L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 4L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 5L).isPresent());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getId());
    assertEquals(4, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("mnase"));
    assertTrue(dataset.getTags().contains("ip"));
    assertTrue(dataset.getTags().contains("chipseq"));
    assertTrue(dataset.getTags().contains("G24D"));
    assertEquals(5, dataset.getSamples().size());
    assertTrue(find(dataset.getSamples(), 1L).isPresent());
    assertTrue(find(dataset.getSamples(), 2L).isPresent());
    assertTrue(find(dataset.getSamples(), 3L).isPresent());
    assertTrue(find(dataset.getSamples(), 4L).isPresent());
    assertTrue(find(dataset.getSamples(), 5L).isPresent());
    verify(view).showNotification(resources.message(MERGED, dataset.getName()));
  }

  @Test
  public void merge_NoSamples() {
    presenter.merge(locale);
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(DATASETS_REQUIRED), view.error.getText());
    verify(sampleService, never()).isMergable(any());
    verify(service, never()).save(any());
    verify(view, never()).showNotification(any());
  }

  @Test
  public void merge_NotMergeable() {
    when(sampleService.isMergable(any())).thenReturn(false);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));
    presenter.merge(locale);
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(MERGE_ERROR), view.error.getText());
    verify(sampleService).isMergable(samplesCaptor.capture());
    assertEquals(5, samplesCaptor.getValue().size());
    assertTrue(find(samplesCaptor.getValue(), 1L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 2L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 3L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 4L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 5L).isPresent());
    verify(service, never()).save(any());
    verify(view, never()).showNotification(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshDatasetsOnSaved() {
    verify(view.datasetDialog).addSavedListener(savedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<DatasetDialog>> savedListener =
        savedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(service, times(2)).all();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshDatasetsOnProtocolSaved() {
    verify(view.protocolDialog).addSavedListener(protocolSavedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<ProtocolDialog>> savedListener =
        protocolSavedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(service, times(2)).all();
  }
}
