package ca.qc.ircm.lanaseq.jobs.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.jobs.Job.UNDETERMINED_PROGRESS;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.CANCELLED;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.ERROR;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.ID;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.INTERRUPTED;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.JOBS;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.JOBS_TITLE;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.PROGRESS;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.REFRESH;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.REMOVE_DONE;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.SUCCESS;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.TIME;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.jobs.Job;
import ca.qc.ircm.lanaseq.jobs.JobService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link JobsView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class JobsViewTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(JobsView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private JobsView view;
  @MockitoBean
  private JobService service;
  @Autowired
  private AuthenticatedUser authenticatedUser;
  private final Locale locale = Locale.ENGLISH;
  private final List<Job> jobs = new ArrayList<>();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeEach() {
    prepareJobs();
    UI.getCurrent().setLocale(locale);
    view = navigate(JobsView.class);
  }

  private void prepareJobs() {
    Job job = job();
    job.time = LocalDateTime.now().minusDays(1);
    job.progress = 1.0;
    job.future = CompletableFuture.completedFuture(null);
    jobs.add(job);
    job = job();
    job.time = LocalDateTime.now().minusHours(12);
    job.progress = 0.4;
    job.future = CompletableFuture.failedFuture(new IllegalStateException("test"));
    jobs.add(job);
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
    jobs.add(job);
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
    jobs.add(job);
    job = job();
    job.time = LocalDateTime.now().minusHours(1);
    job.progress = 0.4;
    jobs.add(job);
    job = job();
    job.time = LocalDateTime.now().minusMinutes(1);
    job.progress = UNDETERMINED_PROGRESS;
    jobs.add(job);
    when(service.getJobs()).thenReturn(jobs);
  }

  private Job job() {
    Job job = new Job();
    job.owner = authenticatedUser.getUser().orElseThrow();
    job.title = RandomStringUtils.insecure().nextAlphanumeric(50);
    job.message = RandomStringUtils.insecure().nextAlphanumeric(20);
    job.future = Mockito.mock(Future.class);
    return job;
  }

  private Job title(String title) {
    Job job = new Job();
    job.title = title;
    return job;
  }

  private Job progress(double progress, String jobState) {
    Job job = new Job();
    job.progress = progress;
    job.future = Mockito.mock(Future.class);
    switch (jobState) {
      case "" -> {
      }
      case SUCCESS -> job.future = CompletableFuture.completedFuture(null);
      case ERROR -> job.future = CompletableFuture.failedFuture(new IllegalStateException("test"));
      case CANCELLED -> {
        when(job.future.isDone()).thenReturn(true);
        when(job.future.isCancelled()).thenReturn(true);
        try {
          doThrow(new CancellationException()).when(job.future).get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return job;
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(JOBS, view.jobs.getId().orElse(""));
    assertEquals(REFRESH, view.refresh.getId().orElse(""));
    assertEquals(REMOVE_DONE, view.removeDone.getId().orElse(""));
  }

  @Test
  public void labels() {
    HeaderRow headerRow = view.jobs.getHeaderRows().get(0);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + JOBS_TITLE),
        headerRow.getCell(view.title).getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TIME),
        headerRow.getCell(view.time).getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PROGRESS),
        headerRow.getCell(view.progress).getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + REFRESH), view.refresh.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + REMOVE_DONE), view.removeDone.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    HeaderRow headerRow = view.jobs.getHeaderRows().get(0);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + JOBS_TITLE),
        headerRow.getCell(view.title).getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TIME),
        headerRow.getCell(view.time).getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PROGRESS),
        headerRow.getCell(view.progress).getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + REFRESH), view.refresh.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + REMOVE_DONE), view.removeDone.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TITLE,
        view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME)), view.getPageTitle());
  }

  @Test
  public void jobs() {
    assertEquals(3, view.jobs.getColumns().size());
    assertNotNull(view.jobs.getColumnByKey(TITLE));
    assertTrue(view.title.isSortable());
    assertNotNull(view.jobs.getColumnByKey(TIME));
    assertTrue(view.time.isSortable());
    assertNotNull(view.jobs.getColumnByKey(PROGRESS));
    assertTrue(view.progress.isSortable());
    assertInstanceOf(SelectionModel.Single.class, view.jobs.getSelectionModel());
    List<Job> jobs = items(view.jobs);
    verify(service).getJobs();
    assertEquals(this.jobs.size(), jobs.size());
    for (Job job : this.jobs) {
      assertTrue(jobs.contains(job), job::toString);
    }
    assertEquals(jobs.size(), view.jobs.getListDataView().getItemCount());
  }

  @Test
  public void jobs_ColumnsValueProvider() {
    for (int i = 0; i < jobs.size(); i++) {
      Job job = jobs.get(i);
      assertEquals(job.title,
          test(view.jobs).getCellText(i, view.jobs.getColumns().indexOf(view.title)));
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(job.time),
          test(view.jobs).getCellText(i, view.jobs.getColumns().indexOf(view.time)));
      ComponentRenderer<Component, Job> progressRenderer = (ComponentRenderer<Component, Job>) view.progress.getRenderer();
      Component progressComponent = progressRenderer.createComponent(job);
      if (i < 4) {
        assertInstanceOf(Span.class, progressComponent);
        Span progress = (Span) progressComponent;
        assertTrue(progress.getElement().getThemeList().contains("badge"));
        if (i == 0) {
          assertTrue(progress.getElement().getThemeList().contains("success"));
          assertEquals(view.getTranslation(MESSAGE_PREFIX + SUCCESS), progress.getText());
        } else if (i == 1) {
          assertTrue(progress.getElement().getThemeList().contains("error"));
          assertEquals(view.getTranslation(MESSAGE_PREFIX + ERROR, "test"), progress.getText());
        } else if (i == 2) {
          assertTrue(progress.getElement().getThemeList().contains("warning"));
          assertEquals(view.getTranslation(MESSAGE_PREFIX + CANCELLED), progress.getText());
        } else {
          assertTrue(progress.getElement().getThemeList().contains("warning"));
          assertEquals(view.getTranslation(MESSAGE_PREFIX + INTERRUPTED), progress.getText());
        }
      } else {
        assertInstanceOf(VerticalLayout.class, progressComponent);
        NativeLabel message = test(progressComponent).find(NativeLabel.class).first();
        assertEquals(job.message, message.getText());
        Span progress = test(progressComponent).find(Span.class).first();
        ProgressBar progressBar = test(progressComponent).find(ProgressBar.class).first();
        if (i == 4) {
          assertEquals("40%", progress.getText());
          assertFalse(progressBar.isIndeterminate());
          assertEquals(0.4, progressBar.getValue());
        } else if (i == 5) {
          assertEquals("", progress.getText());
          assertTrue(progressBar.isIndeterminate());
        }
      }
    }
  }

  @Test
  public void jobs_TitleColumnComparator() {
    Comparator<Job> comparator = view.title.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(title("éê"), title("ee")));
    assertEquals(0, comparator.compare(title("ÉÊ"), title("EE")));
    assertEquals(0, comparator.compare(title("EE"), title("ee")));
    assertTrue(comparator.compare(title("a"), title("e")) < 0);
    assertTrue(comparator.compare(title("a"), title("é")) < 0);
    assertTrue(comparator.compare(title("e"), title("a")) > 0);
    assertTrue(comparator.compare(title("é"), title("a")) > 0);
  }

  @Test
  public void jobs_ProgressColumnComparator() {
    Comparator<Job> comparator = view.progress.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(progress(1.0, SUCCESS), progress(0.5, SUCCESS)));
    assertEquals(0, comparator.compare(progress(1.0, ERROR), progress(0.5, ERROR)));
    assertEquals(0, comparator.compare(progress(1.0, CANCELLED), progress(0.5, CANCELLED)));
    assertTrue(comparator.compare(progress(1.0, SUCCESS), progress(1.0, ERROR)) < 0);
    assertTrue(comparator.compare(progress(1.0, SUCCESS), progress(1.0, CANCELLED)) < 0);
    assertTrue(comparator.compare(progress(0.3, ""), progress(0.5, "")) < 0);
    assertTrue(comparator.compare(progress(0.5, ""), progress(0.3, SUCCESS)) < 0);
    assertTrue(comparator.compare(progress(0.5, ""), progress(0.3, ERROR)) < 0);
    assertTrue(comparator.compare(progress(0.5, ""), progress(0.3, CANCELLED)) < 0);
  }

  @Test
  public void refresh() {
    Job job = job();
    job.time = LocalDateTime.now();
    job.progress = UNDETERMINED_PROGRESS;
    jobs.add(job);
    test(view.refresh).click();
    verify(service, times(2)).getJobs();
  }

  @Test
  public void removeDone() {
    Job first = jobs.get(0);
    jobs.stream().filter(job -> !job.future.isDone())
        .forEach(job -> job.future = CompletableFuture.completedFuture(null));
    jobs.remove(first);
    test(view.removeDone).click();
    verify(service, times(2)).getJobs();
    verify(service).removeJob(first);
    verify(service).removeJob(jobs.get(0));
    verify(service).removeJob(jobs.get(1));
    verify(service).removeJob(jobs.get(2));
    verify(service, never()).removeJob(jobs.get(3));
    verify(service, never()).removeJob(jobs.get(4));
    assertEquals(5, view.jobs.getListDataView().getItemCount());
  }
}
