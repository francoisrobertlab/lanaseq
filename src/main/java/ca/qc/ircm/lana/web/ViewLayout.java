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

package ca.qc.ircm.lana.web;

import ca.qc.ircm.lana.experiment.web.ExperimentsView;
import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.security.web.WebSecurityConfiguration;
import ca.qc.ircm.lana.user.web.SigninView;
import ca.qc.ircm.lana.user.web.UsersView;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

/**
 * Main layout.
 */
@HtmlImport("styles/shared-styles.html")
public class ViewLayout extends VerticalLayout implements RouterLayout, LocaleChangeObserver,
    BeforeEnterObserver, AfterNavigationObserver, BaseComponent {
  public static final String HOME = "home";
  public static final String USERS = "users";
  public static final String SIGNOUT = "signout";
  private static final long serialVersionUID = 710800815636494374L;
  private static final Logger logger = LoggerFactory.getLogger(ViewLayout.class);
  protected Tabs tabs = new Tabs();
  protected Tab home = new Tab();
  protected Tab users = new Tab();
  protected Tab signout = new Tab();
  private Map<Tab, String> tabsHref = new HashMap<>();
  private String currentHref;
  @Inject
  private transient AuthorizationService authorizationService;

  protected ViewLayout() {
  }

  protected ViewLayout(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  @PostConstruct
  void init() {
    add(tabs);
    tabs.add(home, users, signout);
    tabsHref.put(home, ExperimentsView.VIEW_NAME);
    tabsHref.put(users, UsersView.VIEW_NAME);
    tabs.addSelectedChangeListener(e -> selectTab());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    MessageResource resources = new MessageResource(ViewLayout.class, getLocale());
    home.setLabel(resources.message(HOME));
    users.setLabel(resources.message(USERS));
    signout.setLabel(resources.message(SIGNOUT));
  }

  private void selectTab() {
    if (tabs.getSelectedTab() == signout) {
      // Sign-out requires a request to be made outside of Vaadin.
      logger.debug("Redirect to sign out");
      getCurrentUi().getPage()
          .executeJavaScript("location.assign('" + WebSecurityConfiguration.SIGNOUT_URL + "')");
    } else {
      if (!currentHref.equals(tabsHref.get(tabs.getSelectedTab()))) {
        logger.debug("Navigate to {}", tabsHref.get(tabs.getSelectedTab()));
        navigate(tabsHref.get(tabs.getSelectedTab()));
      }
    }
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    if (!authorizationService.isAuthorized(event.getNavigationTarget())) {
      if (authorizationService.isAnonymous()) {
        event.rerouteTo(SigninView.class);
      } else {
        MessageResource resources = new MessageResource(ViewLayout.class, getLocale());
        String message = resources.message(AccessDeniedException.class.getSimpleName(),
            authorizationService.currentUser().getEmail(),
            event.getNavigationTarget().getSimpleName());
        logger.info(message);
        event.rerouteToError(new AccessDeniedException(message), message);
      }
    }
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    currentHref = event.getLocation().getFirstSegment();
    Optional<Tab> currentTab = tabsHref.entrySet().stream()
        .filter(e -> e.getValue().equals(currentHref)).map(e -> e.getKey()).findFirst();
    currentTab.ifPresent(tab -> tabs.setSelectedTab(tab));
  }
}
