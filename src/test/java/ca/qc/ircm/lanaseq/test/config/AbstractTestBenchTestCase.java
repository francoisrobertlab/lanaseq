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

package ca.qc.ircm.lanaseq.test.config;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.SpringConfiguration.messagePrefix;
import static ca.qc.ircm.lanaseq.web.ViewLayout.DATASETS;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.web.AccessDeniedView;
import ca.qc.ircm.lanaseq.user.web.PasswordView;
import ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView;
import ca.qc.ircm.lanaseq.web.SigninView;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import com.vaadin.flow.component.tabs.testbench.TabElement;
import com.vaadin.flow.component.tabs.testbench.TabsElement;
import com.vaadin.testbench.TestBenchTestCase;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;

/**
 * Additional functions for TestBenchTestCase.
 */
public abstract class AbstractTestBenchTestCase extends TestBenchTestCase {
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final String LAYOUT_PREFIX = messagePrefix(ViewLayout.class);
  private static final String SIGNIN_PREFIX = messagePrefix(SigninView.class);
  private static final String USE_FORGOT_PASSWORD_PREFIX =
      messagePrefix(UseForgotPasswordView.class);
  private static final String PASSWORD_PREFIX = messagePrefix(PasswordView.class);
  private static final String ACCESS_DENIED_PREFIX = messagePrefix(AccessDeniedView.class);
  private static final Logger logger = LoggerFactory.getLogger(AbstractTestBenchTestCase.class);
  @Value("http://localhost:${local.server.port}${server.servlet.context-path:}")
  protected String baseUrl;
  private Path home;
  private Path archive;
  private Path analysis;
  private Path upload;
  @Autowired
  private AppConfiguration configuration;
  @Autowired
  private MessageSource messageSource;

  /**
   * Saves home folder to reset its value upon test completion.
   */
  @BeforeEach
  public void saveHomeFolder() throws Throwable {
    Method getHome = AppConfiguration.class.getDeclaredMethod("getHome");
    getHome.setAccessible(true);
    Method getFolder = AppConfiguration.NetworkDrive.class.getDeclaredMethod("getFolder");
    getFolder.setAccessible(true);
    home = (Path) getFolder.invoke((AppConfiguration.NetworkDrive) getHome.invoke(configuration));
  }

  /**
   * Saves archive folder to reset its value upon test completion.
   */
  @BeforeEach
  public void saveArchiveFolder() throws Throwable {
    Method getArchives = AppConfiguration.class.getDeclaredMethod("getArchives");
    getArchives.setAccessible(true);
    Method getFolder = AppConfiguration.NetworkDrive.class.getDeclaredMethod("getFolder");
    getFolder.setAccessible(true);
    archive = (Path) getFolder
        .invoke(((List<AppConfiguration.NetworkDrive>) getArchives.invoke(configuration)).get(0));
  }

  /**
   * Saves analysis folder to reset its value upon test completion.
   */
  @BeforeEach
  public void saveAnalysisFolder() throws Throwable {
    Method getAnalysis = AppConfiguration.class.getDeclaredMethod("getAnalysis");
    getAnalysis.setAccessible(true);
    Method getFolder = AppConfiguration.NetworkDrive.class.getDeclaredMethod("getFolder");
    getFolder.setAccessible(true);
    analysis =
        (Path) getFolder.invoke((AppConfiguration.NetworkDrive) getAnalysis.invoke(configuration));
  }

  /**
   * Saves upload folder to reset its value upon test completion.
   */
  @BeforeEach
  public void saveUploadFolder() throws Throwable {
    Method getUpload = AppConfiguration.class.getDeclaredMethod("getUpload");
    getUpload.setAccessible(true);
    Method getFolder = AppConfiguration.NetworkDrive.class.getDeclaredMethod("getFolder");
    getFolder.setAccessible(true);
    upload =
        (Path) getFolder.invoke((AppConfiguration.NetworkDrive) getUpload.invoke(configuration));
  }

  /**
   * Restores home folder's value.
   */
  @AfterEach
  public void restoreHomeFolder() throws Throwable {
    setHome(home);
  }

  /**
   * Restores archive folder's value.
   */
  @AfterEach
  public void restoreArchiveFolder() throws Throwable {
    setArchive(archive);
  }

  /**
   * Restores upload folder's value.
   */
  @AfterEach
  public void restoreAnalysisFolder() throws Throwable {
    setAnalysis(analysis);
  }

  /**
   * Restores upload folder's value.
   */
  @AfterEach
  public void restoreUploadFolder() throws Throwable {
    setUpload(upload);
  }

  protected String homeUrl() {
    return baseUrl + "/";
  }

  protected String viewUrl(String view) {
    return baseUrl + "/" + view;
  }

  protected String viewUrl(String view, String parameters) {
    return baseUrl + "/" + view + "/" + parameters;
  }

  protected void openView(String view) {
    openView(view, null);
  }

  protected void openView(String view, String parameters) {
    String url = viewUrl(view);
    if (parameters != null && !parameters.isEmpty()) {
      url += "/" + parameters;
    }
    if (url.equals(getDriver().getCurrentUrl())) {
      getDriver().navigate().refresh();
    } else {
      getDriver().get(url);
    }
  }

  protected Locale currentLocale() {
    List<Locale> locales = Constants.getLocales();
    Function<Locale, String> applicationName =
        locale -> messageSource.getMessage(CONSTANTS_PREFIX + APPLICATION_NAME, null, locale);
    TabElement home =
        optional(() -> $(TabsElement.class).first().$(TabElement.class).first()).orElse(null);
    Optional<Locale> optlocale = locales.stream()
        .filter(locale -> messageSource.getMessage(LAYOUT_PREFIX + DATASETS, null, locale)
            .equals(home != null ? home.getText() : ""))
        .findAny();
    if (!optlocale.isPresent()) {
      optlocale = locales.stream()
          .filter(locale -> messageSource.getMessage(SIGNIN_PREFIX + TITLE,
              new Object[] { applicationName.apply(locale) }, locale)
              .equals(getDriver().getTitle()))
          .findAny();
    }
    if (!optlocale.isPresent()) {
      optlocale = locales.stream()
          .filter(locale -> messageSource
              .getMessage(USE_FORGOT_PASSWORD_PREFIX + TITLE,
                  new Object[] { applicationName.apply(locale) }, locale)
              .equals(getDriver().getTitle()))
          .findAny();
    }
    if (!optlocale.isPresent()) {
      optlocale =
          locales.stream()
              .filter(locale -> messageSource
                  .getMessage(PASSWORD_PREFIX + TITLE,
                      new Object[] { applicationName.apply(locale) }, locale)
                  .equals(getDriver().getTitle()))
              .findAny();
    }
    if (!optlocale.isPresent()) {
      optlocale = locales.stream()
          .filter(locale -> messageSource
              .getMessage(ACCESS_DENIED_PREFIX + TITLE,
                  new Object[] { applicationName.apply(locale) }, locale)
              .equals(getDriver().getTitle()))
          .findAny();
    }
    return optlocale.orElse(null);
  }

  protected AppResources resources(Class<?> baseClass) {
    return new AppResources(baseClass, currentLocale());
  }

  protected <T> Optional<T> optional(Supplier<T> supplier) {
    try {
      return Optional.of(supplier.get());
    } catch (Throwable e) {
      return Optional.empty();
    }
  }

  protected void setHome(Path home) throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method getHome = AppConfiguration.class.getDeclaredMethod("getHome");
    getHome.setAccessible(true);
    Method setFolder =
        AppConfiguration.NetworkDrive.class.getDeclaredMethod("setFolder", Path.class);
    setFolder.setAccessible(true);
    setFolder.invoke((AppConfiguration.NetworkDrive) getHome.invoke(configuration), home);
  }

  protected void setArchive(Path archive) throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method getArchives = AppConfiguration.class.getDeclaredMethod("getArchives");
    getArchives.setAccessible(true);
    Method setFolder =
        AppConfiguration.NetworkDrive.class.getDeclaredMethod("setFolder", Path.class);
    setFolder.setAccessible(true);
    setFolder.invoke(
        ((List<AppConfiguration.NetworkDrive>) getArchives.invoke(configuration)).get(0), archive);
  }

  protected void setAnalysis(Path analysis) throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method getAnalysis = AppConfiguration.class.getDeclaredMethod("getAnalysis");
    getAnalysis.setAccessible(true);
    Method setFolder =
        AppConfiguration.NetworkDrive.class.getDeclaredMethod("setFolder", Path.class);
    setFolder.setAccessible(true);
    setFolder.invoke((AppConfiguration.NetworkDrive) getAnalysis.invoke(configuration), analysis);
  }

  protected void setUpload(Path upload) throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method getUpload = AppConfiguration.class.getDeclaredMethod("getUpload");
    getUpload.setAccessible(true);
    Method setFolder =
        AppConfiguration.NetworkDrive.class.getDeclaredMethod("setFolder", Path.class);
    setFolder.setAccessible(true);
    setFolder.invoke((AppConfiguration.NetworkDrive) getUpload.invoke(configuration), upload);
  }
}
