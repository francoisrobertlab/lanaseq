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

import static ca.qc.ircm.lana.text.Strings.styleName;
import static ca.qc.ircm.lana.user.UserProperties.ADMIN;
import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.UserProperties.MANAGER;
import static ca.qc.ircm.lana.user.UserProperties.NAME;
import static ca.qc.ircm.lana.web.WebConstants.BORDER;
import static ca.qc.ircm.lana.web.WebConstants.CANCEL;
import static ca.qc.ircm.lana.web.WebConstants.PRIMARY;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;
import static ca.qc.ircm.lana.web.WebConstants.THEME;

import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryProperties;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.web.SavedEvent;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Users dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserDialog extends Dialog implements LocaleChangeObserver, BaseComponent {
  private static final long serialVersionUID = 3285639770914046262L;
  public static final String CLASS_NAME = "user-dialog";
  public static final String HEADER = "header";
  public static final String CREATE_NEW_LABORATORY = "createNewLaboratory";
  public static final String LABORATORY_NAME = LaboratoryProperties.NAME;
  protected H2 header = new H2();
  protected TextField email = new TextField();
  protected TextField name = new TextField();
  protected Checkbox admin = new Checkbox();
  protected Checkbox manager = new Checkbox();
  protected Checkbox createNewLaboratory = new Checkbox();
  protected PasswordsForm passwords = new PasswordsForm();
  protected ComboBox<Laboratory> laboratory = new ComboBox<>();
  protected VerticalLayout newLaboratoryLayout = new VerticalLayout();
  protected H6 newLaboratoryHeader = new H6();
  protected TextField newLaboratoryName = new TextField();
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  protected Button cancel = new Button();
  @Autowired
  private transient UserDialogPresenter presenter;

  protected UserDialog() {
  }

  protected UserDialog(UserDialogPresenter presenter) {
    this.presenter = presenter;
  }

  /**
   * Initializes user dialog.
   */
  @PostConstruct
  protected void init() {
    setId(CLASS_NAME);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.add(header, email, name, admin, manager, passwords, createNewLaboratory, laboratory,
        newLaboratoryLayout, buttonsLayout);
    newLaboratoryLayout.add(newLaboratoryHeader, newLaboratoryName);
    buttonsLayout.add(save, cancel);
    header.addClassName(HEADER);
    email.addClassName(EMAIL);
    name.addClassName(NAME);
    admin.addClassName(ADMIN);
    manager.addClassName(MANAGER);
    createNewLaboratory.addClassName(CREATE_NEW_LABORATORY);
    laboratory.addClassName(LABORATORY);
    newLaboratoryLayout.addClassName(BORDER);
    newLaboratoryName.addClassName(styleName(LABORATORY, LABORATORY_NAME));
    save.addClassName(SAVE);
    save.getElement().setAttribute(THEME, PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save());
    cancel.addClassName(CANCEL);
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> presenter.cancel());
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final MessageResource resources = new MessageResource(UserDialog.class, getLocale());
    final MessageResource userResources = new MessageResource(User.class, getLocale());
    final MessageResource laboratoryResources = new MessageResource(Laboratory.class, getLocale());
    final MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    updateHeader();
    email.setLabel(userResources.message(EMAIL));
    name.setLabel(userResources.message(NAME));
    admin.setLabel(userResources.message(ADMIN));
    manager.setLabel(userResources.message(MANAGER));
    createNewLaboratory.setLabel(resources.message(CREATE_NEW_LABORATORY));
    laboratory.setLabel(userResources.message(LABORATORY));
    newLaboratoryHeader.setText(userResources.message(LABORATORY));
    newLaboratoryName.setLabel(laboratoryResources.message(LABORATORY_NAME));
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
    presenter.localeChange(getLocale());
  }

  private void updateHeader() {
    final MessageResource resources = new MessageResource(UserDialog.class, getLocale());
    if (presenter.getUser() != null && presenter.getUser().getId() != null) {
      header.setText(resources.message(HEADER, 1, presenter.getUser().getName()));
    } else {
      header.setText(resources.message(HEADER, 0));
    }
  }

  /**
   * Adds listener to be informed when a user was saved.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration addSavedListener(ComponentEventListener<SavedEvent<UserDialog>> listener) {
    return addListener((Class) SavedEvent.class, listener);
  }

  void fireSavedEvent() {
    fireEvent(new SavedEvent<>(this, true));
  }

  public User getUser() {
    return presenter.getUser();
  }

  /**
   * Sets user.
   *
   * @param user
   *          user
   */
  public void setUser(User user) {
    presenter.setUser(user);
    updateHeader();
  }
}
