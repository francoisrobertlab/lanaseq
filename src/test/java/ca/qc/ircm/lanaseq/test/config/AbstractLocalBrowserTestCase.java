package ca.qc.ircm.lanaseq.test.config;

import com.vaadin.testbench.DriverSupplier;
import java.nio.file.Path;
import java.util.HashMap;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;

/**
 * TestBench integration test that requires local browser.
 */
public abstract class AbstractLocalBrowserTestCase extends AbstractBrowserTestCase implements
    DriverSupplier {

  @Value("${download-home:${user.dir}/target}")
  protected Path downloadHome;

  @Override
  public WebDriver createDriver() {
    ChromeOptions options = new ChromeOptions();
    //options.addArguments("--remote-allow-origins=*");
    HashMap<String, Object> chromePrefs = new HashMap<>();
    //chromePrefs.put("profile.default_content_settings.popups", 0);
    chromePrefs.put("download.default_directory", downloadHome.toString());
    options.setExperimentalOption("prefs", chromePrefs);
    options.addArguments("--headless");
    return new ChromeDriver(options);
  }

  @Override
  protected String baseUrl() {
    return "http://localhost:" + port;
  }
}
