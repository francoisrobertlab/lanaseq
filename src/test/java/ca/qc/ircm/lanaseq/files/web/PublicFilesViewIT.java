package ca.qc.ircm.lanaseq.files.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SamplePublicFileRepository;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import com.vaadin.testbench.BrowserTest;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link PublicFilesView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class PublicFilesViewIT extends AbstractBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(PublicFilesView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private SampleRepository sampleRepository;
  @Autowired
  private SamplePublicFileRepository samplePublicFileRepository;

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
    PublicFilesViewElement view = $(PublicFilesViewElement.class).waitForFirst();
    assertTrue(optional(view::files).isPresent());
    assertTrue(optional(() -> view.files().filenameFilter()).isPresent());
    assertTrue(optional(() -> view.files().expiryDateFilter()).isPresent());
    assertTrue(optional(() -> view.files().sampleNameFilter()).isPresent());
    assertTrue(optional(() -> view.files().ownerFilter()).isPresent());
    assertTrue(optional(view::downloadLinks).isPresent());
  }

  @BrowserTest
  public void delete() {
    open();
    Sample sample = sampleRepository.findById(11L).orElseThrow();
    assertTrue(samplePublicFileRepository.findBySampleAndPath(sample,
        "JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bw").isPresent());
    PublicFilesViewElement view = $(PublicFilesViewElement.class).waitForFirst();
    Assertions.assertEquals(4, view.files().getRowCount());

    view.files().delete(0).click();

    Assertions.assertEquals(3, view.files().getRowCount());
    assertFalse(samplePublicFileRepository.findBySampleAndPath(sample,
        "JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bw").isPresent());
  }
}
