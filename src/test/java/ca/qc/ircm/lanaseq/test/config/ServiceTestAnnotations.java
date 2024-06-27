package ca.qc.ircm.lanaseq.test.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Configuration for tests using the database.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@ActiveProfiles("test")
@WebAppConfiguration
@TestExecutionListeners(
    value = { InitializeDatabaseExecutionListener.class, VaadinLicenseExecutionListener.class,
        FixSecurityContextHolderStrategyExecutionListener.class,
        UiUnitTestExecutionListener.class },
    mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@Transactional
@Sql({ "/drop-schema-h2.sql", "/schema-h2.sql", "/user-data.sql", "/dataset-data.sql" })
public @interface ServiceTestAnnotations {

}
