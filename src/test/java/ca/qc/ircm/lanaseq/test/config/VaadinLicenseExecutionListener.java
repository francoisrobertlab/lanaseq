package ca.qc.ircm.lanaseq.test.config;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.unit.UIUnitTest;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Skips tests if Vaadin license is missing.
 */
@Order(0)
public class VaadinLicenseExecutionListener implements TestExecutionListener, InjectDependencies {
  private static final String LICENSE_ERROR_MESSAGE =
      "License for Vaadin TestBench not found. Skipping test class {0} .";
  private static final String[] LICENSE_PATHS =
      new String[] { ".vaadin/offlineKey", ".vaadin/proKey", "vaadin.testbench.developer.license",
          ".vaadin.testbench.developer.license" };
  private static final String LICENSE_SYSTEM_PROPERTY = "vaadin.testbench.developer.license";
  private static final Class<?>[] TEST_BENCH_CLASSES =
      new Class[] { UIUnitTest.class, TestBenchTestCase.class };
  private static final Logger logger =
      LoggerFactory.getLogger(VaadinLicenseExecutionListener.class);

  @Override
  public void beforeTestClass(TestContext testContext) throws Exception {
    injectDependencies(testContext.getApplicationContext());
    if (isTestBenchTest(testContext)) {
      boolean licenseFileExists = false;
      for (String licencePath : LICENSE_PATHS) {
        licenseFileExists |=
            Files.exists(Paths.get(System.getProperty("user.home")).resolve(licencePath));
      }
      if (!licenseFileExists && System.getProperty(LICENSE_SYSTEM_PROPERTY) == null) {
        String message =
            MessageFormat.format(LICENSE_ERROR_MESSAGE, testContext.getTestClass().getName());
        logger.info(message);
        // Vaadin license file not found, skip tests.
        assumeTrue(false, message);
      }
    }
  }

  private boolean isTestBenchTest(TestContext testContext) {
    boolean isTestBenchClass = false;
    for (Class testBenchClass : TEST_BENCH_CLASSES) {
      isTestBenchClass |= testBenchClass.isAssignableFrom(testContext.getTestClass());
    }
    return isTestBenchClass;
  }
}
