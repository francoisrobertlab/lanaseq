package ca.qc.ircm.lanaseq.jobs;

import static ca.qc.ircm.lanaseq.jobs.Job.UNDETERMINED_PROGRESS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Unit tests for {@link JobService}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class JobServiceTest {

  @Autowired
  private JobService service;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private AuthenticatedUser authenticatedUser;
  private final List<Job> jobs = new ArrayList<>();

  @BeforeEach
  public void beforeEach() {
    clearJobs();
    Job job = job();
    job.time = LocalDateTime.now().minusDays(1);
    job.progress = 1.0;
    job.future = CompletableFuture.completedFuture(null);
    jobs.add(job);
    job = job();
    job.time = LocalDateTime.now().minusHours(15);
    job.progress = 1.0;
    job.future = CompletableFuture.completedFuture(null);
    job.owner = userRepository.findById(5L).orElseThrow();
    jobs.add(job);
    job = job();
    job.time = LocalDateTime.now().minusHours(12);
    job.progress = 1.0;
    job.future = CompletableFuture.failedFuture(new IllegalStateException("test"));
    jobs.add(job);
    job = job();
    job.time = LocalDateTime.now().minusHours(10);
    job.progress = 1.0;
    job.future = Mockito.mock(Future.class);
    when(job.future.isDone()).thenReturn(true);
    when(job.future.isCancelled()).thenReturn(true);
    try {
      doThrow(new CancellationException()).when(job.future).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    jobs.add(job);
    job = job();
    job.time = LocalDateTime.now().minusHours(1);
    job.progress = 0.4;
    job.future = Mockito.mock(Future.class);
    jobs.add(job);
    job = job();
    job.time = LocalDateTime.now().minusMinutes(1);
    job.progress = UNDETERMINED_PROGRESS;
    job.future = Mockito.mock(Future.class);
    jobs.add(job);
    jobs.forEach(service::addJob);
  }

  private void clearJobs() {
    try {
      Field field = JobService.class.getDeclaredField("jobs");
      field.setAccessible(true);
      @SuppressWarnings("unchecked") List<Job> jobs = (List<Job>) field.get(service);
      jobs.clear();
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException("Cannot access jobs field from JobService", e);
    }
  }

  private Job job() {
    Job job = new Job();
    job.owner = authenticatedUser.getUser().orElseThrow();
    job.title = RandomStringUtils.insecure().nextAlphanumeric(50);
    job.message = RandomStringUtils.insecure().nextAlphanumeric(20);
    return job;
  }

  @Test
  public void getJobs() {
    List<Job> jobs = service.getJobs();
    assertEquals(5, jobs.size());
    for (int i = 0; i < jobs.size(); i++) {
      Job expected = this.jobs.get(i < 1 ? i : i + 1);
      Job actual = jobs.get(i);
      assertEquals(expected.uuid, actual.uuid);
      assertEquals(3L, actual.owner.getId());
      assertEquals(expected.title, actual.title);
      assertEquals(expected.message, actual.message);
      assertEquals(expected.progress, actual.progress);
      assertEquals(expected.time, actual.time);
    }
    assertTrue(jobs.get(0).future.isDone());
    assertFalse(jobs.get(0).future.isCancelled());
    assertDoesNotThrow(() -> jobs.get(0).future.get());
    assertTrue(jobs.get(1).future.isDone());
    assertFalse(jobs.get(1).future.isCancelled());
    try {
      jobs.get(1).future.get();
    } catch (ExecutionException e) {
      assertInstanceOf(IllegalStateException.class, e.getCause());
    } catch (InterruptedException e) {
      fail("Unexpected InterruptedException");
    }
    assertTrue(jobs.get(2).future.isDone());
    assertTrue(jobs.get(2).future.isCancelled());
    assertThrows(CancellationException.class, () -> jobs.get(2).future.get());
    assertFalse(jobs.get(3).future.isDone());
    assertFalse(jobs.get(3).future.isCancelled());
    assertDoesNotThrow(() -> jobs.get(3).future.get());
    assertFalse(jobs.get(4).future.isDone());
    assertFalse(jobs.get(4).future.isCancelled());
    assertDoesNotThrow(() -> jobs.get(4).future.get());
  }

  @Test
  public void addJob() {
    Job expected = job();
    expected.time = LocalDateTime.now();
    expected.progress = UNDETERMINED_PROGRESS;
    expected.future = Mockito.mock(Future.class);
    service.addJob(expected);
    List<Job> jobs = service.getJobs();
    assertEquals(6, jobs.size());
    Job actual = jobs.get(5);
    assertEquals(expected.uuid, actual.uuid);
    assertEquals(3L, actual.owner.getId());
    assertEquals(expected.title, actual.title);
    assertEquals(expected.message, actual.message);
    assertEquals(expected.progress, actual.progress);
    assertEquals(expected.time, actual.time);
    assertFalse(actual.future.isDone());
    assertFalse(actual.future.isCancelled());
    assertDoesNotThrow(() -> actual.future.get());
  }

  @Test
  public void removeJob() {
    service.removeJob(jobs.get(0));
    List<Job> jobs = service.getJobs();
    assertEquals(4, jobs.size());
    for (int i = 2; i < jobs.size(); i++) {
      Job expected = this.jobs.get(i);
      Job actual = jobs.get(i - 2);
      assertEquals(expected.uuid, actual.uuid);
      assertEquals(3L, actual.owner.getId());
      assertEquals(expected.title, actual.title);
      assertEquals(expected.message, actual.message);
      assertEquals(expected.progress, actual.progress);
      assertEquals(expected.time, actual.time);
    }
  }
}
