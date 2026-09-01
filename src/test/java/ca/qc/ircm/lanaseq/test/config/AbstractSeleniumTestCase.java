package ca.qc.ircm.lanaseq.test.config;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.web.PublicDatasetFiles;
import ca.qc.ircm.lanaseq.sample.web.PublicSampleFiles;
import ca.qc.ircm.lanaseq.web.SigninViewComponent;
import ca.qc.ircm.lanaseq.web.ViewLayoutComponent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Base class for tests that use Selenium.
 */
public abstract class AbstractSeleniumTestCase {

  protected WebDriver driver;
  @Value("${download-home:${user.dir}/target}")
  protected Path downloadHome;
  @Value("${local.server.port}")
  protected int port;
  @Value("${server.servlet.context-path:}")
  protected String contextPath;
  @Value("${selenium.headless:false}")
  protected boolean headless;
  @Autowired
  private AppConfiguration configuration;

  @BeforeEach
  public void createWebDriver() {
    ChromeOptions options = new ChromeOptions();
    HashMap<String, Object> chromePrefs = new HashMap<>();
    chromePrefs.put("download.default_directory", downloadHome.toString());
    chromePrefs.put("credentials_enable_service", false); // Disables the credentials service.
    chromePrefs.put("profile.password_manager_enabled", false); // Turns off the password manager.
    chromePrefs.put("profile.password_manager_leak_detection",
        false); // Disables the password breach warning.
    options.setExperimentalOption("prefs", chromePrefs);
    if (headless) {
      options.addArguments("--headless");
    }
    driver = new ChromeDriver(options);
    driver.manage().window().setSize(new Dimension(1200, 1083));
  }

  @BeforeEach
  public void setServerUrl()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method setServerUrl = AppConfiguration.class.getDeclaredMethod("setServerUrl", String.class);
    setServerUrl.setAccessible(true);
    setServerUrl.invoke(configuration, baseUrl());
  }

  @AfterEach
  public void quitWebDriver() {
    driver.quit();
  }

  protected String baseUrl() {
    String host = "localhost";
    return "http://" + host + ":" + port;
  }

  protected String homeUrl() {
    return baseUrl() + contextPath + "/";
  }

  protected String viewUrl(String view) {
    return baseUrl() + contextPath + "/" + view;
  }

  protected void openView(String view) {
    openView(view, null);
  }

  protected void openView(String view, @Nullable String parameters) {
    String url = viewUrl(view);
    if (parameters != null && !parameters.isEmpty()) {
      url += "/" + parameters;
    }
    if (url.equals(driver.getCurrentUrl())) {
      driver.navigate().refresh();
    } else {
      driver.get(url);
      if (!url.contains(PublicDatasetFiles.REST_MAPPING) && !url.contains(
          PublicSampleFiles.REST_MAPPING)) {
        // The first time the page is loaded, Vaadin may be initiating for a long time.
        waitUntil(d -> {
          try {
            return ViewLayoutComponent.find().apply(d);
          } catch (NoSuchElementException e) {
            return SigninViewComponent.find().apply(d);
          }
        }, Duration.ofSeconds(30));
      }
    }
  }

  protected Locale currentLocale() {
    return Locale.US;
  }

  protected <T> Optional<T> optional(Supplier<T> supplier) {
    try {
      return Optional.of(supplier.get());
    } catch (Throwable e) {
      return Optional.empty();
    }
  }

  protected <T> T waitUntil(Function<? super WebDriver, T> isTrue) {
    return waitUntil(isTrue, Duration.ofSeconds(5));
  }

  protected <T> T waitUntil(Function<? super WebDriver, T> isTrue, Duration waitTime) {
    Wait<WebDriver> wait = new WebDriverWait(driver, waitTime);
    return wait.until(isTrue);
  }

  protected void scrollIntoView(WebElement element) {
    executeScript("arguments[0].scrollIntoView(true);", element);
  }

  protected @Nullable Object executeScript(String script, @Nullable Object... args) {
    assert driver instanceof JavascriptExecutor;
    JavascriptExecutor js = (JavascriptExecutor) driver;
    return js.executeScript(script, args);
  }
}
