package ca.qc.ircm.lanaseq.jobs;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Jobs that are running or that completed recently.
 */
@Component
public class JobService {

  /**
   * All jobs that are running or that completed recently.
   */
  private final List<Job> jobs = new ArrayList<>();
  private final AuthenticatedUser authenticatedUser;

  /**
   * Creates new JobService.
   *
   * @param authenticatedUser authenticated user.
   */
  public JobService(AuthenticatedUser authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
  }

  /**
   * Returns all jobs started by current user.
   *
   * @return all jobs started by current user
   */
  public List<Job> getJobs() {
    User user = authenticatedUser.getUser()
        .orElseThrow(() -> new AccessDeniedException("User is not authenticated"));
    return jobs.stream().filter(j -> user.equals(j.owner)).toList();
  }

  /**
   * Adds job to list of running jobs.
   *
   * @param job job
   */
  public void addJob(Job job) {
    jobs.add(job);
  }

  /**
   * Removes job from list of running jobs.
   *
   * @param job job
   */
  public void removeJob(Job job) {
    jobs.remove(job);
  }
}
