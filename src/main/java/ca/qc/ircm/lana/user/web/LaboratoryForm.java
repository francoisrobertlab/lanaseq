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

import static ca.qc.ircm.lana.user.UserProperties.NAME;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;

import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

/**
 * Laboratory form.
 */
public class LaboratoryForm extends Composite<VerticalLayout>
    implements LocaleChangeObserver, BaseComponent {
  private static final long serialVersionUID = 7602423333455397115L;
  public static final String CLASS_NAME = "laboratory-form";
  protected TextField name = new TextField();
  private Binder<Laboratory> binder = new BeanValidationBinder<>(Laboratory.class);
  private boolean readOnly;

  /**
   * Creates a new {@link LaboratoryForm}.
   */
  public LaboratoryForm() {
    VerticalLayout root = getContent();
    root.setPadding(false);
    root.addClassName(CLASS_NAME);
    root.add(name);
    name.addClassName(NAME);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final MessageResource laboratoryResources = new MessageResource(Laboratory.class, getLocale());
    final MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    name.setLabel(laboratoryResources.message(NAME));
    binder.forField(name).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .bind(NAME);
    setReadOnly(readOnly);
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Sets if form should be read only.
   *
   * @param readOnly
   *          read only
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    binder.setReadOnly(readOnly);
  }

  public Laboratory getLaboratory() {
    return binder.getBean();
  }

  /**
   * Sets laboratory.
   *
   * @param laboratory
   *          laboratory
   */
  public void setLaboratory(Laboratory laboratory) {
    binder.setBean(laboratory);
  }

  /**
   * Validates form.
   *
   * @return validation status
   */
  public BinderValidationStatus<Laboratory> validate() {
    return binder.validate();
  }
}
