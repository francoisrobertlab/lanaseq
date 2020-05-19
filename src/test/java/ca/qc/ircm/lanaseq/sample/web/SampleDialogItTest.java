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
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.SAVED;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Assay;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.Headless;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TestTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SampleDialogItTest extends AbstractTestBenchTestCase {
  @Autowired
  private SampleRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  private Protocol protocol;
  private Assay assay = Assay.MNASE_SEQ;
  private DatasetType type = DatasetType.IMMUNO_PRECIPITATION;
  private String target = "polr3a";
  private String strain = "yFR20";
  private String strainDescription = "WT";
  private String treatment = "37C";
  private String sampleId = "FR3";
  private String replicate = "R3";

  @Before
  public void beforeTest() {
    protocol = protocolRepository.findById(1L).get();
  }

  private void open() {
    openView(VIEW_NAME);
  }

  private void fill(SampleDialogElement dialog) {
    dialog.sampleId().setValue(sampleId);
    dialog.replicate().setValue(replicate);
    dialog.protocol().selectByText(protocol.getName());
    dialog.assay().selectByText(assay.getLabel(currentLocale()));
    dialog.type().selectByText(type.getLabel(currentLocale()));
    dialog.target().setValue(target);
    dialog.strain().setValue(strain);
    dialog.strainDescription().setValue(strainDescription);
    dialog.treatment().setValue(treatment);
  }

  private String name() {
    return sampleId + "_" + assay.getLabel(Locale.ENGLISH) + "_" + type.getLabel(Locale.ENGLISH)
        + "_" + target + "_" + strain + "_" + strainDescription + "_" + treatment + "_" + replicate;
  }

  @Test
  public void fieldsExistence_Add() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.add().click();
    SampleDialogElement dialog = $(SampleDialogElement.class).id(ID);
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.sampleId()).isPresent());
    assertTrue(optional(() -> dialog.replicate()).isPresent());
    assertTrue(optional(() -> dialog.protocol()).isPresent());
    assertTrue(optional(() -> dialog.assay()).isPresent());
    assertTrue(optional(() -> dialog.type()).isPresent());
    assertTrue(optional(() -> dialog.target()).isPresent());
    assertTrue(optional(() -> dialog.strain()).isPresent());
    assertTrue(optional(() -> dialog.strainDescription()).isPresent());
    assertTrue(optional(() -> dialog.treatment()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
    assertFalse(optional(() -> dialog.delete()).isPresent());
  }

  @Test
  public void fieldsExistence_Update() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.doubleClickSample(0);
    SampleDialogElement dialog = $(SampleDialogElement.class).id(ID);
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.sampleId()).isPresent());
    assertTrue(optional(() -> dialog.replicate()).isPresent());
    assertTrue(optional(() -> dialog.protocol()).isPresent());
    assertTrue(optional(() -> dialog.assay()).isPresent());
    assertTrue(optional(() -> dialog.type()).isPresent());
    assertTrue(optional(() -> dialog.target()).isPresent());
    assertTrue(optional(() -> dialog.strain()).isPresent());
    assertTrue(optional(() -> dialog.strainDescription()).isPresent());
    assertTrue(optional(() -> dialog.treatment()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
    assertFalse(optional(() -> dialog.delete()).isPresent());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  @Headless(false)
  public void fieldsExistence_Deletable() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.ownerFilter().setValue("benoit.coulombe@ircm.qc.ca");
    view.doubleClickSample(3);
    SampleDialogElement dialog = $(SampleDialogElement.class).id(ID);
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.sampleId()).isPresent());
    assertTrue(optional(() -> dialog.replicate()).isPresent());
    assertTrue(optional(() -> dialog.protocol()).isPresent());
    assertTrue(optional(() -> dialog.assay()).isPresent());
    assertTrue(optional(() -> dialog.type()).isPresent());
    assertTrue(optional(() -> dialog.target()).isPresent());
    assertTrue(optional(() -> dialog.strain()).isPresent());
    assertTrue(optional(() -> dialog.strainDescription()).isPresent());
    assertTrue(optional(() -> dialog.treatment()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
    assertTrue(optional(() -> dialog.delete()).isPresent());
  }

  @Test
  public void save_New() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.add().click();
    SampleDialogElement dialog = $(SampleDialogElement.class).id(ID);
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    String name = name() + "_" + DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now());
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(SampleDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    List<Sample> samples = repository.findByOwner(new User(3L));
    Sample sample =
        samples.stream().filter(ex -> name.equals(ex.getName())).findFirst().orElse(null);
    assertNotNull(sample);
    assertNotNull(sample.getId());
    assertEquals(name, sample.getName());
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(sample.getDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(sample.getDate()));
    assertEquals((Long) 3L, sample.getOwner().getId());
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(replicate, sample.getReplicate());
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
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.doubleClickSample(0);
    SampleDialogElement dialog = $(SampleDialogElement.class).id(ID);
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    String name =
        name() + "_" + DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.of(2018, 10, 22));
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(SampleDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    Sample sample = repository.findById(4L).get();
    assertEquals(name, sample.getName());
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 50, 20), sample.getDate());
    assertEquals((Long) 3L, sample.getOwner().getId());
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(replicate, sample.getReplicate());
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
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.doubleClickSample(0);
    SampleDialogElement dialog = $(SampleDialogElement.class).id(ID);
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    Sample sample = repository.findById(4L).get();
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 50, 20), sample.getDate());
    assertEquals((Long) 3L, sample.getOwner().getId());
    assertEquals("JS1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void delete() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.ownerFilter().setValue("benoit.coulombe@ircm.qc.ca");
    view.doubleClickSample(3);
    SampleDialogElement dialog = $(SampleDialogElement.class).id(ID);
    Sample sample = repository.findById(9L).get();
    String name = sample.getName();

    TestTransaction.flagForCommit();
    dialog.delete().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(SampleDialog.class);
    assertEquals(resources.message(DELETED, name), notification.getText());
    assertFalse(repository.findById(9L).isPresent());
  }
}
