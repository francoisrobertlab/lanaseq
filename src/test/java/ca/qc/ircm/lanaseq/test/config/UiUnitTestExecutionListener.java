package ca.qc.ircm.lanaseq.test.config;

import com.vaadin.testbench.unit.internal.MockVaadin;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Configures UI unit tests.
 */
public class UiUnitTestExecutionListener implements TestExecutionListener {
  @Override
  public void beforeTestMethod(TestContext testContext) {
    AnnotationFinder
        .findAnnotation(testContext.getTestClass(), testContext.getTestMethod(), UserAgent.class)
        .ifPresent(ua -> MockVaadin.INSTANCE.setUserAgent(ua.value()));
  }
}
