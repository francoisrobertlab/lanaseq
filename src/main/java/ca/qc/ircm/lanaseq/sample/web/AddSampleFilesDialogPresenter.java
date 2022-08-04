/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.CREATE_FOLDER_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.OVERWRITE_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.SAVED;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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
 * Add sample files dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AddSampleFilesDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(AddSampleFilesDialogPresenter.class);
  private AddSampleFilesDialog dialog;
  private Sample sample;
  private Locale locale;
  private Set<String> existingFilenames = new HashSet<>();
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
        createFolder();
        updateFilesThread = createUpdateFilesThread();
        updateFilesThread.start();
      } else {
        if (updateFilesThread != null) {
          updateFilesThread.interrupt();
          try {
            updateFilesThread.join(5000);
          } catch (InterruptedException e) {
            // Assume interrupted.
          }
        }
        dialog.files.setItems(new ArrayList<>());
      }
    });
  }

  void localeChange(Locale locale) {
    this.locale = locale;
    final AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
    dialog.getUI().ifPresent(ui -> {
      WebBrowser browser = ui.getSession().getBrowser();
      boolean unix = browser.isMacOSX() || browser.isLinux();
      if (sample != null) {
        dialog.message.setText(resources.message(MESSAGE, configuration.uploadLabel(sample, unix)));
      }
    });
  }

  private Thread createUpdateFilesThread() {
    Runnable updateFilesRunnable = () -> {
      logger.debug("start checking files in sample upload folder {}", folder());
      while (!Thread.currentThread().isInterrupted()) {
        dialog.getUI().ifPresent(ui -> ui.access(() -> {
          updateFiles();
          try {
            ui.push();
          } catch (IllegalStateException | UIDetachedException e) {
            return;
          }
        }));
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          logger.debug("stop checking files in sample upload folder {}", folder());
          Thread.currentThread().interrupt();
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
        service.files(sample).stream().map(f -> f.toFile().getName()).collect(Collectors.toSet());
    dialog.files.setItems(service.uploadFiles(sample).stream().map(file -> file.toFile()));
  }

  boolean exists(File file) {
    return existingFilenames.contains(file.getName());
  }

  private Path folder() {
    return sample != null ? configuration.upload(sample) : null;
  }

  private boolean validate(Collection<Path> files) {
    dialog.error.setVisible(false);
    boolean anyExists = files.stream()
        .filter(file -> exists(file.toFile()) && !dialog.overwrite(file.toFile()).getValue())
        .findAny().isPresent();
    if (anyExists) {
      final AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
      dialog.error.setVisible(true);
      dialog.error.setText(resources.message(OVERWRITE_ERROR));
    }
    return !anyExists;
  }

  void save(Locale locale) {
    Collection<Path> files = service.uploadFiles(sample);
    if (validate(files)) {
      logger.debug("save new files {} for sample {}", files, sample);
      service.saveFiles(sample, files);
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
    localeChange(locale);
    updateFiles();
  }

  private void createFolder() {
    Path folder = folder();
    if (folder != null) {
      try {
        logger.debug("creating upload folder {} for sample {}", folder, sample);
        Files.createDirectories(folder);
      } catch (IOException e) {
        final AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
        dialog.showNotification(resources.message(CREATE_FOLDER_ERROR, folder));
      }
    }
  }

  Thread updateFilesThread() {
    return updateFilesThread;
  }
}
