package ca.qc.ircm.lanaseq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Starts web application.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
