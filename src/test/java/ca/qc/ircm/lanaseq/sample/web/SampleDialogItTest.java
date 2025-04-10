package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.DELETED;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.SAVED;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchBrowser;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.BrowserTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link SampleDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SampleDialogItTest extends AbstractTestBenchBrowser {

  private static final String MESSAGE_PREFIX = messagePrefix(SampleDialog.class);
  @TempDir
  Path temporaryFolder;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private AppConfiguration configuration;
  @Autowired
  private MessageSource messageSource;
  private Protocol protocol;
  private final LocalDate date = LocalDate.of(2020, 7, 20);
  private final String assay = "RNA-seq";
  private final String type = "IP";
  private final String target = "polr3a";
  private final String strain = "yFR20";
  private final String strainDescription = "WT";
  private final String treatment = "37C";
  private final String sampleId = "FR3";
  private final String replicate = "R3";
  private final String keyword1 = "mnase";
  private final String keyword2 = "ip";
  private final String filename = "OF_20241120_ROB_01";
  private final String note = "test note\nsecond line";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() throws Throwable {
    protocol = protocolRepository.findById(1L).orElseThrow();
    setHome(Files.createDirectory(temporaryFolder.resolve("home")));
  }

  private void open() {
    openView(VIEW_NAME);
  }

  private void fill(SampleDialogElement dialog) {
    dialog.date().setDate(date);
    dialog.sampleId().setValue(sampleId);
    dialog.replicate().setValue(replicate);
    dialog.protocol().selectByText(protocol.getName());
    dialog.assay().clear();
    dialog.assay().sendKeys(assay);
    dialog.assay().sendKeys(Keys.TAB);
    dialog.type().openPopup(); // Causes delay to make unit test work.
    dialog.type().selectByText(type);
    dialog.type().closePopup(); // Causes delay to make unit test work.
    dialog.target().setValue(target);
    dialog.strain().setValue(strain);
    dialog.strainDescription().setValue(strainDescription);
    dialog.treatment().setValue(treatment);
    dialog.keywords().deselectByText("G24D");
    dialog.keywords().selectByText(keyword1);
    dialog.keywords().selectByText(keyword2);
    dialog.filenames().sendKeys(filename + Keys.RETURN);
    dialog.note().setValue(note);
  }

  private String name() {
    return sampleId + "_" + assay.replaceAll("[^\\w]", "") + "_" + type.replaceAll("[^\\w]", "")
        + "_" + target + "_" + strain + "_" + strainDescription + "_" + treatment + "_" + replicate;
  }

  @BrowserTest
  public void fieldsExistence_Add() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.add().click();
    SampleDialogElement dialog = view.dialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::date).isPresent());
    assertTrue(optional(dialog::sampleId).isPresent());
    assertTrue(optional(dialog::replicate).isPresent());
    assertTrue(optional(dialog::protocol).isPresent());
    assertTrue(optional(dialog::assay).isPresent());
    assertTrue(optional(dialog::type).isPresent());
    assertTrue(optional(dialog::target).isPresent());
    assertTrue(optional(dialog::strain).isPresent());
    assertTrue(optional(dialog::strainDescription).isPresent());
    assertTrue(optional(dialog::treatment).isPresent());
    assertTrue(optional(dialog::keywords).isPresent());
    assertTrue(optional(dialog::filenames).isPresent());
    assertTrue(optional(dialog::note).isPresent());
    assertTrue(optional(dialog::save).isPresent());
    assertTrue(optional(dialog::cancel).isPresent());
    assertFalse(optional(dialog::delete).isPresent());
    assertTrue(optional(dialog::confirm).isPresent());
  }

  @BrowserTest
  public void fieldsExistence_Update() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(0);
    view.edit().click();
    SampleDialogElement dialog = view.dialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::date).isPresent());
    assertTrue(optional(dialog::sampleId).isPresent());
    assertTrue(optional(dialog::replicate).isPresent());
    assertTrue(optional(dialog::protocol).isPresent());
    assertTrue(optional(dialog::assay).isPresent());
    assertTrue(optional(dialog::type).isPresent());
    assertTrue(optional(dialog::target).isPresent());
    assertTrue(optional(dialog::strain).isPresent());
    assertTrue(optional(dialog::strainDescription).isPresent());
    assertTrue(optional(dialog::treatment).isPresent());
    assertTrue(optional(dialog::keywords).isPresent());
    assertTrue(optional(dialog::filenames).isPresent());
    assertTrue(optional(dialog::note).isPresent());
    assertTrue(optional(dialog::save).isPresent());
    assertTrue(optional(dialog::cancel).isPresent());
    assertFalse(optional(dialog::delete).isPresent());
    assertTrue(optional(dialog::confirm).isPresent());
  }

  @BrowserTest
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void fieldsExistence_Deletable() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().ownerFilter().setValue("benoit.coulombe@ircm.qc.ca");
    view.samples().select(0);
    view.edit().click();
    SampleDialogElement dialog = view.dialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::date).isPresent());
    assertTrue(optional(dialog::sampleId).isPresent());
    assertTrue(optional(dialog::replicate).isPresent());
    assertTrue(optional(dialog::protocol).isPresent());
    assertTrue(optional(dialog::assay).isPresent());
    assertTrue(optional(dialog::type).isPresent());
    assertTrue(optional(dialog::target).isPresent());
    assertTrue(optional(dialog::strain).isPresent());
    assertTrue(optional(dialog::strainDescription).isPresent());
    assertTrue(optional(dialog::treatment).isPresent());
    assertTrue(optional(dialog::keywords).isPresent());
    assertTrue(optional(dialog::filenames).isPresent());
    assertTrue(optional(dialog::note).isPresent());
    assertTrue(optional(dialog::save).isPresent());
    assertTrue(optional(dialog::cancel).isPresent());
    assertTrue(optional(dialog::delete).isPresent());
    assertTrue(optional(dialog::confirm).isPresent());
  }

  @BrowserTest
  public void save_New() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.add().click();
    SampleDialogElement dialog = view.dialog();
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    String name = name() + "_20200720";
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name}, currentLocale()),
        notification.getText());
    List<Sample> samples = repository.findByOwner(new User(3L));
    Sample sample = samples.stream().filter(ex -> name.equals(ex.getName())).findFirst()
        .orElseThrow();
    assertNotNull(sample);
    assertNotEquals(0, sample.getId());
    Assertions.assertEquals(name, sample.getName());
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(sample.getCreationDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(sample.getCreationDate()));
    Assertions.assertEquals((Long) 3L, sample.getOwner().getId());
    Assertions.assertEquals(date, sample.getDate());
    Assertions.assertEquals(sampleId, sample.getSampleId());
    Assertions.assertEquals(replicate, sample.getReplicate());
    Assertions.assertEquals(protocol.getId(), sample.getProtocol().getId());
    Assertions.assertEquals(assay, sample.getAssay());
    Assertions.assertEquals(type, sample.getType());
    Assertions.assertEquals(target, sample.getTarget());
    Assertions.assertEquals(strain, sample.getStrain());
    Assertions.assertEquals(strainDescription, sample.getStrainDescription());
    Assertions.assertEquals(treatment, sample.getTreatment());
    Assertions.assertEquals(2, sample.getKeywords().size());
    assertTrue(sample.getKeywords().contains(keyword1));
    assertTrue(sample.getKeywords().contains(keyword2));
    Assertions.assertEquals(1, sample.getFilenames().size());
    assertTrue(sample.getFilenames().contains(filename));
    Assertions.assertEquals(note, sample.getNote());
    Assertions.assertEquals(5, view.samples().getRowCount());
  }

  @BrowserTest
  public void save_Update() throws Throwable {
    open();
    Sample sample = repository.findById(4L).orElseThrow();
    Path oldFolder = configuration.getHome().folder(sample);
    Files.createDirectories(oldFolder);
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(view.samples().name(2).startsWith("JS1") ? 2 : 3);
    view.edit().click();
    SampleDialogElement dialog = view.dialog();
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    String name = name() + "_20200720";
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name}, currentLocale()),
        notification.getText());
    sample = repository.findById(4L).orElseThrow();
    Assertions.assertEquals(name, sample.getName());
    Assertions.assertEquals(LocalDateTime.of(2018, 10, 22, 9, 50, 20), sample.getCreationDate());
    Assertions.assertEquals((Long) 3L, sample.getOwner().getId());
    Assertions.assertEquals(date, sample.getDate());
    Assertions.assertEquals(sampleId, sample.getSampleId());
    Assertions.assertEquals(replicate, sample.getReplicate());
    Assertions.assertEquals(protocol.getId(), sample.getProtocol().getId());
    Assertions.assertEquals(assay, sample.getAssay());
    Assertions.assertEquals(type, sample.getType());
    Assertions.assertEquals(target, sample.getTarget());
    Assertions.assertEquals(strain, sample.getStrain());
    Assertions.assertEquals(strainDescription, sample.getStrainDescription());
    Assertions.assertEquals(treatment, sample.getTreatment());
    Assertions.assertEquals(3, sample.getKeywords().size());
    assertTrue(sample.getKeywords().contains("chipseq"));
    assertTrue(sample.getKeywords().contains(keyword1));
    assertTrue(sample.getKeywords().contains(keyword2));
    Assertions.assertEquals(2, sample.getFilenames().size());
    assertTrue(sample.getFilenames().contains("OF_20241118_ROB_01"));
    assertTrue(sample.getFilenames().contains(filename));
    Assertions.assertEquals(note, sample.getNote());
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    Assertions.assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022", dataset.getName());
    dataset = datasetRepository.findById(6L).orElseThrow();
    Assertions.assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1_20181208", dataset.getName());
    Thread.sleep(1000); // Allow time to apply changes to files.
    Path folder = configuration.getHome().folder(sample);
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(oldFolder));
    Assertions.assertEquals(4, view.samples().getRowCount());
    Assertions.assertEquals(name, view.samples().name(0));
  }

  @BrowserTest
  public void cancel() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(0);
    view.edit().click();
    SampleDialogElement dialog = view.dialog();
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    Sample sample = repository.findById(4L).orElseThrow();
    Assertions.assertEquals(LocalDateTime.of(2018, 10, 22, 9, 50, 20), sample.getCreationDate());
    Assertions.assertEquals((Long) 3L, sample.getOwner().getId());
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    Assertions.assertEquals("JS1", sample.getSampleId());
    Assertions.assertEquals("R1", sample.getReplicate());
    Assertions.assertEquals((Long) 3L, sample.getProtocol().getId());
    Assertions.assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    Assertions.assertEquals("Spt16", sample.getTarget());
    Assertions.assertEquals("yFR101", sample.getStrain());
    Assertions.assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    Assertions.assertEquals(3, sample.getKeywords().size());
    assertTrue(sample.getKeywords().contains("chipseq"));
    assertTrue(sample.getKeywords().contains("ip"));
    assertTrue(sample.getKeywords().contains("G24D"));
    Assertions.assertEquals(1, sample.getFilenames().size());
    assertTrue(sample.getFilenames().contains("OF_20241118_ROB_01"));
    assertNull(sample.getNote());
    Assertions.assertEquals(4, view.samples().getRowCount());
  }

  @BrowserTest
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void delete() throws Throwable {
    open();
    Sample sample = repository.findById(9L).get();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().ownerFilter().setValue("benoit.coulombe@ircm.qc.ca");
    view.samples().select(0);
    view.edit().click();
    SampleDialogElement dialog = view.dialog();
    final String name = sample.getName();

    TestTransaction.flagForCommit();
    dialog.delete().click();
    dialog.confirm().getConfirmButton().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + DELETED, new Object[]{name}, currentLocale()),
        notification.getText());
    assertFalse(repository.findById(9L).isPresent());
    Thread.sleep(1000); // Allow time to apply changes to files.
    assertFalse(Files.exists(folder));
    Assertions.assertEquals(3, view.samples().getRowCount());
  }
}
