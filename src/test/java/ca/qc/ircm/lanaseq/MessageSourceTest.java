package ca.qc.ircm.lanaseq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.test.context.support.WithUserDetails;

@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class MessageSourceTest {

  @Autowired
  private MessageSource messageSource;

  @Test
  public void configuration() {
    assertInstanceOf(ReloadableResourceBundleMessageSource.class, messageSource);
    ReloadableResourceBundleMessageSource reloadableMessageSource =
        (ReloadableResourceBundleMessageSource) messageSource;
    List<String> basenames = reloadableMessageSource.getBasenameSet().stream().toList();
    assertEquals(2, basenames.size());
    String currentDir = FilenameUtils.separatorsToUnix(System.getProperty("user.dir"));
    assertEquals("file:" + currentDir + "/messages", basenames.get(0));
    assertEquals("classpath:messages", basenames.get(1));
  }
}
