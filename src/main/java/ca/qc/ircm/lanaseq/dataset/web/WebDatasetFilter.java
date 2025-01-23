package ca.qc.ircm.lanaseq.dataset.web;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetFilter;
import com.vaadin.flow.function.SerializablePredicate;
import java.io.Serial;

/**
 * Serializable {@link DatasetFilter}.
 */
public class WebDatasetFilter extends DatasetFilter implements SerializablePredicate<Dataset> {
  @Serial
  private static final long serialVersionUID = -1498568750159631734L;
}
