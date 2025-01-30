package ca.qc.ircm.lanaseq;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Querydsl.
 */
@Configuration
public class QuerydslConfiguration {

  private final EntityManager entityManager;

  @Autowired
  protected QuerydslConfiguration(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Bean
  public JPAQueryFactory jpaQueryFactory() {
    return new JPAQueryFactory(entityManager);
  }
}
