package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.CREATE_FOLDER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.ERRORS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.FILENAME_PATTERNS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.id;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.CREATE_FOLDER_EXCEPTION;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.analysis.AnalysisService;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.UserAgent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link DatasetsAnalysisDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetsAnalysisDialogTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(DatasetsAnalysisDialog.class);
  private DatasetsAnalysisDialog dialog;
  @MockitoBean
  private DatasetService service;
  @MockitoBean
  private AnalysisService analysisService;
  @MockitoBean
  private AppConfiguration configuration;
  @Autowired
  private DatasetRepository repository;
  @Captor
  private ArgumentCaptor<Collection<String>> filenamePatternsCaptor;
  private Locale locale = Locale.ENGLISH;
  private List<Dataset> datasets = new ArrayList<>();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(service.get(anyLong())).then(
        i -> i.getArgument(0) != null ? repository.findById(i.getArgument(0)) : Optional.empty());
    when(configuration.getAnalysis()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    datasets.add(repository.findById(6L).get());
    datasets.add(repository.findById(7L).get());
    UI.getCurrent().setLocale(locale);
    DatasetsView view = navigate(DatasetsView.class);
    view.datasets.setItems(repository.findAll());
    datasets.forEach(sample -> view.datasets.select(sample));
    view.analyze.click();
    dialog = $(DatasetsAnalysisDialog.class).first();
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(MESSAGE), dialog.message.getId().orElse(""));
    assertEquals(id(FILENAME_PATTERNS), dialog.filenamePatterns.getId().orElse(""));
    assertEquals(id(CREATE_FOLDER), dialog.createFolder.getId().orElse(""));
    assertTrue(
        dialog.createFolder.getThemeNames().contains(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    assertEquals(id(CONFIRM), dialog.confirm.getId().orElse(""));
    assertEquals(id(ERRORS), dialog.errors.getId().orElse(""));
  }

  @Test
  public void labels() {
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, datasets.size()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE), dialog.message.getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILENAME_PATTERNS),
        dialog.filenamePatterns.getHelperText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + CREATE_FOLDER),
        dialog.createFolder.getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + CONFIRM),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + property(CONFIRM, CONFIRM)),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + ERRORS),
        dialog.errors.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + property(ERRORS, CONFIRM)),
        dialog.errors.getElement().getProperty("confirmText"));
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, datasets.size()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE), dialog.message.getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILENAME_PATTERNS),
        dialog.filenamePatterns.getHelperText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + CREATE_FOLDER),
        dialog.createFolder.getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + CONFIRM),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + property(CONFIRM, CONFIRM)),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + ERRORS),
        dialog.errors.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + property(ERRORS, CONFIRM)),
        dialog.errors.getElement().getProperty("confirmText"));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void createFolder_Windows() throws Throwable {
    String folder = "test/dataset";
    when(configuration.getAnalysis().label(any(Collection.class), anyBoolean())).thenReturn(folder);
    dialog.filenamePatterns.setValue("*.fastq", "*.bam");

    dialog.createFolder.click();

    verify(analysisService).copyDatasetsResources(eq(datasets), filenamePatternsCaptor.capture());
    assertEquals(2, filenamePatternsCaptor.getValue().size());
    assertTrue(filenamePatternsCaptor.getValue().contains("*.fastq"));
    assertTrue(filenamePatternsCaptor.getValue().contains("*.bam"));
    verify(configuration.getAnalysis()).label(datasets, false);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void createFolder_Unix() throws Throwable {
    String folder = "test/dataset";
    when(configuration.getAnalysis().label(any(Collection.class), anyBoolean())).thenReturn(folder);
    dialog.filenamePatterns.setValue("*.fastq");

    dialog.createFolder.click();

    verify(analysisService).copyDatasetsResources(eq(datasets), filenamePatternsCaptor.capture());
    assertEquals(1, filenamePatternsCaptor.getValue().size());
    assertTrue(filenamePatternsCaptor.getValue().contains("*.fastq"));
    verify(configuration.getAnalysis()).label(datasets, true);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void createFolder_Mac() throws Throwable {
    String folder = "test/dataset";
    when(configuration.getAnalysis().label(any(Collection.class), anyBoolean())).thenReturn(folder);
    dialog.filenamePatterns.setValue("*.fastq");

    dialog.createFolder.click();

    verify(analysisService).copyDatasetsResources(eq(datasets), filenamePatternsCaptor.capture());
    assertEquals(1, filenamePatternsCaptor.getValue().size());
    assertTrue(filenamePatternsCaptor.getValue().contains("*.fastq"));
    verify(configuration.getAnalysis()).label(datasets, true);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  public void createFolder_IoException() throws Throwable {
    doThrow(new IOException("test")).when(analysisService)
        .copyDatasetsResources(any(Collection.class), any(Collection.class));
    dialog.filenamePatterns.setValue("*.fastq");

    dialog.createFolder.click();

    verify(analysisService).copyDatasetsResources(eq(datasets), filenamePatternsCaptor.capture());
    assertEquals(1, filenamePatternsCaptor.getValue().size());
    assertTrue(filenamePatternsCaptor.getValue().contains("*.fastq"));
    assertFalse(dialog.confirm.isOpened());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + CREATE_FOLDER_EXCEPTION),
        test(dialog.errors).getText());
    assertTrue(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  public void closeOnConfirm() {
    fireEvent(dialog.confirm, new ConfirmDialog.ConfirmEvent(dialog.confirm, false));
    assertFalse(dialog.isOpened());
  }

  @Test
  public void closeOnErrorsConfirm() {
    fireEvent(dialog.errors, new ConfirmDialog.ConfirmEvent(dialog.errors, false));
    assertFalse(dialog.isOpened());
  }

  @Test
  public void getDatasetIds() {
    List<Long> datasetIds = dialog.getDatasetIds();
    assertEquals(this.datasets.size(), datasetIds.size());
    for (Dataset dataset : this.datasets) {
      assertTrue(datasetIds.contains(dataset.getId()));
    }
  }

  @Test
  public void setDatasetId() {
    Dataset dataset = repository.findById(6L).get();
    dialog.setDatasetId(6L);
    assertEquals(
        dialog.getTranslation(MESSAGE_PREFIX + SamplesAnalysisDialog.HEADER, 1, dataset.getName()),
        dialog.getHeaderTitle());
  }

  @Test
  public void setDatasetIds() {
    List<Long> ids = new ArrayList<>();
    ids.add(2L);
    ids.add(6L);
    ids.add(7L);
    dialog.setDatasetIds(ids);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SamplesAnalysisDialog.HEADER, ids.size()),
        dialog.getHeaderTitle());
  }
}
