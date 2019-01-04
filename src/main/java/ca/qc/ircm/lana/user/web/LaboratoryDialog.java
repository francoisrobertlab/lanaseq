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
import static ca.qc.ircm.lana.web.WebConstants.CANCEL;
import static ca.qc.ircm.lana.web.WebConstants.PRIMARY;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;
import static ca.qc.ircm.lana.web.WebConstants.THEME;

import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.SpringComponent;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Laboratory dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LaboratoryDialog extends Dialog implements LocaleChangeObserver, BaseComponent {
  private static final long serialVersionUID = 3285639770914046262L;
  public static final String CLASS_NAME = "laboratory-dialog";
  public static final String HEADER = "header";
  protected H2 header = new H2();
  protected TextField name = new TextField();
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  protected Button cancel = new Button();
  @Inject
  private LaboratoryDialogPresenter presenter;

  protected LaboratoryDialog() {
  }

  protected LaboratoryDialog(LaboratoryDialogPresenter presenter) {
    this.presenter = presenter;
  }

  @PostConstruct
  void init() {
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
    save.addClickListener(e -> presenter.save());
    buttonsLayout.add(cancel);
    cancel.addClassName(CANCEL);
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> presenter.cancel());
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final MessageResource laboratoryResources = new MessageResource(Laboratory.class, getLocale());
    final MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    updateHeader();
    name.setLabel(laboratoryResources.message(NAME));
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
  }

  private void updateHeader() {
    final MessageResource resources = new MessageResource(LaboratoryDialog.class, getLocale());
    Laboratory laboratory = presenter.getLaboratory();
    if (laboratory != null && laboratory.getId() != null) {
      header.setText(resources.message(HEADER, 1, laboratory.getName()));
    } else {
      header.setText(resources.message(HEADER, 0));
    }
  }

  public Laboratory getLaboratory() {
    return presenter.getLaboratory();
  }

  /**
   * Sets laboratory.
   *
   * @param laboratory
   *          laboratory
   */
  public void setLaboratory(Laboratory laboratory) {
    presenter.setLaboratory(laboratory);
    updateHeader();
  }
}
