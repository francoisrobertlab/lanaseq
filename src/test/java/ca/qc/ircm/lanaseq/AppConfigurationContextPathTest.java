package ca.qc.ircm.lanaseq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests for {@link AppConfiguration}.
 */
@ServiceTestAnnotations
@ActiveProfiles({"test", "context-path"})
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AppConfigurationContextPathTest {

  @Autowired
  private AppConfiguration appConfiguration;

  @Test
  public void getUrl() {
    assertEquals("http://localhost:8080/lanaseq-test/myurl/subpath?param1=abc",
        appConfiguration.getUrl("myurl/subpath?param1=abc"));
  }
}
