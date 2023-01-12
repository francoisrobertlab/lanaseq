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

import static ca.qc.ircm.lanaseq.test.config.AnnotationFinder.findAnnotation;
import static org.junit.Assume.assumeTrue;

import com.vaadin.testbench.Parameters;
import com.vaadin.testbench.TestBenchTestCase;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Rule for integration tests using Vaadin's test bench.
 */
@Order(0)
public class TestBenchTestExecutionListener implements TestExecutionListener, InjectDependencies {
  @SuppressWarnings("checkstyle:linelength")
  private static final String LICENSE_ERROR_MESSAGE =
      "License for Vaadin TestBench not found. Skipping test class {0} .";
  private static final String[] LICENSE_PATHS = new String[] { ".vaadin/proKey",
      "vaadin.testbench.developer.license", ".vaadin.testbench.developer.license" };
  private static final String LICENSE_SYSTEM_PROPERTY = "vaadin.testbench.developer.license";
  private static final String SKIP_TESTS_ERROR_MESSAGE = "TestBench tests are skipped";
  private static final String SKIP_TESTS_SYSTEM_PROPERTY = "testbench.skip";
  private static final String DRIVER_SYSTEM_PROPERTY = "testbench.driver";
  private static final String RETRIES_SYSTEM_PROPERTY = "testbench.retries";
  private static final String FIREFOX_DRIVER = FirefoxDriver.class.getName();
  private static final String CHROME_DRIVER = ChromeDriver.class.getName();
  @SuppressWarnings("unused")
  private static final String DEFAULT_DRIVER = CHROME_DRIVER;
  private static final Logger logger =
      LoggerFactory.getLogger(TestBenchTestExecutionListener.class);
  @Value("${download-home:${user.dir}/target}")
  protected String downloadHome;

  @Override
  public void beforeTestClass(TestContext testContext) throws Exception {
    injectDependencies(testContext.getApplicationContext());
    if (isTestBenchTest(testContext)) {
      if (isSkipTestBenchTests()) {
        assumeTrue(SKIP_TESTS_ERROR_MESSAGE, false);
      }

      boolean licenseFileExists = false;
      for (String licencePath : LICENSE_PATHS) {
        licenseFileExists |=
            Files.exists(Paths.get(System.getProperty("user.home")).resolve(licencePath));
      }
      if (!licenseFileExists && System.getProperty(LICENSE_SYSTEM_PROPERTY) == null) {
        String message =
            MessageFormat.format(LICENSE_ERROR_MESSAGE, testContext.getTestClass().getName());
        logger.info(message);
        assumeTrue(message, false);
      }
      setRetries();
    }
  }

  @Override
  public void beforeTestMethod(TestContext testContext) throws Exception {
    if (isTestBenchTest(testContext)) {
      WebDriver driver = driver(testContext);
      TestBenchTestCase target = getInstance(testContext);
      target.setDriver(driver);
      try {
        driver.manage().window().setSize(new Dimension(1280, 960));
      } catch (WebDriverException e) {
        logger.warn("Could not resize browser", e);
      }
    }
  }

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    if (isTestBenchTest(testContext)) {
      TestBenchTestCase target = getInstance(testContext);
      target.getDriver().manage().deleteAllCookies();
      boolean useScreenshotRule = false;
      TestBenchTestAnnotations testBenchTestAnnotations = findAnnotation(target.getClass(),
          testContext.getTestMethod(), TestBenchTestAnnotations.class).orElseThrow(
              () -> new IllegalStateException(TestBenchTestAnnotations.class.getSimpleName()
                  + " must be present on TestBench tests."));
      if (testBenchTestAnnotations != null) {
        useScreenshotRule = testBenchTestAnnotations.useScreenshotRule();
      }
      if (!useScreenshotRule) {
        target.getDriver().quit();
      }
    }
  }

  private boolean isSkipTestBenchTests() {
    return Boolean.valueOf(System.getProperty(SKIP_TESTS_SYSTEM_PROPERTY));
  }

  private boolean isTestBenchTest(TestContext testContext) {
    return TestBenchTestCase.class.isAssignableFrom(testContext.getTestClass());
  }

  private TestBenchTestCase getInstance(TestContext testContext) {
    return (TestBenchTestCase) testContext.getTestInstance();
  }

  private WebDriver driver(TestContext testContext) {
    final boolean headless =
        findAnnotation(testContext.getTestClass(), testContext.getTestMethod(), Headless.class)
            .map(an -> an.value()).orElse(false);
    String driverClass = System.getProperty(DRIVER_SYSTEM_PROPERTY);
    if (driverClass == null) {
      driverClass = DEFAULT_DRIVER;
    }
    if (driverClass.equals(CHROME_DRIVER)) {
      Download downloadAnnotations =
          findAnnotation(testContext.getTestClass(), testContext.getTestMethod(), Download.class)
              .orElse(null);
      ChromeOptions options = new ChromeOptions();
      if (downloadAnnotations != null) {
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        if (SystemUtils.IS_OS_WINDOWS) {
          chromePrefs.put("download.default_directory",
              FilenameUtils.separatorsToWindows(downloadHome));
        } else {
          chromePrefs.put("download.default_directory", downloadHome);
        }
        options.setExperimentalOption("prefs", chromePrefs);
      } else {
        options.setHeadless(headless);
      }
      return new ChromeDriver(options);
    } else if (driverClass.equals(FIREFOX_DRIVER)) {
      FirefoxProfile profile = new FirefoxProfile();
      profile.setPreference("browser.download.folderList", 2);
      profile.setPreference("browser.download.dir", downloadHome);
      profile.setPreference("browser.download.useDownloadDir", true);
      profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
          "application/msword,application/pdf");
      profile.setPreference("pdfjs.disabled", true);
      FirefoxOptions options = new FirefoxOptions();
      options.setProfile(profile);
      options.setHeadless(headless);
      return new FirefoxDriver(options);
    } else {
      try {
        return (WebDriver) Class.forName(driverClass).getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException
          | NoSuchMethodException | InvocationTargetException e) {
        logger.error("Could not instantiate WebDriver class {}", driverClass);
        throw new IllegalStateException("Could not instantiate WebDriver class " + driverClass, e);
      }
    }
  }

  private void setRetries() {
    if (System.getProperty(RETRIES_SYSTEM_PROPERTY) != null) {
      Parameters.setMaxAttempts(Integer.parseInt(System.getProperty(RETRIES_SYSTEM_PROPERTY)));
    }
  }
}
