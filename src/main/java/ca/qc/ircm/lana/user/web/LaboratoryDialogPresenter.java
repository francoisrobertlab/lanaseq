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

package ca.qc.ircm.lana.user.web;

import static ca.qc.ircm.lana.Constants.REQUIRED;
import static ca.qc.ircm.lana.user.UserProperties.NAME;

import ca.qc.ircm.lana.AppResources;
import ca.qc.ircm.lana.Constants;
import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryService;
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
import org.springframework.security.acls.domain.BasePermission;

/**
 * Laboratory dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LaboratoryDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(LaboratoryDialogPresenter.class);
  private LaboratoryDialog dialog;
  private Binder<Laboratory> binder = new BeanValidationBinder<>(Laboratory.class);
  private Laboratory laboratory;
  @Autowired
  private LaboratoryService laboratoryService;
  @Autowired
  private AuthorizationService authorizationService;

  protected LaboratoryDialogPresenter() {
  }

  protected LaboratoryDialogPresenter(LaboratoryService laboratoryService,
      AuthorizationService authorizationService) {
    this.laboratoryService = laboratoryService;
    this.authorizationService = authorizationService;
  }

  void init(LaboratoryDialog dialog) {
    this.dialog = dialog;
    setLaboratory(null);
  }

  void localeChange(Locale locale) {
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(dialog.name).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(NAME);
    updateReadOnly();
  }

  private void updateReadOnly() {
    boolean readOnly = !authorizationService.hasPermission(laboratory, BasePermission.WRITE);
    binder.setReadOnly(readOnly);
    dialog.buttonsLayout.setVisible(!readOnly);
  }

  BinderValidationStatus<Laboratory> validateLaboratory() {
    return binder.validate();
  }

  private boolean validate() {
    return validateLaboratory().isOk();
  }

  void save() {
    if (validate()) {
      logger.debug("Save laboratory {}", laboratory);
      laboratoryService.save(laboratory);
      dialog.close();
      dialog.fireSavedEvent();
    }
  }

  void cancel() {
    dialog.close();
  }

  Laboratory getLaboratory() {
    return laboratory;
  }

  void setLaboratory(Laboratory laboratory) {
    if (laboratory == null) {
      laboratory = new Laboratory();
    }
    this.laboratory = laboratory;
    binder.setBean(laboratory);
    updateReadOnly();
  }
}
