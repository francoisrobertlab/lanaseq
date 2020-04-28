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

package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.ExperimentDialog.SAVED;
import static ca.qc.ircm.lanaseq.dataset.ExperimentProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.ExperimentProperties.PROJECT;
import static ca.qc.ircm.lanaseq.dataset.ExperimentProperties.PROTOCOL;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Experiment;
import ca.qc.ircm.lanaseq.dataset.ExperimentService;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
  @Autowired
  private ExperimentService service;
  @Autowired
  private ProtocolService protocolService;

  protected ExperimentDialogPresenter() {
  }

  protected ExperimentDialogPresenter(ExperimentService service, ProtocolService protocolService) {
    this.service = service;
    this.protocolService = protocolService;
  }

  void init(ExperimentDialog dialog) {
    this.dialog = dialog;
    dialog.protocol.setItems(protocolService.all());
    setExperiment(null);
  }

  void localeChange(Locale locale) {
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(dialog.name).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(NAME);
    binder.forField(dialog.project).withNullRepresentation("").bind(PROJECT);
    binder.forField(dialog.protocol).asRequired(webResources.message(REQUIRED)).bind(PROTOCOL);
  }

  BinderValidationStatus<Experiment> validateExperiment() {
    return binder.validate();
  }

  private boolean validate() {
    return validateExperiment().isOk();
  }

  void save(Locale locale) {
    if (validate()) {
      logger.debug("Save experiment {}", experiment);
      service.save(experiment);
      AppResources resources = new AppResources(ExperimentDialog.class, locale);
      dialog.showNotification(resources.message(SAVED, experiment.getName()));
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
