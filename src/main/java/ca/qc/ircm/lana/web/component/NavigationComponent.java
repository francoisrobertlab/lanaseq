package ca.qc.ircm.lana.web.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.HasUrlParameter;

/**
 * Allows to navigate to another view.
 */
public interface NavigationComponent extends UiComponent {
  public default void navigate(String navigationTarget) {
    getUI().get().navigate(navigationTarget);
  }

  public default void navigate(Class<? extends Component> navigationTarget) {
    getUI().get().navigate(navigationTarget);
  }

  public default <T, C extends Component & HasUrlParameter<T>> void
      navigate(Class<? extends C> navigationTarget, T parameter) {
    getUI().get().navigate(navigationTarget, parameter);
  }
}
