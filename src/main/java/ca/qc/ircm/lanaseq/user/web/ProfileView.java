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
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Profile view.
 */
@Route(value = ProfileView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed(USER)
public class ProfileView extends VerticalLayout
    implements LocaleChangeObserver, HasDynamicTitle, NotificationComponent {
  public static final String VIEW_NAME = "profile";
  public static final String ID = "profile-view";
  public static final String HEADER = "header";
  public static final String SAVED = "saved";
  private static final long serialVersionUID = 1252966315920684518L;
  private static final Logger logger = LoggerFactory.getLogger(ProfileView.class);
  protected H2 header = new H2();
  protected UserForm form;
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  private transient UserService service;
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected ProfileView(UserService service, AuthenticatedUser authenticatedUser, UserForm form) {
    this.service = service;
    this.authenticatedUser = authenticatedUser;
    this.form = form;
  }

  /**
   * Initializes user dialog.
   */
  @PostConstruct
  protected void init() {
    logger.debug("profile view");
    setId(ID);
    setMaxWidth("40em");
    add(header, form, save);
    header.setId(HEADER);
    save.setId(SAVE);
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
    form.setUser(authenticatedUser.getUser().orElse(null));
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(ProfileView.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    save.setText(webResources.message(SAVE));
    header.setText(resources.message(HEADER));
  }

  @Override
  public String getPageTitle() {
    final AppResources resources = new AppResources(getClass(), getLocale());
    final AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }

  void save() {
    if (form.isValid()) {
      User user = form.getUser();
      String password = form.getPassword();
      logger.debug("save user {}", user);
      service.save(user, password);
      final AppResources resources = new AppResources(ProfileView.class, getLocale());
      showNotification(resources.message(SAVED, user.getEmail()));
    }
  }
}
