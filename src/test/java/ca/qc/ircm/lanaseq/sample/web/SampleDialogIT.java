package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.DELETED;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.SAVED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBoxBase.CustomValueSetEvent;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import jakarta.persistence.EntityManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * Integration tests for {@link SampleDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SampleDialogIT extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(SampleDialog.class);
  @MockitoSpyBean
  private SampleService service;
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
  @Autowired
  private EntityManager entityManager;
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
  }

  private void fill(SampleDialog dialog) {
    test(dialog.date).setValue(date);
    test(dialog.sampleId).setValue(sampleId);
    test(dialog.replicate).setValue(replicate);
    test(dialog.protocol).selectItem(protocol.getName());
    fireEvent(dialog.assay, new CustomValueSetEvent<>(dialog.assay, false, assay));
    test(dialog.type).selectItem(type);
    test(dialog.target).setValue(target);
    test(dialog.strain).setValue(strain);
    test(dialog.strainDescription).setValue(strainDescription);
    test(dialog.treatment).setValue(treatment);
    Set<String> keywordsSelection = new HashSet<>(test(dialog.keywords).getSelected());
    keywordsSelection.remove("G24D");
    keywordsSelection.add(keyword1);
    keywordsSelection.add(keyword2);
    test(dialog.keywords).selectItem(keywordsSelection.toArray(new String[0]));
    fireEvent(dialog.filenames, new CustomValueSetEvent<>(dialog.filenames, false, filename));
    test(dialog.note).setValue(note);
  }

  private String name() {
    return sampleId + "_" + assay.replaceAll("[^\\w]", "") + "_" + type.replaceAll("[^\\w]", "")
        + "_" + target + "_" + strain + "_" + strainDescription + "_" + treatment + "_" + replicate;
  }

  private void detachOnServiceGet() {
    when(service.get(anyLong())).then(a -> {
      @SuppressWarnings("unchecked") Optional<Sample> optionalSample = (Optional<Sample>) a.callRealMethod();
      optionalSample.ifPresent(d -> entityManager.detach(d));
      return optionalSample;
    });
  }

  private <T> int column(Grid<T> grid, Column<T> column) {
    return grid.getColumns().indexOf(column);
  }

  @Test
  public void save_New() {
    SamplesView view = navigate(SamplesView.class);
    test(view.add).click();
    SampleDialog dialog = $(SampleDialog.class).first();
    fill(dialog);

    test(dialog.save).click();

    String name = name() + "_20200720";
    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name},
        UI.getCurrent().getLocale()), test(notification).getText());
    List<Sample> samples = repository.findByOwner(new User(3L));
    Sample sample = samples.stream().filter(ex -> name.equals(ex.getName())).findFirst()
        .orElseThrow();
    assertNotNull(sample);
    assertNotEquals(0, sample.getId());
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
    assertEquals(2, sample.getKeywords().size());
    assertTrue(sample.getKeywords().contains(keyword1));
    assertTrue(sample.getKeywords().contains(keyword2));
    assertEquals(1, sample.getFilenames().size());
    assertTrue(sample.getFilenames().contains(filename));
    assertEquals(note, sample.getNote());
    assertEquals(5, test(view.samples).size());
  }

  @Test
  public void save_Update() throws Throwable {
    detachOnServiceGet();
    Sample sample = repository.findById(4L).orElseThrow();
    Path oldFolder = configuration.getHome().folder(sample);
    Files.createDirectories(oldFolder);
    SamplesView view = navigate(SamplesView.class);
    // Sample is randomly JS1 or JS2 because they have the same date. Use a stable select.
    view.samples.select(sample);
    test(view.edit).click();
    SampleDialog dialog = $(SampleDialog.class).first();
    fill(dialog);

    test(dialog.save).click();

    String name = name() + "_20200720";
    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name},
        UI.getCurrent().getLocale()), test(notification).getText());
    sample = repository.findById(4L).orElseThrow();
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
    assertEquals(3, sample.getKeywords().size());
    assertTrue(sample.getKeywords().contains("chipseq"));
    assertTrue(sample.getKeywords().contains(keyword1));
    assertTrue(sample.getKeywords().contains(keyword2));
    assertEquals(2, sample.getFilenames().size());
    assertTrue(sample.getFilenames().contains("OF_20241118_ROB_01"));
    assertTrue(sample.getFilenames().contains(filename));
    assertEquals(note, sample.getNote());
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022", dataset.getName());
    dataset = datasetRepository.findById(6L).orElseThrow();
    assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1_20181208", dataset.getName());
    Thread.sleep(1000); // Allow time to apply changes to files.
    Path folder = configuration.getHome().folder(sample);
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(oldFolder));
    assertEquals(4, test(view.samples).size());
    assertEquals(name, test(view.samples).getCellText(0, column(view.samples, view.name)));
  }

  @Test
  public void cancel() {
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(0);
    test(view.edit).click();
    SampleDialog dialog = $(SampleDialog.class).first();
    fill(dialog);

    test(dialog.cancel).click();

    assertFalse($(Notification.class).exists());
    Sample sample = repository.findById(4L).orElseThrow();
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 50, 20), sample.getCreationDate());
    assertEquals((Long) 3L, sample.getOwner().getId());
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    assertEquals("JS1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals("ChIP-seq", sample.getAssay());
    assertNull(sample.getType());
    assertEquals("Spt16", sample.getTarget());
    assertEquals("yFR101", sample.getStrain());
    assertEquals("G24D", sample.getStrainDescription());
    assertNull(sample.getTreatment());
    assertEquals(3, sample.getKeywords().size());
    assertTrue(sample.getKeywords().contains("chipseq"));
    assertTrue(sample.getKeywords().contains("ip"));
    assertTrue(sample.getKeywords().contains("G24D"));
    assertEquals(1, sample.getFilenames().size());
    assertTrue(sample.getFilenames().contains("OF_20241118_ROB_01"));
    assertNull(sample.getNote());
    assertEquals(4, test(view.samples).size());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void delete() throws Throwable {
    Sample sample = repository.findById(9L).get();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    SamplesView view = navigate(SamplesView.class);
    test(view.ownerFilter).setValue("benoit.coulombe@ircm.qc.ca");
    test(view.samples).select(0);
    test(view.edit).click();
    SampleDialog dialog = $(SampleDialog.class).first();
    final String name = sample.getName();

    test(dialog.delete).click();
    test($(ConfirmDialog.class).first()).confirm();

    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + DELETED, new Object[]{name},
        UI.getCurrent().getLocale()), test(notification).getText());
    assertFalse(repository.findById(9L).isPresent());
    Thread.sleep(1000); // Allow time to apply changes to files.
    assertFalse(Files.exists(folder));
    assertEquals(3, test(view.samples).size());
  }
}
