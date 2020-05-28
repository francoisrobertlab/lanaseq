package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.CREATE_FOLDER_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.SAVED;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Sample dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AddSampleFilesDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(AddSampleFilesDialogPresenter.class);
  private AddSampleFilesDialog dialog;
  private Sample sample;
  private Thread updateFilesThread;
  private SampleService service;
  private AppConfiguration configuration;

  @Autowired
  protected AddSampleFilesDialogPresenter(SampleService service, AppConfiguration configuration) {
    this.service = service;
    this.configuration = configuration;
  }

  void init(AddSampleFilesDialog dialog) {
    this.dialog = dialog;
    updateFilesThread = new Thread(() -> {
      while (true) {
        logger.debug("call updatefiles on ui {}", dialog.getUI());
        dialog.getUI().ifPresent(ui -> ui.access(() -> {
          updateFiles();
          ui.push();
        }));
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          logger.debug("interrupted");
          return;
        }
      }
    });
    updateFilesThread.setDaemon(true);
    dialog.addOpenedChangeListener(e -> {
      if (e.isOpened()) {
        updateFilesThread.start();
      } else {
        updateFilesThread.interrupt();
      }
    });
  }

  void updateFiles() {
    Path folder = folder();
    if (folder != null) {
      try {
        dialog.files.setItems(Files.list(folder));
      } catch (IOException e) {
      }
    }
  }

  Path folder() {
    return sample != null ? configuration.upload(sample) : null;
  }

  void save(Locale locale) {
    Path folder = folder();
    Collection<Path> files;
    try {
      files = folder != null ? Files.list(folder).collect(Collectors.toList())
          : Collections.emptyList();
    } catch (IOException e) {
      files = Collections.emptyList();
    }
    logger.debug("save new files {} for sample {}", files, sample);
    service.saveFiles(sample, files);
    final AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
    dialog.showNotification(resources.message(SAVED, files.size(), sample.getName()));
  }

  Sample getSample() {
    return sample;
  }

  void setSample(Sample sample, Locale locale) {
    if (sample == null) {
      throw new NullPointerException("sample cannot be null");
    }
    if (sample.getId() == null) {
      throw new IllegalArgumentException("sample cannot be new");
    }
    this.sample = sample;
    createFolder(locale);
    updateFiles();
  }

  private void createFolder(Locale locale) {
    Path folder = folder();
    if (folder != null) {
      try {
        logger.debug("creating upload folder {} for sample {}", sample);
        Files.createDirectories(folder);
      } catch (IOException e) {
        final AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
        dialog.showNotification(resources.message(CREATE_FOLDER_ERROR));
      }
    }
  }

  Thread updateFilesThread() {
    return updateFilesThread;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    updateFilesThread.interrupt();
  }
}
