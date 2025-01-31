package ca.qc.ircm.lanaseq.web;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import java.io.Serial;

/**
 * Deleted event.
 */
public class DeletedEvent<V extends Component> extends ComponentEvent<V> {

  @Serial
  private static final long serialVersionUID = -4851866491581465448L;

  public DeletedEvent(V source, boolean fromClient) {
    super(source, fromClient);
  }
}
