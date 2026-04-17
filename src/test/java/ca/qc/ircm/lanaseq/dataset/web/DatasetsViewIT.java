package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link DatasetsView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetsViewIT extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(DatasetsView.class);
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private MessageSource messageSource;

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    navigate(VIEW_NAME, SigninView.class);
  }

  @Test
  public void edit() {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(0);
    test(view.edit).click();
    assertTrue($(DatasetDialog.class).exists());
  }

  @Test
  public void edit_DeselectAfterSave() {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(0);
    test(view.edit).click();
    DatasetDialog dialog = $(DatasetDialog.class).first();
    test(dialog.save).click();
    assertFalse(view.edit.isEnabled());
    test(view.datasets).select(0);
    assertTrue(view.edit.isEnabled());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void edit_DeselectAfterDelete() {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets.ownerFilter).setValue("benoit.coulombe@ircm.qc.ca");
    test(view.datasets).select(1);
    test(view.edit).click();
    DatasetDialog dialog = $(DatasetDialog.class).first();
    test(dialog.delete).click();
    test($(ConfirmDialog.class).first()).confirm();
    assertFalse(view.edit.isEnabled());
    test(view.datasets).select(0);
    assertTrue(view.edit.isEnabled());
  }

  @Test
  public void merge() {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(0);
    test(view.datasets).select(3);

    test(view.merge).click();

    String name = "ChIPseq_Spt16_yFR101_G24D_JS1-JS2-JS3_20181022";
    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + MERGED, new Object[]{name},
        UI.getCurrent().getLocale()), test(notification).getText());
    List<Dataset> datasets = repository.findByOwner(new User(3L));
    Dataset dataset = datasets.stream().filter(ex -> name.equals(ex.getName())).findFirst()
        .orElseThrow();
    assertNotEquals(0, dataset.getId());
    assertEquals(name, dataset.getName());
    assertEquals(4, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertTrue(dataset.getKeywords().contains("G24D"));
    assertTrue(dataset.getKeywords().contains("Spt16"));
    assertEquals(LocalDate.of(2018, 10, 22), dataset.getDate());
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(dataset.getCreationDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(dataset.getCreationDate()));
    assertTrue(dataset.isEditable());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(3, dataset.getSamples().size());
    assertTrue(find(dataset.getSamples(), 4L).isPresent());
    assertTrue(find(dataset.getSamples(), 5L).isPresent());
    assertTrue(find(dataset.getSamples(), 11L).isPresent());
  }

  @Test
  public void files() {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(0);
    test(view.files).click();
    assertTrue($(DatasetFilesDialog.class).exists());
  }

  @Test
  public void analyze() {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(0);
    test(view.analyze).click();
    assertTrue($(DatasetsAnalysisDialog.class).exists());
  }
}
