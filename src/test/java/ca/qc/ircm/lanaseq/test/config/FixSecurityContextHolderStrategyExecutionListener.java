package ca.qc.ircm.lanaseq.test.config;

import static ca.qc.ircm.lanaseq.UsedBy.SPRING;

import ca.qc.ircm.lanaseq.UsedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Fixes SecurityContextHolderStrategy present in SecurityContextHolder.
 */
public class FixSecurityContextHolderStrategyExecutionListener
    implements TestExecutionListener, InjectDependencies {

  private static final Logger logger =
      LoggerFactory.getLogger(FixSecurityContextHolderStrategyExecutionListener.class);
  private SecurityContextHolderStrategy injectedStrategy;

  @Override
  public void beforeTestClass(TestContext testContext) {
    injectDependencies(testContext.getApplicationContext());
    SecurityContextHolderStrategy holderStrategy = SecurityContextHolder.getContextHolderStrategy();
    if (injectedStrategy != holderStrategy) {
      logger.warn(
          "Missmatch between injected SecurityContextHolderStrategy {} and strategy present in holder {}, "
              + "setting injected strategy in context holder",
          injectedStrategy, holderStrategy);
      SecurityContextHolder.setContextHolderStrategy(injectedStrategy);
    }
  }

  @Autowired
  @UsedBy(SPRING)
  public void setInjectedStrategy(SecurityContextHolderStrategy injectedStrategy) {
    this.injectedStrategy = injectedStrategy;
  }
}
