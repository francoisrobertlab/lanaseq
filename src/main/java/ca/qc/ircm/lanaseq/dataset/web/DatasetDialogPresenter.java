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
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAVED;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROJECT;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROTOCOL;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
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
 * Dataset dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(DatasetDialogPresenter.class);
  private DatasetDialog dialog;
  private Binder<Dataset> binder = new BeanValidationBinder<>(Dataset.class);
  private Dataset dataset;
  @Autowired
  private DatasetService service;
  @Autowired
  private ProtocolService protocolService;

  protected DatasetDialogPresenter() {
  }

  protected DatasetDialogPresenter(DatasetService service, ProtocolService protocolService) {
    this.service = service;
    this.protocolService = protocolService;
  }

  void init(DatasetDialog dialog) {
    this.dialog = dialog;
    dialog.protocol.setItems(protocolService.all());
    setDataset(null);
  }

  void localeChange(Locale locale) {
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(dialog.name).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(NAME);
    binder.forField(dialog.project).withNullRepresentation("").bind(PROJECT);
    binder.forField(dialog.protocol).asRequired(webResources.message(REQUIRED)).bind(PROTOCOL);
  }

  BinderValidationStatus<Dataset> validateDataset() {
    return binder.validate();
  }

  private boolean validate() {
    return validateDataset().isOk();
  }

  void save(Locale locale) {
    if (validate()) {
      logger.debug("Save dataset {}", dataset);
      service.save(dataset);
      AppResources resources = new AppResources(DatasetDialog.class, locale);
      dialog.showNotification(resources.message(SAVED, dataset.getName()));
      dialog.close();
      dialog.fireSavedEvent();
    }
  }

  void cancel() {
    dialog.close();
  }

  Dataset getDataset() {
    return dataset;
  }

  /**
   * Sets dataset.
   *
   * @param dataset
   *          dataset
   */
  void setDataset(Dataset dataset) {
    if (dataset == null) {
      dataset = new Dataset();
    }
    this.dataset = dataset;
    binder.setBean(dataset);
  }
}
