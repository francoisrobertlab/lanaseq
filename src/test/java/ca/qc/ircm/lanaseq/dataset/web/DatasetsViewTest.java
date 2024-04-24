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

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.ANALYZE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS_MORE_THAN_ONE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS_REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGE_ERROR;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.EditEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link DatasetsView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetsViewTest extends SpringUIUnitTest {
  private DatasetsView view;
  @MockBean
  private DatasetService service;
  @MockBean
  private SampleService sampleService;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Captor
  private ArgumentCaptor<List<Sample>> samplesCaptor;
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private EntityManager entityManager;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetsView.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private AppResources datasetResources = new AppResources(Dataset.class, locale);
  private List<Dataset> datasets;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    datasets = datasetRepository.findAll();
    when(service.all(any())).thenReturn(datasets);
    UI.getCurrent().setLocale(locale);
    view = navigate(DatasetsView.class);
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(HEADER, view.header.getId().orElse(""));
    assertEquals(DatasetGrid.ID, view.datasets.getId().orElse(""));
    assertEquals(ERROR_TEXT, view.error.getId().orElse(""));
    assertTrue(view.error.getClassNames().contains(ERROR_TEXT));
    assertEquals(MERGE, view.merge.getId().orElse(""));
    validateIcon(VaadinIcon.CONNECT.create(), view.merge.getIcon());
    assertEquals(FILES, view.files.getId().orElse(""));
    validateIcon(VaadinIcon.FILE_O.create(), view.files.getIcon());
    assertEquals(ANALYZE, view.analyze.getId().orElse(""));
  }

  @Test
  public void labels() {
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals(resources.message(MERGE), view.merge.getText());
    assertEquals(resources.message(FILES), view.files.getText());
    assertEquals(resources.message(ANALYZE), view.analyze.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(DatasetsView.class, locale);
    UI.getCurrent().setLocale(locale);
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals(resources.message(MERGE), view.merge.getText());
    assertEquals(resources.message(FILES), view.files.getText());
    assertEquals(resources.message(ANALYZE), view.analyze.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void datasets() {
    assertTrue(view.datasets.getSelectionModel() instanceof SelectionModel.Multi);
  }

  @Test
  public void datasets_View() {
    Dataset dataset = datasets.get(0);
    when(service.get(any())).thenReturn(Optional.of(dataset));

    doubleClickItem(view.datasets, dataset);

    DatasetDialog dialog = $(DatasetDialog.class).first();
    assertEquals(dataset, dialog.getDataset());
    verify(service).get(dataset.getId());
  }

  @Test
  public void datasets_View_RefreshOnSave() {
    Dataset dataset = datasets.get(0);
    when(service.get(any())).thenReturn(Optional.of(dataset));

    doubleClickItem(view.datasets, dataset);

    DatasetDialog dialog = $(DatasetDialog.class).first();
    view.datasets.setItems(mock(DataProvider.class));
    dialog.fireSavedEvent();
    verify(view.datasets.getDataProvider()).refreshAll();
  }

  @Test
  public void datasets_View_RefreshOnDelete() {
    Dataset dataset = datasets.get(0);
    when(service.get(any())).thenReturn(Optional.of(dataset));

    doubleClickItem(view.datasets, dataset);

    DatasetDialog dialog = $(DatasetDialog.class).first();
    view.datasets.setItems(mock(DataProvider.class));
    dialog.fireDeletedEvent();
    verify(view.datasets.getDataProvider()).refreshAll();
  }

  @Test
  public void datasets_AddFiles_Control() {
    Dataset dataset = datasets.get(0);
    when(service.get(any())).thenReturn(Optional.of(dataset));

    clickItem(view.datasets, dataset, view.datasets.name, true, false, false, false);

    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();
    assertEquals(dataset, dialog.getDataset());
  }

  @Test
  public void datasets_AddFiles_Meta() {
    Dataset dataset = datasets.get(0);
    when(service.get(any())).thenReturn(Optional.of(dataset));

    clickItem(view.datasets, dataset, view.datasets.name, false, false, false, true);

    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();
    assertEquals(dataset, dialog.getDataset());
  }

  @Test
  public void datasets_Edit() {
    Dataset dataset = datasets.get(0);
    when(service.get(any())).thenReturn(Optional.of(dataset));

    fireEvent(view.datasets, new EditEvent<>(view.datasets, false, dataset));

    DatasetDialog dialog = $(DatasetDialog.class).first();
    assertEquals(dataset, dialog.getDataset());
    verify(service).get(dataset.getId());
  }

  @Test
  public void merge() {
    when(sampleService.isMergable(any())).thenReturn(true);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));

    clickButton(view.merge);

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
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(MERGED, dataset.getName()), test(notification).getText());
  }

  @Test
  public void merge_RefreshAfter() {
    when(sampleService.isMergable(any())).thenReturn(true);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));
    view.datasets = spy(view.datasets);

    clickButton(view.merge);

    verify(view.datasets).refreshDatasets();
  }

  @Test
  public void merge_SortById() {
    when(sampleService.isMergable(any())).thenReturn(true);
    view.datasets.select(datasets.get(1));
    view.datasets.select(datasets.get(0));

    clickButton(view.merge);

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
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(MERGED, dataset.getName()), test(notification).getText());
  }

  @Test
  public void merge_NoSamples() {
    clickButton(view.merge);

    assertTrue(view.error.isVisible());
    assertEquals(resources.message(DATASETS_REQUIRED), view.error.getText());
    verify(sampleService, never()).isMergable(any());
    verify(service, never()).save(any());
    assertFalse($(Notification.class).exists());
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

    clickButton(view.merge);

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
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(MERGED, dataset.getName()), test(notification).getText());
  }

  @Test
  public void merge_NotMergeable() {
    when(sampleService.isMergable(any())).thenReturn(false);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));

    clickButton(view.merge);

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
    assertFalse($(Notification.class).exists());
  }

  @Test
  public void merge_NameExists() {
    when(sampleService.isMergable(any())).thenReturn(true);
    when(service.exists(any())).thenReturn(true);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));

    clickButton(view.merge);

    assertTrue(view.error.isVisible());
    assertEquals(
        datasetResources.message(NAME_ALREADY_EXISTS,
            "MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3-JS1-JS2_20181020"),
        view.error.getText());
    verify(service).exists("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3-JS1-JS2_20181020");
    verify(service, never()).save(any());
    assertFalse($(Notification.class).exists());
  }

  @Test
  public void files() {
    Dataset dataset = datasets.get(0);
    view.datasets.select(dataset);

    clickButton(view.files);

    assertFalse(view.error.isVisible());
    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();
    assertEquals(dataset, dialog.getDataset());
  }

  @Test
  public void files_NoSelection() {
    clickButton(view.files);

    assertTrue(view.error.isVisible());
    assertEquals(resources.message(DATASETS_REQUIRED), view.error.getText());
    assertFalse($(DatasetFilesDialog.class).exists());
  }

  @Test
  public void files_MoreThanOneDatasetSelected() {
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));

    clickButton(view.files);

    assertTrue(view.error.isVisible());
    assertEquals(resources.message(DATASETS_MORE_THAN_ONE), view.error.getText());
    assertFalse($(DatasetFilesDialog.class).exists());
  }

  @Test
  public void analyze_One() {
    Dataset dataset = datasets.get(0);
    view.datasets.select(dataset);

    clickButton(view.analyze);

    assertFalse(view.error.isVisible());
    DatasetsAnalysisDialog dialog = $(DatasetsAnalysisDialog.class).first();
    List<Dataset> datasets = dialog.getDatasets();
    assertEquals(1, datasets.size());
    assertTrue(datasets.contains(dataset));
  }

  @Test
  public void analyze_Many() {
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));

    clickButton(view.analyze);

    assertFalse(view.error.isVisible());
    DatasetsAnalysisDialog dialog = $(DatasetsAnalysisDialog.class).first();
    List<Dataset> datasets = dialog.getDatasets();
    assertEquals(2, datasets.size());
    assertTrue(datasets.contains(datasets.get(0)));
    assertTrue(datasets.contains(datasets.get(1)));
  }

  @Test
  public void analyze_NoSelection() {
    clickButton(view.analyze);

    assertFalse($(DatasetsAnalysisDialog.class).exists());
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(DATASETS_REQUIRED), view.error.getText());
  }

  @Test
  public void analyze_ClearError() {
    clickButton(view.analyze);
    assertTrue(view.error.isVisible());
    view.datasets.select(datasets.get(0));
    clickButton(view.analyze);
    assertFalse(view.error.isVisible());
  }
}
