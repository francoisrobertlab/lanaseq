package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.CREATE_FOLDER_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.NETWORK;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.OVERWRITE_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.SAVED;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Sample dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AddSampleFilesDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(AddSampleFilesDialogPresenter.class);
  private AddSampleFilesDialog dialog;
  private Sample sample;
  private Set<Path> existingFilenames = new HashSet<>();
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
    dialog.addOpenedChangeListener(event -> {
      if (event.isOpened()) {
        if (updateFilesThread != null) {
          updateFilesThread.interrupt();
        }
        updateFilesThread = createUpdateFilesThread();
        updateFilesThread.start();
      } else {
        if (updateFilesThread != null) {
          updateFilesThread.interrupt();
        }
      }
    });
  }

  void localeChange(Locale locale) {
    final AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
    dialog.getUI().ifPresent(ui -> {
      WebBrowser browser = ui.getSession().getBrowser();
      boolean unix = browser.isMacOSX() || browser.isLinux();
      if (sample != null) {
        dialog.message.setText(resources.message(MESSAGE, configuration.uploadLabel(sample, unix)));
      }
      String network = configuration.uploadNetwork(unix);
      dialog.network.setVisible(network != null && !network.isEmpty());
      dialog.network.setText(resources.message(NETWORK, network));
    });
  }

  Thread createUpdateFilesThread() {
    Runnable updateFilesRunnable = () -> {
      logger.debug("stat checking files in sample {} upload folder", sample);
      while (true) {
        dialog.getUI().ifPresent(ui -> ui.access(() -> {
          updateFiles();
          ui.push();
        }));
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          logger.debug("stop checking files in sample {} upload folder", sample);
          return;
        }
      }
    };
    DelegatingSecurityContextRunnable wrappedRunnable = new DelegatingSecurityContextRunnable(
        updateFilesRunnable, SecurityContextHolder.getContext());
    Thread thread = new Thread(wrappedRunnable);
    thread.setDaemon(true);
    return thread;
  }

  void updateFiles() {
    existingFilenames =
        service.files(sample).stream().map(f -> f.getFileName()).collect(Collectors.toSet());
    Path folder = folder();
    if (folder != null) {
      try {
        dialog.files.setItems(Files.list(folder).filter(file -> {
          try {
            return Files.isRegularFile(file) && !Files.isHidden(file);
          } catch (IOException e) {
            return false;
          }
        }));
      } catch (IOException e) {
      }
    }
  }

  boolean exists(Path file) {
    return existingFilenames.contains(file.getFileName());
  }

  private Path folder() {
    return sample != null ? configuration.upload(sample) : null;
  }

  boolean validate(Collection<Path> files, Locale locale) {
    dialog.error.setVisible(false);
    boolean anyExists = files.stream()
        .filter(file -> exists(file) && !dialog.overwrite(file).getValue()).findAny().isPresent();
    if (anyExists) {
      final AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
      dialog.error.setVisible(true);
      dialog.error.setText(resources.message(OVERWRITE_ERROR));
    }
    return !anyExists;
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
    if (validate(files, locale)) {
      logger.debug("save new files {} for sample {}", files, sample);
      service.saveFiles(sample, files);
      try {
        Files.delete(folder);
      } catch (IOException e) {
        logger.warn("could not delete upload folder {}", folder);
      }
      final AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
      dialog.showNotification(resources.message(SAVED, files.size(), sample.getName()));
      dialog.fireSavedEvent();
      dialog.close();
    }
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
    localeChange(locale);
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
