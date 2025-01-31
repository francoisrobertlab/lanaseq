package ca.qc.ircm.lanaseq.protocol.web;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFilter;
import com.vaadin.flow.function.SerializablePredicate;
import java.io.Serial;

/**
 * Serializable {@link ProtocolFilter}.
 */
public class WebProtocolFilter extends ProtocolFilter implements SerializablePredicate<Protocol> {

  @Serial
  private static final long serialVersionUID = 3530179527089133408L;
}
