package ca.qc.ircm.lanaseq.protocol.web;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFilter;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Serializable {@link ProtocolFilter}.
 */
public class WebProtocolFilter extends ProtocolFilter implements SerializablePredicate<Protocol> {
  private static final long serialVersionUID = 3530179527089133408L;
}
