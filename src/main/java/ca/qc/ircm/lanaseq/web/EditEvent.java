package ca.qc.ircm.lanaseq.web;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

/**
 * Edit event.
 */
public class EditEvent<V extends Component, E> extends ComponentEvent<V> {

  private final E item;

  public EditEvent(V source, boolean fromClient, E item) {
    super(source, fromClient);
    this.item = item;
  }

  public E getItem() {
    return item;
  }
}
