package ca.qc.ircm.lanaseq.test.config;

import com.vaadin.testbench.TestBench;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

/**
 * Configuration for {@link TestBench} tests.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@TestExecutionListeners(
    value = { InitializeDatabaseExecutionListener.class, VaadinLicenseExecutionListener.class,
        FixSecurityContextHolderStrategyExecutionListener.class,
        TestBenchTestExecutionListener.class, TestBenchSecurityFilter.class },
    mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@Headless
@Transactional
@Sql({ "/drop-schema-h2.sql", "/schema-h2.sql", "/user-data.sql", "/dataset-data.sql",
    "/fix-it-tests.sql" })
public @interface TestBenchTestAnnotations {
  /**
   * Returns true if ScreenshotOnFailureRule is used, false otherwise.
   *
   * @return true if ScreenshotOnFailureRule is used, false otherwise
   */
  public boolean useScreenshotRule() default false;
}
