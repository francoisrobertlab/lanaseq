package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.ANALYZE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS_MORE_THAN_ONE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS_REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGE_ERROR;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
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

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.ErrorNotification;
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
  private static final String MESSAGE_PREFIX = messagePrefix(DatasetsView.class);
  private static final String DATASET_PREFIX = messagePrefix(Dataset.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
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
  private DatasetRepository repository;
  @Autowired
  private EntityManager entityManager;
  private Locale locale = Locale.ENGLISH;
  private List<Dataset> datasets;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(service.get(any())).then(
        i -> i.getArgument(0) != null ? repository.findById(i.getArgument(0)) : Optional.empty());
    datasets = repository.findAll();
    when(service.all(any())).thenReturn(datasets);
    UI.getCurrent().setLocale(locale);
    view = navigate(DatasetsView.class);
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(DatasetGrid.ID, view.datasets.getId().orElse(""));
    assertEquals(EDIT, view.edit.getId().orElse(""));
    validateIcon(VaadinIcon.EDIT.create(), view.edit.getIcon());
    assertEquals(MERGE, view.merge.getId().orElse(""));
    validateIcon(VaadinIcon.CONNECT.create(), view.merge.getIcon());
    assertEquals(FILES, view.files.getId().orElse(""));
    validateIcon(VaadinIcon.FILE_O.create(), view.files.getIcon());
    assertEquals(ANALYZE, view.analyze.getId().orElse(""));
  }

  @Test
  public void labels() {
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + EDIT), view.edit.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MERGE), view.merge.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FILES), view.files.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ANALYZE), view.analyze.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + EDIT), view.edit.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MERGE), view.merge.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FILES), view.files.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ANALYZE), view.analyze.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TITLE,
        view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME)), view.getPageTitle());
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
    assertEquals(dataset.getId(), dialog.getDatasetId());
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
    assertEquals(dataset.getId(), dialog.getDatasetId());
  }

  @Test
  public void datasets_AddFiles_Meta() {
    Dataset dataset = datasets.get(0);
    when(service.get(any())).thenReturn(Optional.of(dataset));

    clickItem(view.datasets, dataset, view.datasets.name, false, false, false, true);

    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();
    assertEquals(dataset.getId(), dialog.getDatasetId());
  }

  @Test
  public void edit_Enabled() {
    assertFalse(view.edit.isEnabled());
    view.datasets.select(datasets.get(0));
    assertTrue(view.edit.isEnabled());
    view.datasets.select(datasets.get(1));
    assertFalse(view.edit.isEnabled());
    view.datasets.deselectAll();
    assertFalse(view.edit.isEnabled());
  }

  @Test
  public void edit() {
    Dataset dataset = datasets.get(0);
    view.datasets.select(dataset);

    view.edit.click();

    DatasetDialog dialog = $(DatasetDialog.class).first();
    assertEquals(dataset.getId(), dialog.getDatasetId());
  }

  @Test
  public void edit_NoSelection() {
    view.edit();

    Notification error = $(Notification.class).first();
    assertTrue(error instanceof ErrorNotification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DATASETS_REQUIRED),
        ((ErrorNotification) error).getText());
    assertFalse($(DatasetFilesDialog.class).exists());
  }

  @Test
  public void edit_MoreThanOneDatasetSelected() {
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));

    view.edit();

    Notification error = $(Notification.class).first();
    assertTrue(error instanceof ErrorNotification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DATASETS_MORE_THAN_ONE),
        ((ErrorNotification) error).getText());
    assertFalse($(DatasetFilesDialog.class).exists());
  }

  @Test
  public void merge_Enabled() {
    assertFalse(view.merge.isEnabled());
    view.datasets.select(datasets.get(0));
    assertTrue(view.merge.isEnabled());
    view.datasets.select(datasets.get(1));
    assertTrue(view.merge.isEnabled());
    view.datasets.deselectAll();
    assertFalse(view.merge.isEnabled());
  }

  @Test
  public void merge() {
    when(sampleService.isMergable(any())).thenReturn(true);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));

    view.merge.click();

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
    assertEquals(4, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("mnase"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains("G24D"));
    assertEquals(5, dataset.getSamples().size());
    assertEquals((Long) 1L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 2L, dataset.getSamples().get(1).getId());
    assertEquals((Long) 3L, dataset.getSamples().get(2).getId());
    assertEquals((Long) 4L, dataset.getSamples().get(3).getId());
    assertEquals((Long) 5L, dataset.getSamples().get(4).getId());
    assertEquals(datasets.get(0).getDate(), dataset.getDate());
    Notification notification = $(Notification.class).first();
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MERGED, dataset.getName()),
        test(notification).getText());
  }

  @Test
  public void merge_RefreshAfter() {
    when(sampleService.isMergable(any())).thenReturn(true);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));
    view.datasets = spy(view.datasets);

    view.merge.click();

    verify(view.datasets).refreshDatasets();
  }

  @Test
  public void merge_SortById() {
    when(sampleService.isMergable(any())).thenReturn(true);
    view.datasets.select(datasets.get(1));
    view.datasets.select(datasets.get(0));

    view.merge.click();

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
    assertEquals(4, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("mnase"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains("G24D"));
    assertEquals(5, dataset.getSamples().size());
    assertEquals((Long) 1L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 2L, dataset.getSamples().get(1).getId());
    assertEquals((Long) 3L, dataset.getSamples().get(2).getId());
    assertEquals((Long) 4L, dataset.getSamples().get(3).getId());
    assertEquals((Long) 5L, dataset.getSamples().get(4).getId());
    assertEquals(datasets.get(0).getDate(), dataset.getDate());
    Notification notification = $(Notification.class).first();
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MERGED, dataset.getName()),
        test(notification).getText());
  }

  @Test
  public void merge_NoSamples() {
    view.merge();

    Notification error = $(Notification.class).first();
    assertTrue(error instanceof ErrorNotification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DATASETS_REQUIRED),
        ((ErrorNotification) error).getText());
    verify(sampleService, never()).isMergable(any());
    verify(service, never()).save(any());
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

    view.merge.click();

    verify(sampleService).isMergable(samplesCaptor.capture());
    assertEquals(2, samplesCaptor.getValue().size());
    assertTrue(find(samplesCaptor.getValue(), 4L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 5L).isPresent());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getId());
    assertEquals(3, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("ip"));
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains("G24D"));
    assertEquals(2, dataset.getSamples().size());
    assertEquals((Long) 4L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 5L, dataset.getSamples().get(1).getId());
    assertEquals(dataset1.getDate(), dataset.getDate());
    Notification notification = $(Notification.class).first();
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MERGED, dataset.getName()),
        test(notification).getText());
  }

  @Test
  public void merge_NotMergeable() {
    when(sampleService.isMergable(any())).thenReturn(false);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));

    view.merge.click();

    Notification error = $(Notification.class).first();
    assertTrue(error instanceof ErrorNotification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MERGE_ERROR),
        ((ErrorNotification) error).getText());
    verify(sampleService).isMergable(samplesCaptor.capture());
    assertEquals(5, samplesCaptor.getValue().size());
    assertTrue(find(samplesCaptor.getValue(), 1L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 2L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 3L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 4L).isPresent());
    assertTrue(find(samplesCaptor.getValue(), 5L).isPresent());
    verify(service, never()).save(any());
  }

  @Test
  public void merge_NameExists() {
    when(sampleService.isMergable(any())).thenReturn(true);
    when(service.exists(any())).thenReturn(true);
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));

    view.merge.click();

    Notification error = $(Notification.class).first();
    assertTrue(error instanceof ErrorNotification);
    assertEquals(
        view.getTranslation(DATASET_PREFIX + NAME_ALREADY_EXISTS,
            "MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3-JS1-JS2_20181020"),
        ((ErrorNotification) error).getText());
    verify(service).exists("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3-JS1-JS2_20181020");
    verify(service, never()).save(any());
  }

  @Test
  public void files_Enabled() {
    assertFalse(view.files.isEnabled());
    view.datasets.select(datasets.get(0));
    assertTrue(view.files.isEnabled());
    view.datasets.select(datasets.get(1));
    assertFalse(view.files.isEnabled());
    view.datasets.deselectAll();
    assertFalse(view.files.isEnabled());
  }

  @Test
  public void files() {
    Dataset dataset = datasets.get(0);
    view.datasets.select(dataset);

    view.files.click();

    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();
    assertEquals(dataset.getId(), dialog.getDatasetId());
  }

  @Test
  public void files_NoSelection() {
    view.viewFiles();

    Notification error = $(Notification.class).first();
    assertTrue(error instanceof ErrorNotification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DATASETS_REQUIRED),
        ((ErrorNotification) error).getText());
    assertFalse($(DatasetFilesDialog.class).exists());
  }

  @Test
  public void files_MoreThanOneDatasetSelected() {
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));

    view.viewFiles();

    Notification error = $(Notification.class).first();
    assertTrue(error instanceof ErrorNotification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DATASETS_MORE_THAN_ONE),
        ((ErrorNotification) error).getText());
    assertFalse($(DatasetFilesDialog.class).exists());
  }

  @Test
  public void analyze_Enabled() {
    assertFalse(view.analyze.isEnabled());
    view.datasets.select(datasets.get(0));
    assertTrue(view.analyze.isEnabled());
    view.datasets.select(datasets.get(1));
    assertTrue(view.analyze.isEnabled());
    view.datasets.deselectAll();
    assertFalse(view.analyze.isEnabled());
  }

  @Test
  public void analyze_One() {
    Dataset dataset = datasets.get(0);
    view.datasets.select(dataset);

    view.analyze.click();

    DatasetsAnalysisDialog dialog = $(DatasetsAnalysisDialog.class).first();
    List<Long> datasetIds = dialog.getDatasetIds();
    assertEquals(1, datasetIds.size());
    assertTrue(datasetIds.contains(dataset.getId()));
  }

  @Test
  public void analyze_Many() {
    view.datasets.select(datasets.get(0));
    view.datasets.select(datasets.get(1));

    view.analyze.click();

    DatasetsAnalysisDialog dialog = $(DatasetsAnalysisDialog.class).first();
    List<Long> datasetIds = dialog.getDatasetIds();
    assertEquals(2, datasetIds.size());
    assertTrue(datasetIds.contains(datasets.get(0).getId()));
    assertTrue(datasetIds.contains(datasets.get(1).getId()));
  }

  @Test
  public void analyze_NoSelection() {
    view.analyze();

    assertFalse($(DatasetsAnalysisDialog.class).exists());
    Notification error = $(Notification.class).first();
    assertTrue(error instanceof ErrorNotification);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DATASETS_REQUIRED),
        ((ErrorNotification) error).getText());
  }
}
