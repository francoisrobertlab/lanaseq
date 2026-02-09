package ca.qc.ircm.lanaseq.jobs.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.jobs.Job.UNDETERMINED_PROGRESS;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.jobs.Job;
import ca.qc.ircm.lanaseq.jobs.JobService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import com.vaadin.testbench.BrowserTest;
import java.time.LocalDateTime;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link JobsView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class JobsViewIT extends AbstractBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(JobsView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private JobService service;
  @Autowired
  private AuthenticatedUser authenticatedUser;

  @AfterEach
  public void clearJobs() {
    authenticatedUser.getUser().ifPresent(user -> service.getJobs().forEach(service::removeJob));
  }

  private void open() {
    openView(VIEW_NAME);
  }

  private void prepareJobs() {
    Job job = job();
    job.time = LocalDateTime.now().minusDays(1);
    job.progress = 1.0;
    job.future = CompletableFuture.completedFuture(null);
    service.addJob(job);
    job = job();
    job.time = LocalDateTime.now().minusHours(12);
    job.progress = 0.4;
    job.future = CompletableFuture.failedFuture(new IllegalStateException("test"));
    service.addJob(job);
    job = job();
    job.time = LocalDateTime.now().minusHours(10);
    job.progress = 0.2;
    when(job.future.isDone()).thenReturn(true);
    when(job.future.isCancelled()).thenReturn(true);
    try {
      doThrow(new CancellationException()).when(job.future).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    service.addJob(job);
    // Fake job to test InterruptedException in JobsView.
    job = job();
    job.time = LocalDateTime.now().minusHours(8);
    job.progress = 0.6;
    when(job.future.isDone()).thenReturn(true);
    try {
      doThrow(new InterruptedException()).when(job.future).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    service.addJob(job);
    job = job();
    job.time = LocalDateTime.now().minusHours(1);
    job.progress = 0.4;
    service.addJob(job);
    job = job();
    job.time = LocalDateTime.now().minusMinutes(1);
    job.progress = UNDETERMINED_PROGRESS;
    service.addJob(job);
  }

  private Job job() {
    Job job = new Job();
    job.owner = authenticatedUser.getUser().orElseThrow();
    job.title = RandomStringUtils.insecure().nextAlphanumeric(50);
    job.message = RandomStringUtils.insecure().nextAlphanumeric(20);
    job.future = Mockito.mock(Future.class);
    return job;
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
    JobsViewElement view = $(JobsViewElement.class).waitForFirst();
    assertTrue(optional(view::jobs).isPresent());
    assertTrue(optional(view::refresh).isPresent());
    assertTrue(optional(view::removeDone).isPresent());
  }

  @BrowserTest
  public void refresh() {
    prepareJobs();
    open();
    JobsViewElement view = $(JobsViewElement.class).waitForFirst();
    Assertions.assertEquals(6, view.jobs().getRowCount());
    Job job = job();
    job.time = LocalDateTime.now();
    job.progress = UNDETERMINED_PROGRESS;
    service.addJob(job);
    view.refresh().click();
    Assertions.assertEquals(7, view.jobs().getRowCount());
  }

  @BrowserTest
  public void removeDone() {
    prepareJobs();
    open();
    JobsViewElement view = $(JobsViewElement.class).waitForFirst();
    Assertions.assertEquals(6, view.jobs().getRowCount());
    view.removeDone().click();
    Assertions.assertEquals(2, view.jobs().getRowCount());
  }
}
