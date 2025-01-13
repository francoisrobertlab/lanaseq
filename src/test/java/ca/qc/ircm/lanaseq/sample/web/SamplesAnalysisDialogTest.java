package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.CREATE_FOLDER;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.CREATE_FOLDER_EXCEPTION;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.ERRORS;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.FILENAME_PATTERNS;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.DataWithFiles;
import ca.qc.ircm.lanaseq.analysis.AnalysisService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
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
 * Tests for {@link SamplesAnalysisDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SamplesAnalysisDialogTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(SamplesAnalysisDialog.class);
  private SamplesAnalysisDialog dialog;
  @MockitoBean
  private SampleService service;
  @MockitoBean
  private AnalysisService analysisService;
  @MockitoBean
  private AppConfiguration configuration;
  @Autowired
  private SampleRepository repository;
  @Captor
  private ArgumentCaptor<Collection<String>> filenamePatternsCaptor;
  private Locale locale = Locale.ENGLISH;
  private List<Sample> samples = new ArrayList<>();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(service.get(anyLong())).then(
        i -> i.getArgument(0) != null ? repository.findById(i.getArgument(0)) : Optional.empty());
    @SuppressWarnings("unchecked")
    AppConfiguration.NetworkDrive<Collection<? extends DataWithFiles>> analysisFolder =
        mock(AppConfiguration.NetworkDrive.class);
    when(configuration.getAnalysis()).thenReturn(analysisFolder);
    samples.add(repository.findById(10L).get());
    samples.add(repository.findById(11L).get());
    UI.getCurrent().setLocale(locale);
    SamplesView view = navigate(SamplesView.class);
    view.samples.setItems(repository.findAll());
    samples.forEach(sample -> view.samples.select(sample));
    view.analyze.click();
    dialog = $(SamplesAnalysisDialog.class).first();
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
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, samples.size()),
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
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, samples.size()),
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
    String folder = "test/sample";
    when(configuration.getAnalysis().label(anyCollection(), anyBoolean())).thenReturn(folder);
    dialog.filenamePatterns.setValue("*.fastq", "*.bam");

    dialog.createFolder.click();

    verify(analysisService).copySamplesResources(eq(samples), filenamePatternsCaptor.capture());
    assertEquals(2, filenamePatternsCaptor.getValue().size());
    assertTrue(filenamePatternsCaptor.getValue().contains("*.fastq"));
    assertTrue(filenamePatternsCaptor.getValue().contains("*.bam"));
    verify(configuration.getAnalysis()).label(samples, false);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void createFolder_Unix() throws Throwable {
    String folder = "test/sample";
    when(configuration.getAnalysis().label(anyCollection(), anyBoolean())).thenReturn(folder);
    dialog.filenamePatterns.setValue("*.fastq");

    dialog.createFolder.click();

    verify(analysisService).copySamplesResources(eq(samples), filenamePatternsCaptor.capture());
    assertEquals(1, filenamePatternsCaptor.getValue().size());
    assertTrue(filenamePatternsCaptor.getValue().contains("*.fastq"));
    verify(configuration.getAnalysis()).label(samples, true);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void createFolder_Mac() throws Throwable {
    String folder = "test/sample";
    when(configuration.getAnalysis().label(anyCollection(), anyBoolean())).thenReturn(folder);
    dialog.filenamePatterns.setValue("*.fastq");

    dialog.createFolder.click();

    verify(analysisService).copySamplesResources(eq(samples), filenamePatternsCaptor.capture());
    assertEquals(1, filenamePatternsCaptor.getValue().size());
    assertTrue(filenamePatternsCaptor.getValue().contains("*.fastq"));
    verify(configuration.getAnalysis()).label(samples, true);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  public void createFolder_IoException() throws Throwable {
    doThrow(new IOException("test")).when(analysisService).copySamplesResources(anyCollection(),
        anyCollection());
    dialog.filenamePatterns.setValue("*.fastq");

    dialog.createFolder.click();

    verify(analysisService).copySamplesResources(eq(samples), filenamePatternsCaptor.capture());
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
  public void getSampleIds() {
    List<Long> samples = dialog.getSampleIds();
    assertEquals(this.samples.size(), samples.size());
    for (Sample sample : this.samples) {
      assertTrue(samples.contains(sample.getId()));
    }
  }

  @Test
  public void setSampleId() {
    Sample sample = repository.findById(10L).get();
    dialog.setSampleId(10L);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, sample.getName()),
        dialog.getHeaderTitle());
  }

  @Test
  public void setSampleIds() {
    List<Long> ids = new ArrayList<>();
    ids.add(4L);
    ids.add(5L);
    ids.add(10L);
    dialog.setSampleIds(ids);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, ids.size()),
        dialog.getHeaderTitle());
  }
}
