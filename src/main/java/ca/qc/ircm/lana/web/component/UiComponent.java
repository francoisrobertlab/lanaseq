package ca.qc.ircm.lana.web.component;

import com.vaadin.flow.component.UI;
import java.util.Optional;

/**
 * A component connected to a UI.
 */
public interface UiComponent {
  /**
   * Gets the UI this component is attached to.
   * <p>
   * Copied from Vaadin
   * </p>
   *
   * @return an optional UI component, or an empty optional if this component is not attached to a
   *         UI
   */
  @SuppressWarnings("checkstyle:all")
  public Optional<UI> getUI();
}
