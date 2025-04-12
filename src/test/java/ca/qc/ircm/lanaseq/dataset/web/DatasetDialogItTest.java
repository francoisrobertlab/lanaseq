package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAVED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
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
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.BrowserTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
public class DatasetDialogItTest extends AbstractBrowserTestCase {

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
  private final String namePrefix = "ChIPseq_Spt16_yFR101_G24D_JS1-JS2";
  private final String keyword1 = "mnase";
  private final String keyword2 = "ip";
  private final String filename = "OF_20241120_ROB_01";
  private final String note = "test note\nsecond line";
  private final LocalDate date = LocalDate.of(2020, 7, 20);

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

  @BrowserTest
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

  @BrowserTest
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
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name}, currentLocale()),
        notification.getText());
    dataset = repository.findById(2L).orElseThrow();
    Assertions.assertEquals(name, dataset.getName());
    Assertions.assertEquals(3, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains(keyword1));
    assertTrue(dataset.getKeywords().contains(keyword2));
    Assertions.assertEquals(2, dataset.getFilenames().size());
    assertTrue(dataset.getFilenames().contains("OF_20241118_ROB"));
    assertTrue(dataset.getFilenames().contains(filename));
    Assertions.assertEquals(note, dataset.getNote());
    Assertions.assertEquals(date, dataset.getDate());
    Assertions.assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getCreationDate());
    Assertions.assertEquals((Long) 3L, dataset.getOwner().getId());
    Assertions.assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    Assertions.assertEquals("JS1", sample.getSampleId());
    Assertions.assertEquals("R1", sample.getReplicate());
    Assertions.assertEquals(3L, sample.getProtocol().getId());
    Assertions.assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    Assertions.assertEquals("Spt16", sample.getTarget());
    Assertions.assertEquals("yFR101", sample.getStrain());
    Assertions.assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    sample = dataset.getSamples().get(1);
    Assertions.assertEquals("JS2", sample.getSampleId());
    Assertions.assertEquals("R2", sample.getReplicate());
    Assertions.assertEquals(3L, sample.getProtocol().getId());
    Assertions.assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    Assertions.assertEquals("Spt16", sample.getTarget());
    Assertions.assertEquals("yFR101", sample.getStrain());
    Assertions.assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    Assertions.assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1_20181208",
        repository.findById(6L).orElseThrow().getName());
    Thread.sleep(1000); // Allow time to apply changes to files.
    Path folder = configuration.getHome().folder(dataset);
    Path sampleFolder = configuration.getHome().folder(dataset.getSamples().get(0));
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(oldFolder));
    assertTrue(Files.exists(sampleFolder));
  }

  @BrowserTest
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
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name}, currentLocale()),
        notification.getText());
    Dataset dataset = repository.findById(2L).orElseThrow();
    Assertions.assertEquals(name, dataset.getName());
    Assertions.assertEquals(3, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertTrue(dataset.getKeywords().contains("G24D"));
    Assertions.assertEquals(1, dataset.getFilenames().size());
    assertTrue(dataset.getFilenames().contains("OF_20241118_ROB"));
    assertNull(dataset.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), dataset.getDate());
    Assertions.assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getCreationDate());
    Assertions.assertEquals((Long) 3L, dataset.getOwner().getId());
    Assertions.assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    Assertions.assertEquals((Long) 5L, sample.getId());
    Assertions.assertEquals("JS2", sample.getSampleId());
    Assertions.assertEquals("R2", sample.getReplicate());
    Assertions.assertEquals((Long) 3L, sample.getProtocol().getId());
    Assertions.assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    Assertions.assertEquals("Spt16", sample.getTarget());
    Assertions.assertEquals("yFR101", sample.getStrain());
    Assertions.assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    sample = dataset.getSamples().get(1);
    Assertions.assertEquals((Long) 4L, sample.getId());
    Assertions.assertEquals("JS1", sample.getSampleId());
    Assertions.assertEquals("R1", sample.getReplicate());
    Assertions.assertEquals((Long) 3L, sample.getProtocol().getId());
    Assertions.assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    Assertions.assertEquals("Spt16", sample.getTarget());
    Assertions.assertEquals("yFR101", sample.getStrain());
    Assertions.assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
  }

  @BrowserTest
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
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{dataset.getName()},
            currentLocale()), notification.getText());
    Assertions.assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1-JS2-JS1_20181022", dataset.getName());
    Assertions.assertEquals(3, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertTrue(dataset.getKeywords().contains("G24D"));
    Assertions.assertEquals(1, dataset.getFilenames().size());
    assertTrue(dataset.getFilenames().contains("OF_20241118_ROB"));
    assertNull(dataset.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), dataset.getDate());
    Assertions.assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getCreationDate());
    Assertions.assertEquals((Long) 3L, dataset.getOwner().getId());
    Assertions.assertEquals(3, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    Assertions.assertEquals((Long) 4L, sample.getId());
    Assertions.assertEquals("JS1", sample.getSampleId());
    Assertions.assertEquals("R1", sample.getReplicate());
    Assertions.assertEquals((Long) 3L, sample.getProtocol().getId());
    Assertions.assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    Assertions.assertEquals("Spt16", sample.getTarget());
    Assertions.assertEquals("yFR101", sample.getStrain());
    Assertions.assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    sample = dataset.getSamples().get(1);
    Assertions.assertEquals((Long) 5L, sample.getId());
    Assertions.assertEquals("JS2", sample.getSampleId());
    Assertions.assertEquals("R2", sample.getReplicate());
    Assertions.assertEquals((Long) 3L, sample.getProtocol().getId());
    Assertions.assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    Assertions.assertEquals("Spt16", sample.getTarget());
    Assertions.assertEquals("yFR101", sample.getStrain());
    Assertions.assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    sample = dataset.getSamples().get(2);
    Assertions.assertEquals((Long) 10L, sample.getId());
    Assertions.assertEquals("JS1", sample.getSampleId());
    Assertions.assertEquals("R1", sample.getReplicate());
    Assertions.assertEquals((Long) 3L, sample.getProtocol().getId());
    Assertions.assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    Assertions.assertEquals("Spt16", sample.getTarget());
    Assertions.assertEquals("yFR101", sample.getStrain());
    Assertions.assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 12, 10), sample.getDate());
    Thread.sleep(1000); // Allow time to apply changes to files.
    Path folder = configuration.getHome().folder(dataset);
    logger.debug("dataset folder {} exists {}", folder, Files.exists(folder));
    logger.debug("dataset old folder {} exists {}", oldFolder, Files.exists(oldFolder));
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(oldFolder));
  }

  @BrowserTest
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
    Assertions.assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022", dataset.getName());
    Assertions.assertEquals(3, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertTrue(dataset.getKeywords().contains("G24D"));
    Assertions.assertEquals(1, dataset.getFilenames().size());
    assertTrue(dataset.getFilenames().contains("OF_20241118_ROB"));
    assertNull(dataset.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), dataset.getDate());
    Assertions.assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getCreationDate());
    Assertions.assertEquals((Long) 3L, dataset.getOwner().getId());
    Assertions.assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    Assertions.assertEquals("JS1", sample.getSampleId());
    Assertions.assertEquals("R1", sample.getReplicate());
    Assertions.assertEquals((Long) 3L, sample.getProtocol().getId());
    Assertions.assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    Assertions.assertEquals("Spt16", sample.getTarget());
    Assertions.assertEquals("yFR101", sample.getStrain());
    Assertions.assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    sample = dataset.getSamples().get(1);
    Assertions.assertEquals("JS2", sample.getSampleId());
    Assertions.assertEquals("R2", sample.getReplicate());
    Assertions.assertEquals((Long) 3L, sample.getProtocol().getId());
    Assertions.assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    Assertions.assertEquals("Spt16", sample.getTarget());
    Assertions.assertEquals("yFR101", sample.getStrain());
    Assertions.assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getNote());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    assertNull(sample.getTreatment());
  }

  @BrowserTest
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
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + DELETED, new Object[]{name}, currentLocale()),
        notification.getText());
    assertFalse(repository.findById(4L).isPresent());
    Thread.sleep(1000); // Allow time to apply changes to files.
    assertFalse(Files.exists(folder));
  }
}
