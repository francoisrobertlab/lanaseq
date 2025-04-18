package ca.qc.ircm.lanaseq.test.config;

import static ca.qc.ircm.lanaseq.test.config.AnnotationFinder.findAnnotation;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.vaadin.testbench.BrowserTestBase;
import com.vaadin.testbench.TestBench;
import java.io.File;
import java.util.HashMap;
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
  @SuppressWarnings("unused")
  private static final String FIREFOX_DRIVER = FirefoxDriver.class.getName();
  private static final String CHROME_DRIVER = ChromeDriver.class.getName();
  private static final String DEFAULT_DRIVER = CHROME_DRIVER;
  private static final Logger logger = LoggerFactory.getLogger(
      TestBenchTestExecutionListener.class);
  @Value("${download-home:${user.dir}/target}")
  protected File downloadHome;

  @Override
  public void beforeTestClass(TestContext testContext) {
    injectDependencies(testContext.getApplicationContext());
    if (isTestBenchTest(testContext)) {
      assumeFalse(isSkipTestBenchTests(), SKIP_TESTS_ERROR_MESSAGE);
    }
  }

  @Override
  public void beforeTestMethod(TestContext testContext) {
    if (isTestBenchTest(testContext)) {
      WebDriver driver = TestBench.createDriver(driver(testContext));
      BrowserTestBase target = getInstance(testContext);
      target.setDriver(driver);
      try {
        driver.manage().window().setSize(new Dimension(1280, 960));
      } catch (WebDriverException e) {
        logger.warn("Could not resize browser", e);
      }
    }
  }

  private boolean isSkipTestBenchTests() {
    return Boolean.parseBoolean(System.getProperty(SKIP_TESTS_SYSTEM_PROPERTY));
  }

  private boolean isTestBenchTest(TestContext testContext) {
    return BrowserTestBase.class.isAssignableFrom(testContext.getTestClass());
  }

  private BrowserTestBase getInstance(TestContext testContext) {
    return (BrowserTestBase) testContext.getTestInstance();
  }

  private WebDriver driver(TestContext testContext) {
    final boolean headless = findAnnotation(testContext.getTestClass(), testContext.getTestMethod(),
        Headless.class).map(Headless::value).orElse(false);
    String driverClass = System.getProperty(DRIVER_SYSTEM_PROPERTY);
    if (driverClass == null) {
      driverClass = DEFAULT_DRIVER;
    }
    if (driverClass.equals(CHROME_DRIVER)) {
      Download downloadAnnotations = findAnnotation(testContext.getTestClass(),
          testContext.getTestMethod(), Download.class).orElse(null);
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
      throw new IllegalStateException("We support only with Firefox and Chrome");
    }
  }
}
