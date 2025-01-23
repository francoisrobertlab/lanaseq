package ca.qc.ircm.lanaseq.sample.web;

import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleFilter;
import com.vaadin.flow.function.SerializablePredicate;
import java.io.Serial;

/**
 * Serializable {@link SampleFilter}.
 */
public class WebSampleFilter extends SampleFilter implements SerializablePredicate<Sample> {
  @Serial
  private static final long serialVersionUID = 2610000381544515210L;
}
