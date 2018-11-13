package ca.qc.ircm.lana.web.component;

import static org.mockito.Mockito.verify;

import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class NavigationComponentTest {
  private NavigationComponentForTest navigationComponent = new NavigationComponentForTest();
  @Mock
  private UI ui;

  @Test
  public void navigate_String() {
    navigationComponent.navigate("abc");

    verify(ui).navigate("abc");
  }

  @Test
  public void navigate() {
    navigationComponent.navigate(TestView.class);

    verify(ui).navigate(TestView.class);
  }

  @Test
  public void navigate_Parameter() {
    navigationComponent.navigate(TestView.class, "someParameters");

    verify(ui).navigate(TestView.class, "someParameters");
  }

  private class NavigationComponentForTest implements NavigationComponent {
    @Override
    public Optional<UI> getUI() {
      return Optional.of(ui);
    }
  }

  @Route("test-view")
  @SuppressWarnings("serial")
  private static class TestView extends VerticalLayout implements HasUrlParameter<String> {
    @Override
    public void setParameter(BeforeEvent event, String parameter) {
    }
  }
}
