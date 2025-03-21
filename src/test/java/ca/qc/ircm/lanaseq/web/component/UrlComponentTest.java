package ca.qc.ircm.lanaseq.web.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link UrlComponent}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class UrlComponentTest extends SpringUIUnitTest {

  private final UrlComponentForTest urlComponent = new UrlComponentForTest();

  @Test
  public void getUrl() {
    String url = urlComponent.getUrl(DatasetsView.class);
    assertEquals(DatasetsView.VIEW_NAME, url);
  }

  @Test
  public void getUrl_Parameter() {
    String url = urlComponent.getUrl(UseForgotPasswordView.class, "test/sub");
    assertEquals(UseForgotPasswordView.VIEW_NAME + "/test/sub", url);
  }

  @Test
  public void getUrlWithContextPath() {
    String url = urlComponent.getUrlWithContextPath(DatasetsView.class);
    assertEquals("/" + DatasetsView.VIEW_NAME, url);
  }

  @Test
  public void getUrlWithContextPath_Parameter() {
    String url = urlComponent.getUrlWithContextPath(UseForgotPasswordView.class, "test/sub");
    assertEquals("/" + UseForgotPasswordView.VIEW_NAME + "/test/sub", url);
  }

  @Test
  public void prependContextPath() {
    String url = urlComponent.prependContextPath("test/sub");
    assertEquals("/test/sub", url);
  }

  private static class UrlComponentForTest implements UrlComponent {

  }
}
