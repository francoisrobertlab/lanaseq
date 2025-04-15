package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGED;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.BrowserTest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SamplesView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SamplesViewItTest extends AbstractBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(SamplesView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private MessageSource messageSource;

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  @WithAnonymousUser
  public void security_Anonymous() {
    open();
    $(SigninViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void title() {
    open();
    String applicationName = messageSource.getMessage(CONSTANTS_PREFIX + APPLICATION_NAME, null,
        currentLocale());
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + TITLE, new Object[]{applicationName},
            currentLocale()), getDriver().getTitle());
  }

  @BrowserTest
  public void fieldsExistence() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    assertTrue(optional(view::samples).isPresent());
    assertTrue(optional(view::add).isPresent());
    assertTrue(optional(view::edit).isPresent());
    assertTrue(optional(view::merge).isPresent());
    assertTrue(optional(view::files).isPresent());
    assertTrue(optional(view::analyze).isPresent());
  }

  @BrowserTest
  public void viewFiles() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(0);
    assertTrue(view.filesDialog().isOpen());
  }

  @BrowserTest
  public void edit() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(0);
    view.edit().click();
    assertTrue(view.dialog().isOpen());
  }

  @BrowserTest
  public void edit_DeselectAfterSave() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(0);
    view.edit().click();
    view.dialog().save().click();
    assertFalse(view.edit().isEnabled());
    view.samples().select(0);
    assertTrue(view.edit().isEnabled());
  }

  @BrowserTest
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void edit_DeselectAfterDelete() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().ownerFilter().setValue("benoit.coulombe@ircm.qc.ca");
    view.samples().select(0);
    view.edit().click();
    SampleDialogElement dialog = view.dialog();
    dialog.delete().click();
    dialog.confirm().getConfirmButton().click();
    assertFalse(view.edit().isEnabled());
    view.samples().select(0);
    assertTrue(view.edit().isEnabled());
  }

  @BrowserTest
  public void add() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.add().click();
    assertTrue(view.dialog().isOpen());
  }

  @BrowserTest
  public void merge() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(1);
    view.samples().select(view.samples().name(2).startsWith("JS2") ? 2 : 3);

    view.merge().click();

    String name = "ChIPseq_Spt16_yFR101_G24D_JS2-JS1_20181022";
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + MERGED, new Object[]{name}, currentLocale()),
        notification.getText());
    List<Dataset> datasets = datasetRepository.findByOwner(new User(3L));
    Dataset dataset = datasets.stream().filter(ex -> name.equals(ex.getName())).findFirst()
        .orElseThrow();
    assertNotEquals(0, dataset.getId());
    Assertions.assertEquals(name, dataset.getName());
    Assertions.assertEquals(4, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("chipseq"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertTrue(dataset.getKeywords().contains("G24D"));
    assertTrue(dataset.getKeywords().contains("Spt16"));
    Assertions.assertEquals(LocalDate.of(2018, 10, 22), dataset.getDate());
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(dataset.getCreationDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(dataset.getCreationDate()));
    assertTrue(dataset.isEditable());
    Assertions.assertEquals((Long) 3L, dataset.getOwner().getId());
    Assertions.assertEquals(2, dataset.getSamples().size());
    assertTrue(find(dataset.getSamples(), 5L).isPresent());
    assertTrue(find(dataset.getSamples(), 10L).isPresent());
  }

  @BrowserTest
  public void files() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(0);
    view.files().click();
    assertTrue(view.filesDialog().isOpen());
  }

  @BrowserTest
  public void analyze() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(0);
    view.analyze().click();
    assertTrue(view.analyzeDialog().isOpen());
  }
}
