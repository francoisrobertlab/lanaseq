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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.DELETED;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.SAVED;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Assay;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link SampleDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SampleDialogItTest extends AbstractTestBenchTestCase {
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
  private Protocol protocol;
  private LocalDate date = LocalDate.of(2020, 07, 20);
  private Assay assay = Assay.MNASE_SEQ;
  private SampleType type = SampleType.IMMUNO_PRECIPITATION;
  private String target = "polr3a";
  private String strain = "yFR20";
  private String strainDescription = "WT";
  private String treatment = "37C";
  private String sampleId = "FR3";
  private String replicate = "R3";
  private String note = "test note\nsecond line";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() throws Throwable {
    protocol = protocolRepository.findById(1L).get();
    setHome(Files.createDirectory(temporaryFolder.resolve("home")));
  }

  private void open() {
    openView(VIEW_NAME);
  }

  private void fill(SampleDialogElement dialog) throws InterruptedException {
    dialog.date().setDate(date);
    dialog.sampleId().setValue(sampleId);
    dialog.replicate().setValue(replicate);
    dialog.protocol().selectByText(protocol.getName());
    dialog.assay().openPopup(); // Causes delay to make unit test work.
    dialog.assay().selectByText(assay.getLabel(currentLocale()));
    dialog.assay().closePopup(); // Causes delay to make unit test work.
    dialog.type().openPopup(); // Causes delay to make unit test work.
    dialog.type().selectByText(type.getLabel(currentLocale()));
    dialog.type().closePopup(); // Causes delay to make unit test work.
    dialog.target().setValue(target);
    dialog.strain().setValue(strain);
    dialog.strainDescription().setValue(strainDescription);
    dialog.treatment().setValue(treatment);
    dialog.note().setValue(note);
  }

  private String name() {
    return sampleId + "_" + assay.getLabel(Locale.ENGLISH).replaceAll("[^\\w]", "") + "_"
        + type.getLabel(Locale.ENGLISH) + "_" + target + "_" + strain + "_" + strainDescription
        + "_" + treatment + "_" + replicate;
  }

  @Test
  public void fieldsExistence_Add() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.add().click();
    SampleDialogElement dialog = view.dialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.date()).isPresent());
    assertTrue(optional(() -> dialog.sampleId()).isPresent());
    assertTrue(optional(() -> dialog.replicate()).isPresent());
    assertTrue(optional(() -> dialog.protocol()).isPresent());
    assertTrue(optional(() -> dialog.assay()).isPresent());
    assertTrue(optional(() -> dialog.type()).isPresent());
    assertTrue(optional(() -> dialog.target()).isPresent());
    assertTrue(optional(() -> dialog.strain()).isPresent());
    assertTrue(optional(() -> dialog.strainDescription()).isPresent());
    assertTrue(optional(() -> dialog.treatment()).isPresent());
    assertTrue(optional(() -> dialog.note()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
    assertFalse(optional(() -> dialog.delete()).isPresent());
    assertTrue(optional(() -> dialog.confirm()).isPresent());
  }

  @Test
  public void fieldsExistence_Update() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.samples().edit(0).click();
    SampleDialogElement dialog = view.dialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.date()).isPresent());
    assertTrue(optional(() -> dialog.sampleId()).isPresent());
    assertTrue(optional(() -> dialog.replicate()).isPresent());
    assertTrue(optional(() -> dialog.protocol()).isPresent());
    assertTrue(optional(() -> dialog.assay()).isPresent());
    assertTrue(optional(() -> dialog.type()).isPresent());
    assertTrue(optional(() -> dialog.target()).isPresent());
    assertTrue(optional(() -> dialog.strain()).isPresent());
    assertTrue(optional(() -> dialog.strainDescription()).isPresent());
    assertTrue(optional(() -> dialog.treatment()).isPresent());
    assertTrue(optional(() -> dialog.note()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
    assertFalse(optional(() -> dialog.delete()).isPresent());
    assertTrue(optional(() -> dialog.confirm()).isPresent());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void fieldsExistence_Deletable() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.samples().ownerFilter().setValue("benoit.coulombe@ircm.qc.ca");
    view.samples().edit(0).click();
    SampleDialogElement dialog = view.dialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.date()).isPresent());
    assertTrue(optional(() -> dialog.sampleId()).isPresent());
    assertTrue(optional(() -> dialog.replicate()).isPresent());
    assertTrue(optional(() -> dialog.protocol()).isPresent());
    assertTrue(optional(() -> dialog.assay()).isPresent());
    assertTrue(optional(() -> dialog.type()).isPresent());
    assertTrue(optional(() -> dialog.target()).isPresent());
    assertTrue(optional(() -> dialog.strain()).isPresent());
    assertTrue(optional(() -> dialog.strainDescription()).isPresent());
    assertTrue(optional(() -> dialog.treatment()).isPresent());
    assertTrue(optional(() -> dialog.note()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
    assertTrue(optional(() -> dialog.delete()).isPresent());
    assertTrue(optional(() -> dialog.confirm()).isPresent());
  }

  @Test
  public void save_New() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.add().click();
    SampleDialogElement dialog = view.dialog();
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    String name = name() + "_20200720";
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(SampleDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    List<Sample> samples = repository.findByOwner(new User(3L));
    Sample sample =
        samples.stream().filter(ex -> name.equals(ex.getName())).findFirst().orElse(null);
    assertNotNull(sample);
    assertNotNull(sample.getId());
    assertEquals(name, sample.getName());
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(sample.getCreationDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(sample.getCreationDate()));
    assertEquals((Long) 3L, sample.getOwner().getId());
    assertEquals(date, sample.getDate());
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(replicate, sample.getReplicate());
    assertEquals(protocol.getId(), sample.getProtocol().getId());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
    assertEquals(note, sample.getNote());
    assertEquals(5, view.samples().getRowCount());
  }

  @Test
  public void save_Update() throws Throwable {
    open();
    Sample sample = repository.findById(4L).get();
    Path oldFolder = configuration.folder(sample);
    Files.createDirectories(oldFolder);
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.samples().edit(view.samples().name(2).startsWith("JS1") ? 2 : 3).click();
    SampleDialogElement dialog = view.dialog();
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    String name = name() + "_20200720";
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(SampleDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    sample = repository.findById(4L).get();
    assertEquals(name, sample.getName());
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 50, 20), sample.getCreationDate());
    assertEquals((Long) 3L, sample.getOwner().getId());
    assertEquals(date, sample.getDate());
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(replicate, sample.getReplicate());
    assertEquals(protocol.getId(), sample.getProtocol().getId());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
    assertEquals(note, sample.getNote());
    Dataset dataset = datasetRepository.findById(2L).get();
    assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022", dataset.getName());
    dataset = datasetRepository.findById(6L).get();
    assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1_20181208", dataset.getName());
    Thread.sleep(1000); // Allow time to apply changes to files.
    Path folder = configuration.folder(sample);
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(oldFolder));
    assertEquals(4, view.samples().getRowCount());
    assertEquals(name, view.samples().name(0));
  }

  @Test
  public void cancel() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.samples().edit(0).click();
    SampleDialogElement dialog = view.dialog();
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    Sample sample = repository.findById(4L).get();
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 50, 20), sample.getCreationDate());
    assertEquals((Long) 3L, sample.getOwner().getId());
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    assertEquals("JS1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertNull(sample.getNote());
    assertEquals(4, view.samples().getRowCount());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void delete() throws Throwable {
    open();
    Sample sample = repository.findById(9L).get();
    Path folder = configuration.folder(sample);
    Files.createDirectories(folder);
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.samples().ownerFilter().setValue("benoit.coulombe@ircm.qc.ca");
    view.samples().edit(0).click();
    SampleDialogElement dialog = view.dialog();
    final String name = sample.getName();

    TestTransaction.flagForCommit();
    dialog.delete().click();
    dialog.confirm().getConfirmButton().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(SampleDialog.class);
    assertEquals(resources.message(DELETED, name), notification.getText());
    assertFalse(repository.findById(9L).isPresent());
    Thread.sleep(1000); // Allow time to apply changes to files.
    assertFalse(Files.exists(folder));
    assertEquals(3, view.samples().getRowCount());
  }
}
