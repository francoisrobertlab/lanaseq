package ca.qc.ircm.lanaseq;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Tests for {@link AsyncConfiguration}.
 */
@Service
public class AsyncConfigurationTestBean {

  @Async
  public CompletableFuture<Void> run() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return CompletableFuture.completedFuture(null);
  }
}
