package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAVED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.web.SelectSampleDialogElement;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link DatasetDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetDialogItTest extends AbstractTestBenchTestCase {
  private static final String MESSAGE_PREFIX = messagePrefix(DatasetDialog.class);
  private static final Logger logger = LoggerFactory.getLogger(DatasetDialogItTest.class);
  @TempDir
  Path temporaryFolder;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private SampleRepository sampleRepository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private AppConfiguration configuration;
  @Autowired
  private MessageSource messageSource;
  private String namePrefix = "ChIPseq_Spt16_yFR101_G24D_JS1-JS2";
  private String keyword1 = "mnase";
  private String keyword2 = "ip";
  private String filename = "OF_20241120_ROB_01";
  private String note = "test note\nsecond line";
  private LocalDate date = LocalDate.of(2020, 7, 20);

  @BeforeEach
  public void beforeTest() throws Throwable {
    setHome(Files.createDirectory(temporaryFolder.resolve("home")));
  }

  private void open() {
    openView(VIEW_NAME);
  }

  private void fill(DatasetDialogElement dialog) {
    dialog.keywords().deselectByText("G24D");
    dialog.namePrefix().setValue(namePrefix);
    dialog.keywords().selectByText(keyword1);
    dialog.keywords().selectByText(keyword2);
    dialog.filenames().sendKeys(filename + Keys.RETURN);
    dialog.note().setValue(note);
    dialog.date().setDate(date);
  }

  @Test
  public void fieldsExistence() {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(0);
    view.edit().click();
    DatasetDialogElement dialog = view.dialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::namePrefix).isPresent());
    assertTrue(optional(dialog::generateName).isPresent());
    assertTrue(optional(dialog::keywords).isPresent());
    assertTrue(optional(dialog::filenames).isPresent());
    assertTrue(optional(dialog::protocol).isPresent());
    assertTrue(optional(dialog::assay).isPresent());
    assertTrue(optional(dialog::type).isPresent());
    assertTrue(optional(dialog::target).isPresent());
    assertTrue(optional(dialog::strain).isPresent());
    assertTrue(optional(dialog::strainDescription).isPresent());
    assertTrue(optional(dialog::treatment).isPresent());
    assertTrue(optional(dialog::note).isPresent());
    assertTrue(optional(dialog::date).isPresent());
    assertTrue(optional(dialog::samplesHeader).isPresent());
    assertTrue(optional(dialog::samples).isPresent());
    assertTrue(optional(dialog::addSample).isPresent());
    assertFalse(optional(dialog::error).isPresent());
    assertTrue(optional(dialog::save).isPresent());
    assertTrue(optional(dialog::cancel).isPresent());
    assertTrue(optional(dialog::delete).isPresent());
  }

  @Test
  public void save_Update() throws Throwable {
    open();
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path oldFolder = configuration.getHome().folder(dataset);
    Files.createDirectories(oldFolder);
    Path oldSampleFolder = configuration.getHome().folder(dataset.getSamples().get(0));
    Files.createDirectories(oldSampleFolder);
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(3);
    view.edit().click();
    DatasetDialogElement dialog = view.dialog();
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    String name = namePrefix + "_" + DateTimeFormatter.BASIC_ISO_DATE.format(date);
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[] { name }, currentLocale()),
        notification.getText());
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
    Thread.sleep(1000); // Allow time to apply changes to files.
    Path folder = configuration.getHome().folder(dataset);
    Path sampleFolder = configuration.getHome().folder(dataset.getSamples().get(0));
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(oldFolder));
    assertTrue(Files.exists(sampleFolder));
  }

  @Test
  @Disabled("Drag and drop function moves to random element instead of the right location")
  public void save_ReorderSamples() {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(3);
    view.edit().click();
    DatasetDialogElement dialog = view.dialog();
    Actions dragAndDrop = new Actions(dialog.getDriver());
    WebElement drag = dialog.samples().getCell(0, 2);
    WebElement drop = dialog.samples().getCell(1, 2);
    int yoffset = drop.getLocation().y - drag.getLocation().y;
    dragAndDrop.dragAndDropBy(drop, 0, yoffset).perform();

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    String name = namePrefix + "_" + DateTimeFormatter.BASIC_ISO_DATE.format(date);
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[] { name }, currentLocale()),
        notification.getText());
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
    open();
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path oldFolder = configuration.getHome().folder(dataset);
    Files.createDirectories(oldFolder);
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(3);
    view.edit().click();
    DatasetDialogElement dialog = view.dialog();
    dialog.addSample().click();
    SelectSampleDialogElement selectSampleDialog = dialog.selectSampleDialog();
    selectSampleDialog.samples().doubleClick(2);
    dialog.generateName().click();

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    dataset = repository.findById(2L).orElseThrow();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED,
        new Object[] { dataset.getName() }, currentLocale()), notification.getText());
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
    Thread.sleep(1000); // Allow time to apply changes to files.
    Path folder = configuration.getHome().folder(dataset);
    logger.debug("dataset folder {} exists {}", folder, Files.exists(folder));
    logger.debug("dataset old folder {} exists {}", oldFolder, Files.exists(oldFolder));
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(oldFolder));
  }

  @Test
  public void cancel() {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(0);
    view.edit().click();
    DatasetDialogElement dialog = view.dialog();
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
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
    open();
    Dataset dataset = repository.findById(4L).get();
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().ownerFilter().setValue("benoit.coulombe@ircm.qc.ca");
    view.datasets().select(1);
    view.edit().click();
    DatasetDialogElement dialog = view.dialog();
    final String name = dataset.getName();

    TestTransaction.flagForCommit();
    dialog.delete().click();
    dialog.confirm().getConfirmButton().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + DELETED, new Object[] { name }, currentLocale()),
        notification.getText());
    assertFalse(repository.findById(4L).isPresent());
    Thread.sleep(1000); // Allow time to apply changes to files.
    assertFalse(Files.exists(folder));
  }
}
