package ca.qc.ircm.lanaseq;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategy;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

/**
 * Configuration for Spring.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

  private final AsyncTaskExecutor asyncTaskExecutor;
  private final SecurityContextHolderStrategy securityContextHolderStrategy;

  /**
   * Creates a new {@link AsyncConfiguration} instance.
   *
   * @param applicationTaskExecutor       default {@link AsyncTaskExecutor} from Spring Boot
   * @param securityContextHolderStrategy SecurityContextHolderStrategy
   */
  @Autowired
  public AsyncConfiguration(AsyncTaskExecutor applicationTaskExecutor,
      SecurityContextHolderStrategy securityContextHolderStrategy) {
    this.asyncTaskExecutor = applicationTaskExecutor;
    this.securityContextHolderStrategy = securityContextHolderStrategy;
  }

  @Override
  public Executor getAsyncExecutor() {
    assert securityContextHolderStrategy instanceof VaadinAwareSecurityContextHolderStrategy;
    DelegatingSecurityContextAsyncTaskExecutor executor = new DelegatingSecurityContextAsyncTaskExecutor(
        asyncTaskExecutor);
    executor.setSecurityContextHolderStrategy(securityContextHolderStrategy);
    return executor;
  }
}
