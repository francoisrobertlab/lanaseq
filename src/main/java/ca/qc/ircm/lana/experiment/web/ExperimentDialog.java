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
import static ca.qc.ircm.lana.web.WebConstants.CANCEL;
import static ca.qc.ircm.lana.web.WebConstants.PRIMARY;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;
import static ca.qc.ircm.lana.web.WebConstants.THEME;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.web.SaveEvent;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Experiment dialog.
 */
public class ExperimentDialog extends Dialog implements LocaleChangeObserver, BaseComponent {
  private static final Logger logger = LoggerFactory.getLogger(ExperimentDialog.class);
  private static final long serialVersionUID = 3285639770914046262L;
  public static final String CLASS_NAME = "experiment-dialog";
  public static final String HEADER = "header";
  protected H2 header = new H2();
  protected TextField name = new TextField();
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  protected Button cancel = new Button();
  private Binder<Experiment> binder = new BeanValidationBinder<>(Experiment.class);
  private Experiment experiment;
  private boolean readOnly;

  /**
   * Creates a new ExperimentDialog.
   */
  public ExperimentDialog() {
    setId(CLASS_NAME);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.add(header, name);
    header.addClassName(HEADER);
    name.addClassName(NAME);
    layout.add(buttonsLayout);
    buttonsLayout.add(save);
    save.addClassName(SAVE);
    save.getElement().setAttribute(THEME, PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
    buttonsLayout.add(cancel);
    cancel.addClassName(CANCEL);
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> close());
    setExperiment(null);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final MessageResource experimentResources = new MessageResource(Experiment.class, getLocale());
    final MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    updateHeader();
    name.setLabel(experimentResources.message(NAME));
    binder.forField(name).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .bind(NAME);
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
    setReadOnly(readOnly);
  }

  private void updateHeader() {
    final MessageResource resources = new MessageResource(ExperimentDialog.class, getLocale());
    if (experiment != null && experiment.getId() != null) {
      header.setText(resources.message(HEADER, 1, experiment.getName()));
    } else {
      header.setText(resources.message(HEADER, 0));
    }
  }

  BinderValidationStatus<Experiment> validateExperiment() {
    return binder.validate();
  }

  private boolean validate() {
    return validateExperiment().isOk();
  }

  private void save() {
    if (validate()) {
      logger.debug("Fire save event for experiment {}", experiment);
      fireEvent(new SaveEvent<>(this, false, experiment));
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration addSaveListener(ComponentEventListener<SaveEvent<Experiment>> listener) {
    return addListener((Class) SaveEvent.class, listener);
  }

  void fireClickSave() {
    save();
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Sets if dialog should be read only.
   *
   * @param readOnly
   *          read only
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    binder.setReadOnly(readOnly);
    buttonsLayout.setVisible(!readOnly);
  }

  public Experiment getExperiment() {
    return experiment;
  }

  /**
   * Sets experiment.
   *
   * @param experiment
   *          experiment
   */
  public void setExperiment(Experiment experiment) {
    if (experiment == null) {
      experiment = new Experiment();
    }
    this.experiment = experiment;
    binder.setBean(experiment);
    updateHeader();
  }
}
