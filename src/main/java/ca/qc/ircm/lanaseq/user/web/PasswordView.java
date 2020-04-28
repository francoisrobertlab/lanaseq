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

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.PRIMARY;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Change password view.
 */
@Route(value = PasswordView.VIEW_NAME)
public class PasswordView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {
  public static final String VIEW_NAME = "password";
  public static final String HEADER = "header";
  private static final long serialVersionUID = -8554355390432590290L;
  protected H2 header = new H2();
  protected PasswordsForm passwords = new PasswordsForm();
  protected Button save = new Button();
  @Autowired
  private PasswordViewPresenter presenter;

  protected PasswordView() {
  }

  protected PasswordView(PasswordViewPresenter presenter) {
    this.presenter = presenter;
  }

  @PostConstruct
  void init() {
    setId(VIEW_NAME);
    add(header, passwords, save);
    header.setId(HEADER);
    passwords.setResponsiveSteps(new ResponsiveStep("30em", 1));
    passwords.setMaxWidth("30em");
    save.setId(SAVE);
    save.setThemeName(PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save());
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    AppResources resources = new AppResources(PasswordView.class, getLocale());
    AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER));
    save.setText(webResources.message(SAVE));
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(PasswordView.class, getLocale());
    AppResources webResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, webResources.message(APPLICATION_NAME));
  }
}
