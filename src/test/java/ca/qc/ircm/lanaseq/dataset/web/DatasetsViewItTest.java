package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGED;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
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
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetsViewItTest extends AbstractTestBenchTestCase {
  private static final String MESSAGE_PREFIX = messagePrefix(DatasetsView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private MessageSource messageSource;

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  @WithAnonymousUser
  public void security_Anonymous() throws Throwable {
    open();
    $(SigninViewElement.class).waitForFirst();
  }

  @Test
  public void title() throws Throwable {
    open();
    String applicationName =
        messageSource.getMessage(CONSTANTS_PREFIX + APPLICATION_NAME, null, currentLocale());
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + TITLE, new Object[] { applicationName },
        currentLocale()), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.datasets()).isPresent());
    assertTrue(optional(() -> view.edit()).isPresent());
    assertTrue(optional(() -> view.merge()).isPresent());
    assertTrue(optional(() -> view.files()).isPresent());
  }

  @Test
  public void view_Files() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(0);
    assertTrue(view.filesDialog().isOpen());
  }

  @Test
  public void edit() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(0);
    view.edit().click();
    assertTrue(view.dialog().isOpen());
  }

  @Test
  public void merge() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(0);
    view.datasets().select(3);

    view.merge().click();

    String name = "ChIPseq_Spt16_yFR101_G24D_JS1-JS2-JS3_20181022";
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + MERGED, new Object[] { name }, currentLocale()),
        notification.getText());
    List<Dataset> datasets = repository.findByOwner(new User(3L));
    Dataset dataset =
        datasets.stream().filter(ex -> name.equals(ex.getName())).findFirst().orElseThrow();
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
  public void files() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(0);
    view.files().click();
    assertTrue(view.filesDialog().isOpen());
  }

  @Test
  public void analyze() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(0);
    view.analyze().click();
    assertTrue(view.analyzeDialog().isOpen());
  }
}
