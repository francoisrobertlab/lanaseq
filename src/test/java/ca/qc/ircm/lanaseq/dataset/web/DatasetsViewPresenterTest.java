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

import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS_MORE_THAN_ONE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS_REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGE_ERROR;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link DatasetsViewPresenter}.
 */
@ServiceTestAnnotations
@WithMockUser
public class DatasetsViewPresenterTest extends AbstractKaribuTestCase {
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
  private AuthenticatedUser authenticatedUser;
  @Autowired
  private ObjectFactory<DatasetDialog> dialogFactory;
  @MockBean
  private DatasetDialog dialog;
  @Autowired
  private ObjectFactory<DatasetFilesDialog> filesDialogFactory;
  @MockBean
  private DatasetFilesDialog filesDialog;
  @Autowired
  private ObjectFactory<DatasetsAnalysisDialog> analysisDialogFactory;
  @MockBean
  private DatasetsAnalysisDialog analysisDialog;
  @Mock
  private DataProvider<Dataset, ?> dataProvider;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Captor
  private ArgumentCaptor<List<Sample>> samplesCaptor;
  @Captor
  private ArgumentCaptor<List<Dataset>> datasetsCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<DatasetDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<DeletedEvent<DatasetDialog>>> deletedListenerCaptor;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<SavedEvent<ProtocolDialog>>> protocolSavedListenerCaptor;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private EntityManager entityManager;
  private List<Dataset> datasets;
  private User currentUser;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetsView.class, locale);
  private AppResources datasetResources = new AppResources(Dataset.class, locale);

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    view.header = new H2();
    view.datasets = new DatasetGrid();
    view.datasets.setSelectionMode(SelectionMode.MULTI);
    view.error = new Div();
    view.dialogFactory = dialogFactory;
    view.filesDialogFactory = filesDialogFactory;
    view.analysisDialogFactory = analysisDialogFactory;
    datasets = repository.findAll();
    view.datasets.setItems(datasets);
    currentUser = userRepository.findById(3L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(currentUser));
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.init(view);
    presenter.localeChange(locale);
  }

  @Test
  public void datasets() {
    assertEquals(0, view.datasets.getSelectedItems().size());
    datasets.forEach(dataset -> view.datasets.select(dataset));
    assertEquals(datasets.size(), view.datasets.getSelectedItems().size());
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
    when(service.get(any())).thenReturn(Optional.of(databaseDataset));
    presenter.view(dataset);
    verify(service).get(2L);
    verify(dialog).setDataset(databaseDataset);
    verify(dialog).addSavedListener(any());
    verify(dialog).addDeletedListener(any());
    verify(dialog).open();
  }

  @Test
  public void viewFiles() {
    Dataset dataset = datasets.get(0);
    view.datasets.select(dataset);
    presenter.viewFiles();
    assertFalse(view.error.isVisible());
    verify(filesDialog).setDataset(dataset);
    verify(filesDialog).open();
  }

  @Test
  public void viewFiles_NoSelection() {
    presenter.viewFiles();
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(DATASETS_REQUIRED), view.error.getText());
    verify(filesDialog, never()).setDataset(any());
    verify(filesDialog, never()).open();
  }

  @Test
  public void viewFiles_MoreThanOneDatasetSelected() {
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));
    presenter.viewFiles();
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(DATASETS_MORE_THAN_ONE), view.error.getText());
    verify(filesDialog, never()).setDataset(any());
    verify(filesDialog, never()).open();
  }

  @Test
  public void viewFiles_Dataset() {
    Dataset dataset = datasets.get(0);
    presenter.viewFiles(dataset);
    verify(filesDialog).setDataset(dataset);
    verify(filesDialog).open();
  }

  @Test
  public void merge() {
    when(sampleService.isMergable(any())).thenReturn(true);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));
    presenter.merge();
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
    assertEquals((Long) 1L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 2L, dataset.getSamples().get(1).getId());
    assertEquals((Long) 3L, dataset.getSamples().get(2).getId());
    assertEquals((Long) 4L, dataset.getSamples().get(3).getId());
    assertEquals((Long) 5L, dataset.getSamples().get(4).getId());
    assertEquals(datasets.get(0).getDate(), dataset.getDate());
    verify(view).showNotification(resources.message(MERGED, dataset.getName()));
  }

  @Test
  public void merge_SortById() {
    when(sampleService.isMergable(any())).thenReturn(true);
    view.datasets.select(datasets.get(1));
    view.datasets.select(datasets.get(0));
    presenter.merge();
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
    assertEquals((Long) 1L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 2L, dataset.getSamples().get(1).getId());
    assertEquals((Long) 3L, dataset.getSamples().get(2).getId());
    assertEquals((Long) 4L, dataset.getSamples().get(3).getId());
    assertEquals((Long) 5L, dataset.getSamples().get(4).getId());
    assertEquals(datasets.get(0).getDate(), dataset.getDate());
    verify(view).showNotification(resources.message(MERGED, dataset.getName()));
  }

  @Test
  public void merge_NoSamples() {
    presenter.merge();
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(DATASETS_REQUIRED), view.error.getText());
    verify(sampleService, never()).isMergable(any());
    verify(service, never()).save(any());
    verify(view, never()).showNotification(any());
  }

  @Test
  public void merge_DuplicatedSample() {
    when(sampleService.isMergable(any())).thenReturn(true);
    Dataset dataset1 = find(datasets, 2L).get();
    Dataset dataset2 = find(datasets, 6L).get();
    dataset1.getSamples();
    dataset1.getSamples().forEach(sample -> entityManager.detach(sample));
    dataset2.getSamples();
    dataset2.getSamples().forEach(sample -> entityManager.detach(sample));
    view.datasets.select(dataset1);
    view.datasets.select(dataset2);
    presenter.merge();
    assertFalse(view.error.isVisible());
    verify(sampleService).isMergable(samplesCaptor.capture());
    assertEquals(2, samplesCaptor.getValue().size());
    assertTrue(find(samplesCaptor.getValue(), 4L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 5L).isPresent());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getId());
    assertEquals(3, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("ip"));
    assertTrue(dataset.getTags().contains("chipseq"));
    assertTrue(dataset.getTags().contains("G24D"));
    assertEquals(2, dataset.getSamples().size());
    assertEquals((Long) 4L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 5L, dataset.getSamples().get(1).getId());
    assertEquals(dataset1.getDate(), dataset.getDate());
    verify(view).showNotification(resources.message(MERGED, dataset.getName()));
  }

  @Test
  public void merge_NotMergeable() {
    when(sampleService.isMergable(any())).thenReturn(false);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));
    presenter.merge();
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
  public void merge_NameExists() {
    when(sampleService.isMergable(any())).thenReturn(true);
    when(service.exists(any())).thenReturn(true);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));
    presenter.merge();
    assertTrue(view.error.isVisible());
    assertEquals(
        datasetResources.message(NAME_ALREADY_EXISTS,
            "MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3-JS1-JS2_20181020"),
        view.error.getText());
    verify(service).exists("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3-JS1-JS2_20181020");
    verify(service, never()).save(any());
    verify(view, never()).showNotification(any());
  }

  @Test
  public void analyze_One() {
    Dataset dataset = datasets.get(0);
    view.datasets.select(dataset);
    presenter.analyze();
    assertFalse(view.error.isVisible());
    verify(analysisDialog).setDatasets(datasetsCaptor.capture());
    List<Dataset> datasets = datasetsCaptor.getValue();
    assertEquals(1, datasets.size());
    assertTrue(datasets.contains(dataset));
    verify(analysisDialog).open();
  }

  @Test
  public void analyze_Many() {
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));
    presenter.analyze();
    assertFalse(view.error.isVisible());
    verify(analysisDialog).setDatasets(datasetsCaptor.capture());
    List<Dataset> datasets = datasetsCaptor.getValue();
    assertEquals(2, datasets.size());
    assertTrue(datasets.contains(datasets.get(0)));
    assertTrue(datasets.contains(datasets.get(1)));
    verify(analysisDialog).open();
  }

  @Test
  public void analyze_NoSelection() {
    datasets.clear();
    presenter.analyze();
    verify(analysisDialog, never()).setDatasets(any());
    verify(analysisDialog, never()).open();
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(DATASETS_REQUIRED), view.error.getText());
  }

  @Test
  public void analyze_ClearError() {
    presenter.analyze();
    assertTrue(view.error.isVisible());
    view.datasets.select(datasets.get(0));
    presenter.analyze();
    assertFalse(view.error.isVisible());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshDatasetsOnSaved() {
    Dataset dataset = mock(Dataset.class);
    when(service.get(any())).thenReturn(Optional.of(dataset));
    view.datasets = mock(DatasetGrid.class);
    presenter.view(dataset);

    verify(dialog).addSavedListener(savedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<DatasetDialog>> savedListener =
        savedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(view.datasets).refreshDatasets();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshDatasetsOnDeleted() {
    Dataset dataset = mock(Dataset.class);
    when(service.get(any())).thenReturn(Optional.of(dataset));
    view.datasets = mock(DatasetGrid.class);
    presenter.view(dataset);

    verify(dialog).addDeletedListener(deletedListenerCaptor.capture());
    ComponentEventListener<DeletedEvent<DatasetDialog>> deletedListener =
        deletedListenerCaptor.getValue();
    deletedListener.onComponentEvent(mock(DeletedEvent.class));
    verify(view.datasets).refreshDatasets();
  }
}
