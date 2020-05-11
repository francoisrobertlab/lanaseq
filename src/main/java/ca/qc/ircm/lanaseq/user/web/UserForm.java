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

package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.user.UserProperties.ADMIN;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.MANAGER;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.user.LaboratoryProperties;
import ca.qc.ircm.lanaseq.user.User;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.SpringComponent;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * User form.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserForm extends FormLayout implements LocaleChangeObserver {
  private static final long serialVersionUID = 3285639770914046262L;
  public static final String ID = "user-form";
  public static final String CREATE_NEW_LABORATORY = "createNewLaboratory";
  public static final String LABORATORY_NAME = LaboratoryProperties.NAME;
  public static final String NEW_LABORATORY_NAME = "newLaboratoryName";
  protected TextField email = new TextField();
  protected TextField name = new TextField();
  protected Checkbox admin = new Checkbox();
  protected Checkbox manager = new Checkbox();
  protected PasswordsForm passwords = new PasswordsForm();
  @Autowired
  private transient UserFormPresenter presenter;

  protected UserForm() {
  }

  protected UserForm(UserFormPresenter presenter) {
    this.presenter = presenter;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  /**
   * Initializes user dialog.
   */
  @PostConstruct
  protected void init() {
    setId(ID);
    setResponsiveSteps(new ResponsiveStep("30em", 1));
    add(new FormLayout(email, name, admin, manager, passwords));
    email.setId(id(EMAIL));
    name.setId(id(NAME));
    admin.setId(id(ADMIN));
    manager.setId(id(MANAGER));
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources userResources = new AppResources(User.class, getLocale());
    email.setLabel(userResources.message(EMAIL));
    name.setLabel(userResources.message(NAME));
    admin.setLabel(userResources.message(ADMIN));
    manager.setLabel(userResources.message(MANAGER));
    presenter.localeChange(getLocale());
  }

  public boolean isValid() {
    return presenter.isValid();
  }

  public String getPassword() {
    return presenter.getPassword();
  }

  public User getUser() {
    return presenter.getUser();
  }

  public void setUser(User user) {
    presenter.setUser(user);
  }
}
