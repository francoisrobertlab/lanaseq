package ca.qc.ircm.lanaseq.test.config;

import ca.qc.ircm.lanaseq.AppConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Do not use temporary folders for {@link AppConfiguration}.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KeepFoldersForAppConfiguration {

}
