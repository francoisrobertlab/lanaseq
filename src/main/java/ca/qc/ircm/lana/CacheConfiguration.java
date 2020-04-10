package ca.qc.ircm.lana;

import java.util.Arrays;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {
  /**
   * Returns cache manager.
   *
   * @return cache manager
   */
  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache("acl")));
    return cacheManager;
  }
}
