package ca.qc.ircm.lanaseq.test.config;

import static org.junit.jupiter.api.Assumptions.abort;

import java.text.MessageFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Skips Selenium test based on configuration.
 */
@Order(0)
public class SkipSeleniumTestExecutionListener implements TestExecutionListener,
    InjectDependencies {

  private static final String SKIP_SELENIUM_ERROR_MESSAGE = "Selenium tests are skipped. Skipping test class {0}.";
  private static final Class<?>[] SELENIUM_CLASSES = new Class[]{AbstractSeleniumTestCase.class};

  @Value("${selenium.skip:false}")
  private boolean skipSeleniumTest;

  @Override
  public void beforeTestClass(TestContext testContext) {
    injectDependencies(testContext.getApplicationContext());
    if (isSeleniumTest(testContext)) {
      if (skipSeleniumTest) {
        String message = MessageFormat.format(SKIP_SELENIUM_ERROR_MESSAGE,
            testContext.getTestClass().getName());
        abort(message);
      }
    }
  }

  private boolean isSeleniumTest(TestContext testContext) {
    boolean isSeleniumClass = false;
    for (Class<?> seleniumClass : SELENIUM_CLASSES) {
      isSeleniumClass |= seleniumClass.isAssignableFrom(testContext.getTestClass());
    }
    return isSeleniumClass;
  }
}
