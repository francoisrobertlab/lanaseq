package ca.qc.ircm.lanaseq.experiment.web;

import ca.qc.ircm.lanaseq.experiment.Experiment;
import ca.qc.ircm.lanaseq.experiment.ExperimentFilter;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Serializable {@link ExperimentFilter}.
 */
public class WebExperimentFilter extends ExperimentFilter
    implements SerializablePredicate<Experiment> {
  private static final long serialVersionUID = -1498568750159631734L;
}
