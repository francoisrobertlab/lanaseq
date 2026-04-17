package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAVED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBoxBase.CustomValueSetEvent;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import jakarta.persistence.EntityManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * Integration tests for {@link DatasetDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetDialogIT extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(DatasetDialog.class);
  private static final Logger logger = LoggerFactory.getLogger(DatasetDialogIT.class);
  @MockitoSpyBean
  private DatasetService datasetService;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private AppConfiguration configuration;
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private EntityManager entityManager;
  private final String namePrefix = "ChIPseq_Spt16_yFR101_G24D_JS1-JS2";
  private final String keyword1 = "mnase";
  private final String keyword2 = "ip";
  private final String filename = "OF_20241120_ROB_01";
  private final String note = "test note\nsecond line";
  private final LocalDate date = LocalDate.of(2020, 7, 20);

  private void fill(DatasetDialog dialog) {
    test(dialog.namePrefix).setValue(namePrefix);
    Set<String> keywordsSelection = new HashSet<>(test(dialog.keywords).getSelected());
    keywordsSelection.remove("G24D");
    keywordsSelection.add(keyword1);
    keywordsSelection.add(keyword2);
    test(dialog.keywords).selectItem(keywordsSelection.toArray(new String[0]));
    fireEvent(dialog.filenames, new CustomValueSetEvent<>(dialog.filenames, false, filename));
    test(dialog.note).setValue(note);
    test(dialog.date).setValue(date);
  }

  private void detachOnServiceGet() {
    when(datasetService.get(anyLong())).then(a -> {
      @SuppressWarnings("unchecked") Optional<Dataset> optionalDataset = (Optional<Dataset>) a.callRealMethod();
      optionalDataset.ifPresent(d -> entityManager.detach(d));
      return optionalDataset;
    });
  }

  @Test
  public void save_Update() throws Throwable {
    detachOnServiceGet();
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path oldFolder = configuration.getHome().folder(dataset);
    Files.createDirectories(oldFolder);
    Path oldSampleFolder = configuration.getHome().folder(dataset.getSamples().get(0));
    Files.createDirectories(oldSampleFolder);
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.edit).click();
    DatasetDialog dialog = $(DatasetDialog.class).first();
    fill(dialog);

    test(dialog.save).click();

    String name = namePrefix + "_" + DateTimeFormatter.BASIC_ISO_DATE.format(date);
    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name},
        UI.getCurrent().getLocale()), test(notification).getText());
    dataset = repository.findById(2L).orElseThrow();
    assertEquals(name, dataset.getName());
    assertEquals(3, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains(keyword1));
    assertTrue(dataset.getKeywords().contains(keyword2));
    assertEquals(2, dataset.getFilenames().size());
    assertTrue(dataset.getFilenames().contains("OF_20241118_ROB"));
    assertTrue(dataset.getFilenames().contains(filename));
    assertEquals(note, dataset.getNote());
    assertEquals(date, dataset.getDate());
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getCreationDate());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals("JS1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals(3L, sample.getProtocol().getId());
    assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    sample = dataset.getSamples().get(1);
    assertEquals("JS2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    assertEquals(3L, sample.getProtocol().getId());
    assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1_20181208",
        repository.findById(6L).orElseThrow().getName());
    Path folder = configuration.getHome().folder(dataset);
    Path sampleFolder = configuration.getHome().folder(dataset.getSamples().get(0));
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(oldFolder));
    assertTrue(Files.exists(sampleFolder));
  }

  @Test
  public void save_ReorderSamples() {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.edit).click();
    DatasetDialog dialog = $(DatasetDialog.class).first();
    List<Sample> samples = new ArrayList<>(dialog.samples.getListDataView().getItems().toList());
    Collections.reverse(samples);
    dialog.samples.setItems(samples);

    test(dialog.save).click();

    String name = "ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022";
    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name},
        UI.getCurrent().getLocale()), test(notification).getText());
    Dataset dataset = repository.findById(2L).orElseThrow();
    assertEquals(name, dataset.getName());
    assertEquals(3, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertTrue(dataset.getKeywords().contains("G24D"));
    assertEquals(1, dataset.getFilenames().size());
    assertTrue(dataset.getFilenames().contains("OF_20241118_ROB"));
    assertNull(dataset.getNote());
    assertEquals(LocalDate.of(2018, 10, 22), dataset.getDate());
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getCreationDate());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals((Long) 5L, sample.getId());
    assertEquals("JS2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    sample = dataset.getSamples().get(1);
    assertEquals((Long) 4L, sample.getId());
    assertEquals("JS1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
  }

  @Test
  public void addSample() throws Throwable {
    detachOnServiceGet();
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path oldFolder = configuration.getHome().folder(dataset);
    Files.createDirectories(oldFolder);
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.edit).click();
    DatasetDialog dialog = $(DatasetDialog.class).first();
    test(dialog.addSample).click();
    SelectSampleDialog selectSampleDialog = $(SelectSampleDialog.class).first();
    @SuppressWarnings("unchecked") Grid<Sample> samplesGrid = test(selectSampleDialog).find(
        Grid.class).first();
    test(samplesGrid).doubleClickRow(2);
    test(dialog.generateName).click();

    test(dialog.save).click();

    Notification notification = $(Notification.class).first();
    dataset = repository.findById(2L).orElseThrow();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{dataset.getName()},
        UI.getCurrent().getLocale()), test(notification).getText());
    assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1-JS2-JS1_20181022", dataset.getName());
    assertEquals(3, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertTrue(dataset.getKeywords().contains("G24D"));
    assertEquals(1, dataset.getFilenames().size());
    assertTrue(dataset.getFilenames().contains("OF_20241118_ROB"));
    assertNull(dataset.getNote());
    assertEquals(LocalDate.of(2018, 10, 22), dataset.getDate());
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getCreationDate());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(3, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals((Long) 4L, sample.getId());
    assertEquals("JS1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    sample = dataset.getSamples().get(1);
    assertEquals((Long) 5L, sample.getId());
    assertEquals("JS2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    sample = dataset.getSamples().get(2);
    assertEquals((Long) 10L, sample.getId());
    assertEquals("JS1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    assertEquals(LocalDate.of(2018, 12, 10), sample.getDate());
    Path folder = configuration.getHome().folder(dataset);
    logger.debug("dataset folder {} exists {}", folder, Files.exists(folder));
    logger.debug("dataset old folder {} exists {}", oldFolder, Files.exists(oldFolder));
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(oldFolder));
  }

  @Test
  public void cancel() {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(0);
    test(view.edit).click();
    DatasetDialog dialog = $(DatasetDialog.class).first();
    fill(dialog);

    test(dialog.cancel).click();

    assertFalse($(Notification.class).exists());
    Dataset dataset = repository.findById(2L).orElseThrow();
    assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022", dataset.getName());
    assertEquals(3, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertTrue(dataset.getKeywords().contains("G24D"));
    assertEquals(1, dataset.getFilenames().size());
    assertTrue(dataset.getFilenames().contains("OF_20241118_ROB"));
    assertNull(dataset.getNote());
    assertEquals(LocalDate.of(2018, 10, 22), dataset.getDate());
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getCreationDate());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals("JS1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    sample = dataset.getSamples().get(1);
    assertEquals("JS2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getNote());
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    assertNull(sample.getTreatment());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void delete() throws Throwable {
    Dataset dataset = repository.findById(4L).get();
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets.ownerFilter).setValue("benoit.coulombe@ircm.qc.ca");
    test(view.datasets).select(1);
    test(view.edit).click();
    DatasetDialog dialog = $(DatasetDialog.class).first();
    final String name = dataset.getName();

    test(dialog.delete).click();
    test($(ConfirmDialog.class).first()).confirm();

    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + DELETED, new Object[]{name},
        UI.getCurrent().getLocale()), test(notification).getText());
    assertFalse(repository.findById(4L).isPresent());
    Thread.sleep(1000); // Allow time to apply changes to files.
    assertFalse(Files.exists(folder));
  }
}
