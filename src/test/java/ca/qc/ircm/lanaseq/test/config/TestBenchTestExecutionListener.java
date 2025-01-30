package ca.qc.ircm.lanaseq.test.config;

import static ca.qc.ircm.lanaseq.test.config.AnnotationFinder.findAnnotation;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.vaadin.testbench.Parameters;
import com.vaadin.testbench.TestBenchTestCase;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
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

  private static final String SKIP_TESTS_ERROR_MESSAGE = "TestBench tests are skipped";
  private static final String SKIP_TESTS_SYSTEM_PROPERTY = "testbench.skip";
  private static final String DRIVER_SYSTEM_PROPERTY = "testbench.driver";
  private static final String RETRIES_SYSTEM_PROPERTY = "testbench.retries";
  @SuppressWarnings("unused")
  private static final String FIREFOX_DRIVER = FirefoxDriver.class.getName();
  private static final String CHROME_DRIVER = ChromeDriver.class.getName();
  @SuppressWarnings("unused")
  private static final String DEFAULT_DRIVER = CHROME_DRIVER;
  private static final Logger logger =
      LoggerFactory.getLogger(TestBenchTestExecutionListener.class);
  @Value("${download-home:${user.dir}/target}")
  protected File downloadHome;

  @Override
  public void beforeTestClass(TestContext testContext) {
    injectDependencies(testContext.getApplicationContext());
    if (isTestBenchTest(testContext)) {
      assumeFalse(isSkipTestBenchTests(), SKIP_TESTS_ERROR_MESSAGE);

      setRetries();
    }
  }

  @Override
  public void beforeTestMethod(@NotNull TestContext testContext) {
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
  public void afterTestMethod(@NotNull TestContext testContext) {
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
    return Boolean.parseBoolean(System.getProperty(SKIP_TESTS_SYSTEM_PROPERTY));
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
            .map(Headless::value).orElse(false);
    String driverClass = System.getProperty(DRIVER_SYSTEM_PROPERTY);
    if (driverClass == null) {
      driverClass = DEFAULT_DRIVER;
    }
    if (driverClass.equals(CHROME_DRIVER)) {
      Download downloadAnnotations =
          findAnnotation(testContext.getTestClass(), testContext.getTestMethod(), Download.class)
              .orElse(null);
      ChromeOptions options = new ChromeOptions();
      options.addArguments("--remote-allow-origins=*");
      if (downloadAnnotations != null) {
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", downloadHome.getPath());
        options.setExperimentalOption("prefs", chromePrefs);
      } else if (headless) {
        options.addArguments("--headless");
      }
      return new ChromeDriver(options);
    } else if (driverClass.equals(FIREFOX_DRIVER)) {
      FirefoxProfile profile = new FirefoxProfile();
      profile.setPreference("browser.download.folderList", 2);
      profile.setPreference("browser.download.dir", downloadHome.getPath());
      profile.setPreference("browser.download.useDownloadDir", true);
      profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
          "application/msword,application/pdf");
      profile.setPreference("pdfjs.disabled", true);
      FirefoxOptions options = new FirefoxOptions();
      options.setProfile(profile);
      if (headless) {
        options.addArguments("--headless");
      }
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
