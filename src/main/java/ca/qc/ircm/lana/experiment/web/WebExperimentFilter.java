package ca.qc.ircm.lana.experiment.web;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentFilter;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Serializable {@link ExperimentFilter}.
 */
public class WebExperimentFilter extends ExperimentFilter
    implements SerializablePredicate<Experiment> {
  private static final long serialVersionUID = -1498568750159631734L;
}
