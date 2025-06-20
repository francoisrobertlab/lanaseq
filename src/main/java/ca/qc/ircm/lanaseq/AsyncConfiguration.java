package ca.qc.ircm.lanaseq;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategy;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

/**
 * Configuration for Spring.
 */
@Configuration
@EnableAsync
@DependsOn("VaadinSecurityContextHolderStrategy")
public class AsyncConfiguration implements AsyncConfigurer {

  private final AsyncTaskExecutor asyncTaskExecutor;
  private final VaadinAwareSecurityContextHolderStrategy securityContextHolderStrategy;

  /**
   * Creates a new {@link AsyncConfiguration} instance.
   *
   * @param applicationTaskExecutor       default {@link AsyncTaskExecutor} from Spring Boot
   * @param securityContextHolderStrategy Vaadin's VaadinAwareSecurityContextHolderStrategy
   */
  @Autowired
  public AsyncConfiguration(AsyncTaskExecutor applicationTaskExecutor,
      VaadinAwareSecurityContextHolderStrategy securityContextHolderStrategy) {
    this.asyncTaskExecutor = applicationTaskExecutor;
    this.securityContextHolderStrategy = securityContextHolderStrategy;
  }

  @Override
  public Executor getAsyncExecutor() {
    System.out.println(asyncTaskExecutor.getClass());
    DelegatingSecurityContextAsyncTaskExecutor executor = new DelegatingSecurityContextAsyncTaskExecutor(
        asyncTaskExecutor);
    executor.setSecurityContextHolderStrategy(securityContextHolderStrategy);
    return executor;
  }
}
