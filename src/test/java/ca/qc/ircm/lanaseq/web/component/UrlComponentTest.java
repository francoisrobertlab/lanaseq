package ca.qc.ircm.lanaseq.web.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link UrlComponent}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class UrlComponentTest extends SpringUIUnitTest {
  private UrlComponentForTest urlComponent = new UrlComponentForTest();

  @Test
  public void getUrl() {
    String url = urlComponent.getUrl(DatasetsView.VIEW_NAME);
    assertEquals("/" + DatasetsView.VIEW_NAME, url);
  }

  private class UrlComponentForTest implements UrlComponent {
  }
}
