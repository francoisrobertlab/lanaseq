package ca.qc.ircm.lanaseq.web;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import java.io.Serial;

/**
 * Select event.
 */
public class SelectedEvent<V extends Component, E> extends ComponentEvent<V> {
  @Serial
  private static final long serialVersionUID = -9036570365228629040L;
  private final E selection;

  public SelectedEvent(V source, boolean fromClient, E selection) {
    super(source, fromClient);
    this.selection = selection;
  }

  public E getSelection() {
    return selection;
  }
}
