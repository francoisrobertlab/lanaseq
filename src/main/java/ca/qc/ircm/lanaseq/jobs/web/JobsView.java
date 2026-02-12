package ca.qc.ircm.lanaseq.jobs.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.jobs.Job.UNDETERMINED_PROGRESS;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.normalizedCollator;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.jobs.Job;
import ca.qc.ircm.lanaseq.jobs.JobService;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.io.Serial;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Show running jobs and progression message.
 */
@Route(value = JobsView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({USER})
public class JobsView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {

  public static final String VIEW_NAME = "jobs";
  public static final String ID = "jobs-view";
  public static final String JOBS = "jobs";
  public static final String JOBS_EMPTY = styleName("jobs", "empty");
  public static final String JOBS_TITLE = styleName("jobs", TITLE);
  public static final String MESSAGE = "message";
  public static final String TIME = "time";
  public static final String PROGRESS = "progress";
  public static final String SUCCESS = "success";
  public static final String ERROR = "error";
  public static final String CANCELLED = "cancelled";
  public static final String INTERRUPTED = "interrupted";
  public static final String REFRESH = "refresh";
  public static final String REMOVE_DONE = "remove-done";
  private static final String MESSAGE_PREFIX = messagePrefix(JobsView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Serial
  private static final long serialVersionUID = 4580019829931636008L;
  private static final Logger logger = LoggerFactory.getLogger(JobsView.class);
  protected Grid<Job> jobs = new Grid<>();
  protected Column<Job> title;
  protected Column<Job> time;
  protected Column<Job> progress;
  protected Button refresh = new Button();
  protected Button removeDone = new Button();
  private final Map<Job, Boolean> jobsDone = new ConcurrentHashMap<>();
  private final transient JobService service;

  /**
   * Create new JobsView.
   *
   * @param service job service
   */
  @Autowired
  public JobsView(JobService service) {
    this.service = service;
  }

  @PostConstruct
  void init() {
    logger.debug("jobs view");
    setId(ID);
    setHeightFull();
    add(jobs, new HorizontalLayout(refresh, removeDone));
    jobs.setId(JOBS);
    title = jobs.addColumn(job -> job.title, TITLE).setKey(TITLE)
        .setComparator(Comparator.comparing(job -> job.title, normalizedCollator()));
    time = jobs.addColumn(
            new LocalDateTimeRenderer<>(job -> job.time, () -> DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .setKey(TIME).setSortProperty(TIME);
    progress = jobs.addColumn(new ComponentRenderer<>(this::progressBar)).setKey(PROGRESS)
        .setSortable(true).setComparator(this::progressComparator);
    refresh.setId(REFRESH);
    refresh.addClickListener(e -> loadJobs());
    removeDone.setId(REMOVE_DONE);
    removeDone.addClickListener(e -> removeDone());
    loadJobs();
  }

  private Component progressBar(Job job) {
    if (job.future.isDone()) {
      Span progress = new Span();
      try {
        job.future.get();
        progress.getElement().getThemeList().add("badge success");
        progress.setText(getTranslation(MESSAGE_PREFIX + SUCCESS));
      } catch (ExecutionException e) {
        progress.getElement().getThemeList().add("badge error");
        progress.setText(getTranslation(MESSAGE_PREFIX + ERROR, e.getCause().getMessage()));
      } catch (CancellationException e) {
        progress.getElement().getThemeList().add("badge warning");
        progress.setText(getTranslation(MESSAGE_PREFIX + CANCELLED));
      } catch (InterruptedException e) {
        progress.getElement().getThemeList().add("badge warning");
        progress.setText(getTranslation(MESSAGE_PREFIX + INTERRUPTED));
      }
      return progress;
    } else {
      ProgressBar progressBar = new ProgressBar();
      progressBar.setIndeterminate(job.progress == UNDETERMINED_PROGRESS);
      NativeLabel message = new NativeLabel(job.message);
      message.setId(styleName(job.uuid));
      progressBar.getElement().setAttribute("aria-labelledby", styleName(job.uuid));
      Span progress = new Span();
      if (job.progress != UNDETERMINED_PROGRESS) {
        progressBar.setValue(job.progress);
        progress.setText(String.format("%.0f%%", job.progress * 100));
      }
      HorizontalLayout progressBarLabel = new HorizontalLayout(message, progress);
      progressBarLabel.setJustifyContentMode(JustifyContentMode.BETWEEN);
      return new VerticalLayout(progressBarLabel, progressBar);
    }
  }

  private int progressComparator(Job job1, Job job2) {
    int job1State = Math.min(Math.max(0, (int) (job1.progress * 100)), 100);
    if (job1.future.isDone()) {
      try {
        job1.future.get();
        job1State = 200;
      } catch (ExecutionException e) {
        job1State = 400;
      } catch (CancellationException | InterruptedException e) {
        job1State = 300;
      }
    }
    int job2State = Math.min(Math.max(0, (int) (job2.progress * 100)), 100);
    if (job2.future.isDone()) {
      try {
        job2.future.get();
        job2State = 200;
      } catch (ExecutionException e) {
        job2State = 400;
      } catch (CancellationException | InterruptedException e) {
        job2State = 300;
      }
    }
    return job1State - job2State;
  }

  private void loadJobs() {
    List<Job> jobs = service.getJobs();
    jobsDone.clear();
    jobs.forEach(job -> jobsDone.put(job, job.future.isDone()));
    this.jobs.setItems(jobs);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    jobs.setEmptyStateText(getTranslation(MESSAGE_PREFIX + JOBS_EMPTY));
    title.setHeader(getTranslation(MESSAGE_PREFIX + JOBS_TITLE));
    time.setHeader(getTranslation(MESSAGE_PREFIX + TIME));
    progress.setHeader(getTranslation(MESSAGE_PREFIX + PROGRESS));
    refresh.setText(getTranslation(MESSAGE_PREFIX + REFRESH));
    removeDone.setText(getTranslation(MESSAGE_PREFIX + REMOVE_DONE));
  }

  @Override
  public String getPageTitle() {
    return getTranslation(MESSAGE_PREFIX + TITLE,
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
  }

  private void removeDone() {
    jobsDone.entrySet().stream().filter(Entry::getValue)
        .forEach(e -> service.removeJob(e.getKey()));
    loadJobs();
  }
}
