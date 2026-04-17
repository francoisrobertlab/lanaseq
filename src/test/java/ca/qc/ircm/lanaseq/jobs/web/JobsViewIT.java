package ca.qc.ircm.lanaseq.jobs.web;

import static ca.qc.ircm.lanaseq.jobs.Job.UNDETERMINED_PROGRESS;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.jobs.Job;
import ca.qc.ircm.lanaseq.jobs.JobService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.time.LocalDateTime;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link JobsView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class JobsViewIT extends SpringUIUnitTest {

  @Autowired
  private JobService service;
  @Autowired
  private AuthenticatedUser authenticatedUser;

  @AfterEach
  public void afterTest() {
    if (!authenticatedUser.isAnonymous()) {
      service.getJobs().forEach(service::removeJob);
    }
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

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    navigate(VIEW_NAME, SigninView.class);
  }

  @Test
  public void refresh() {
    prepareJobs();
    JobsView view = navigate(JobsView.class);
    assertEquals(6, test(view.jobs).size());
    Job job = job();
    job.time = LocalDateTime.now();
    job.progress = UNDETERMINED_PROGRESS;
    service.addJob(job);
    test(view.refresh).click();
    assertEquals(7, test(view.jobs).size());
  }

  @Test
  public void removeDone() {
    prepareJobs();
    JobsView view = navigate(JobsView.class);
    assertEquals(6, test(view.jobs).size());
    test(view.removeDone).click();
    assertEquals(2, test(view.jobs).size());
  }
}
