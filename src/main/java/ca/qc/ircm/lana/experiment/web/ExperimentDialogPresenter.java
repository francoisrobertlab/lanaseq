/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lana.experiment.web;

import static ca.qc.ircm.lana.user.UserProperties.NAME;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentService;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Experiment dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExperimentDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(ExperimentDialogPresenter.class);
  private ExperimentDialog dialog;
  private Binder<Experiment> binder = new BeanValidationBinder<>(Experiment.class);
  private Experiment experiment;
  @Inject
  private ExperimentService experimentService;

  protected ExperimentDialogPresenter() {
  }

  protected ExperimentDialogPresenter(ExperimentService experimentService) {
    this.experimentService = experimentService;
  }

  void init(ExperimentDialog dialog) {
    this.dialog = dialog;
    setExperiment(null);
  }

  void localeChange(Locale locale) {
    final MessageResource webResources = new MessageResource(WebConstants.class, locale);
    binder.forField(dialog.name).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(NAME);
  }

  BinderValidationStatus<Experiment> validateExperiment() {
    return binder.validate();
  }

  private boolean validate() {
    return validateExperiment().isOk();
  }

  void save() {
    if (validate()) {
      logger.debug("Save experiment {}", experiment);
      experimentService.save(experiment);
      dialog.close();
      dialog.fireSavedEvent();
    }
  }

  void cancel() {
    dialog.close();
  }

  Experiment getExperiment() {
    return experiment;
  }

  /**
   * Sets experiment.
   *
   * @param experiment
   *          experiment
   */
  void setExperiment(Experiment experiment) {
    if (experiment == null) {
      experiment = new Experiment();
    }
    this.experiment = experiment;
    binder.setBean(experiment);
  }
}
