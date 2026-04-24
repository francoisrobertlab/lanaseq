package ca.qc.ircm.lanaseq.test.config;

import ca.qc.ircm.lanaseq.AppConfiguration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;

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
  @Autowired
  private AppConfiguration configuration;

  @BeforeEach
  public void createWebDriver() {
    ChromeOptions options = new ChromeOptions();
    HashMap<String, Object> chromePrefs = new HashMap<>();
    chromePrefs.put("download.default_directory", downloadHome.toString());
    options.setExperimentalOption("prefs", chromePrefs);
    //options.addArguments("--headless");
    driver = new ChromeDriver(options);
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
}
