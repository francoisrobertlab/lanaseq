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

import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAVED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Assay;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TestTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetDialogItTest extends AbstractTestBenchTestCase {
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  private String tag1 = "mnase";
  private String tag2 = "ip";
  private Protocol protocol;
  private Assay assay = Assay.MNASE_SEQ;
  private SampleType type = SampleType.IMMUNO_PRECIPITATION;
  private String target = "polr3a";
  private String strain = "yFR20";
  private String strainDescription = "WT";
  private String treatment = "37C";
  private String sampleId = "FR3";
  private String sampleReplicate = "R3";

  @Before
  public void beforeTest() {
    protocol = protocolRepository.findById(1L).get();
  }

  private void open() {
    openView(VIEW_NAME);
  }

  private void fill(DatasetDialogElement dialog) {
    ButtonElement tag = dialog.tags().tag("G24D");
    if (tag != null) {
      tag.click();
    }
    dialog.tags().newTag().setValue(tag1);
    dialog.tags().newTag().sendKeys(Keys.ENTER);
    dialog.tags().newTag().setValue(tag2);
    dialog.tags().newTag().sendKeys(Keys.ENTER);
    dialog.protocol().selectByText(protocol.getName());
    dialog.assay().selectByText(assay.getLabel(currentLocale()));
    dialog.type().selectByText(type.getLabel(currentLocale()));
    dialog.target().setValue(target);
    dialog.strain().setValue(strain);
    dialog.strainDescription().setValue(strainDescription);
    dialog.treatment().setValue(treatment);
    dialog.sampleId(0).setValue(sampleId);
    dialog.replicate(0).setValue(sampleReplicate);
  }

  private String name() {
    return assay.getLabel(Locale.ENGLISH) + "_" + type.getLabel(Locale.ENGLISH) + "_" + target + "_"
        + strain + "_" + strainDescription + "_" + treatment;
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.doubleClickDataset(0);
    DatasetDialogElement dialog = $(DatasetDialogElement.class).id(ID);
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.tags()).isPresent());
    assertTrue(optional(() -> dialog.protocol()).isPresent());
    assertTrue(optional(() -> dialog.assay()).isPresent());
    assertTrue(optional(() -> dialog.type()).isPresent());
    assertTrue(optional(() -> dialog.target()).isPresent());
    assertTrue(optional(() -> dialog.strain()).isPresent());
    assertTrue(optional(() -> dialog.strainDescription()).isPresent());
    assertTrue(optional(() -> dialog.treatment()).isPresent());
    assertTrue(optional(() -> dialog.samplesHeader()).isPresent());
    assertTrue(optional(() -> dialog.samples()).isPresent());
    assertTrue(optional(() -> dialog.addSample()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
  }

  @Test
  public void save_New() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.add().click();
    DatasetDialogElement dialog = $(DatasetDialogElement.class).id(ID);
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    String name =
        name() + "_" + sampleId + "_" + DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now());
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(DatasetDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    List<Dataset> datasets = repository.findByOwner(new User(3L));
    Dataset dataset =
        datasets.stream().filter(ex -> name.equals(ex.getName())).findFirst().orElse(null);
    assertNotNull(dataset);
    assertNotNull(dataset.getId());
    assertEquals(name, dataset.getName());
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains(tag1));
    assertTrue(dataset.getTags().contains(tag2));
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(dataset.getDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(dataset.getDate()));
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(1, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(sampleReplicate, sample.getReplicate());
    assertEquals(protocol.getId(), sample.getProtocol().getId());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
  }

  @Test
  public void save_Update() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.doubleClickDataset(0);
    DatasetDialogElement dialog = $(DatasetDialogElement.class).id(ID);
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    String name = name() + "_" + sampleId + "-JS2_"
        + DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.of(2018, 10, 22));
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(DatasetDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    Dataset dataset = repository.findById(2L).get();
    assertEquals(name, dataset.getName());
    assertEquals(3, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("chipseq"));
    assertTrue(dataset.getTags().contains(tag1));
    assertTrue(dataset.getTags().contains(tag2));
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getDate());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(sampleReplicate, sample.getReplicate());
    assertEquals(protocol.getId(), sample.getProtocol().getId());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
    sample = dataset.getSamples().get(1);
    assertEquals("JS2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    assertEquals(protocol.getId(), sample.getProtocol().getId());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
  }

  @Test
  public void cancel() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.doubleClickDataset(0);
    DatasetDialogElement dialog = $(DatasetDialogElement.class).id(ID);
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    Dataset dataset = repository.findById(2L).get();
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("chipseq"));
    assertTrue(dataset.getTags().contains("G24D"));
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getDate());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals("JS1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    sample = dataset.getSamples().get(1);
    assertEquals("JS2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
  }
}
