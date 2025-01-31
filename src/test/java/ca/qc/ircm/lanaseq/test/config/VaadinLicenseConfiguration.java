package ca.qc.ircm.lanaseq.test.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Vaadin license configuration.
 */
@ConfigurationProperties(prefix = VaadinLicenseConfiguration.PREFIX)
public record VaadinLicenseConfiguration(boolean assume, List<String> paths) {

  public static final String PREFIX = "vaadin.license";
}
