package ca.qc.ircm.lanaseq.jobs;

import ca.qc.ircm.lanaseq.user.User;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Information about a running job.
 */
public class Job {

  public static final double UNDETERMINED_PROGRESS = -1.0;

  /**
   * Job's id.
   */
  public UUID uuid = UUID.randomUUID();
  /**
   * Job's {@link Future}.
   */
  public Future<?> future;
  /**
   * User who started the job.
   */
  public User owner;
  /**
   * Job's title.
   */
  public String title;
  /**
   * Job's message.
   */
  public String message;
  /**
   * Job's progression.
   */
  public double progress = UNDETERMINED_PROGRESS;
  /**
   * Job's submission date.
   */
  public LocalDateTime time = LocalDateTime.now();
}
