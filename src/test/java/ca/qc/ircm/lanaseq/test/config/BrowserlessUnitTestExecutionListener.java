package ca.qc.ircm.lanaseq.test.config;

import com.vaadin.browserless.internal.MockVaadin;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Configures browserless unit tests.
 */
public class BrowserlessUnitTestExecutionListener implements TestExecutionListener {

  @Override
  public void beforeTestMethod(TestContext testContext) {
    AnnotationFinder.findAnnotation(testContext.getTestClass(), testContext.getTestMethod(),
        UserAgent.class).ifPresent(ua -> MockVaadin.INSTANCE.setUserAgent(ua.value()));
  }
}
