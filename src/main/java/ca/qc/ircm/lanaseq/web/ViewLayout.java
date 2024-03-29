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

package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolsView;
import ca.qc.ircm.lanaseq.sample.web.SamplesView;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.SwitchUserService;
import ca.qc.ircm.lanaseq.user.web.ProfileView;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import ca.qc.ircm.lanaseq.web.component.UrlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

/**
 * Main layout.
 */
@JsModule("./styles/shared-styles.js")
public class ViewLayout extends VerticalLayout
    implements RouterLayout, LocaleChangeObserver, AfterNavigationObserver, UrlComponent {
  public static final String ID = "view-layout";
  public static final String TABS = styleName(ID, "tabs");
  public static final String DATASETS = "datasets";
  public static final String SAMPLES = "samples";
  public static final String PROTOCOLS = "protocols";
  public static final String PROFILE = "profile";
  public static final String USERS = "users";
  public static final String EXIT_SWITCH_USER = "exitSwitchUser";
  public static final String EXIT_SWITCH_USER_FORM = "exitSwitchUserform";
  public static final String SIGNOUT = "signout";
  public static final String TAB = "tab";
  private static final long serialVersionUID = 710800815636494374L;
  private static final Logger logger = LoggerFactory.getLogger(ViewLayout.class);
  protected Tabs tabs = new Tabs();
  protected Tab datasets = new Tab();
  protected Tab samples = new Tab();
  protected Tab protocols = new Tab();
  protected Tab profile = new Tab();
  protected Tab users = new Tab();
  protected Tab exitSwitchUser = new Tab();
  protected Tab signout = new Tab();
  private Map<Tab, String> tabsHref = new HashMap<>();
  private String currentHref;
  @Autowired
  private transient SwitchUserService switchUserService;
  @Autowired
  private transient AuthenticatedUser authenticatedUser;

  protected ViewLayout() {
  }

  protected ViewLayout(SwitchUserService switchUserService, AuthenticatedUser authenticatedUser) {
    this.switchUserService = switchUserService;
    this.authenticatedUser = authenticatedUser;
  }

  @PostConstruct
  void init() {
    setId(ID);
    setSizeFull();
    setPadding(false);
    setSpacing(false);
    add(tabs);
    tabs.setId(TABS);
    tabs.add(datasets, samples, protocols, profile, users, exitSwitchUser, signout);
    datasets.setId(styleName(DATASETS, TAB));
    samples.setId(styleName(SAMPLES, TAB));
    protocols.setId(styleName(PROTOCOLS, TAB));
    profile.setId(styleName(PROFILE, TAB));
    users.setId(styleName(USERS, TAB));
    users.setVisible(false);
    exitSwitchUser.setId(styleName(EXIT_SWITCH_USER, TAB));
    exitSwitchUser.setVisible(false);
    signout.setId(styleName(SIGNOUT, TAB));
    tabsHref.put(datasets, DatasetsView.VIEW_NAME);
    tabsHref.put(samples, SamplesView.VIEW_NAME);
    tabsHref.put(protocols, ProtocolsView.VIEW_NAME);
    tabsHref.put(profile, ProfileView.VIEW_NAME);
    tabsHref.put(users, UsersView.VIEW_NAME);
    tabs.addSelectedChangeListener(e -> selectTab());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    AppResources resources = new AppResources(ViewLayout.class, getLocale());
    datasets.setLabel(resources.message(DATASETS));
    samples.setLabel(resources.message(SAMPLES));
    protocols.setLabel(resources.message(PROTOCOLS));
    profile.setLabel(resources.message(PROFILE));
    users.setLabel(resources.message(USERS));
    exitSwitchUser.setLabel(resources.message(EXIT_SWITCH_USER));
    signout.setLabel(resources.message(SIGNOUT));
  }

  private void selectTab() {
    if (tabs.getSelectedTab() == signout) {
      logger.debug("Sign out user {}", authenticatedUser);
      UI.getCurrent().getPage().setLocation(getUrl(MainView.VIEW_NAME));
      CompositeLogoutHandler logoutHandler = new CompositeLogoutHandler(
          new CookieClearingLogoutHandler("remember-me"), new SecurityContextLogoutHandler());
      logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(),
          VaadinServletResponse.getCurrent().getHttpServletResponse(), null);
    } else if (tabs.getSelectedTab() == exitSwitchUser) {
      switchUserService.exitSwitchUser();
      UI.getCurrent().getPage().setLocation(getUrl(MainView.VIEW_NAME));
    } else {
      if (!currentHref.equals(tabsHref.get(tabs.getSelectedTab()))) {
        logger.debug("navigate to {}", tabsHref.get(tabs.getSelectedTab()));
        UI.getCurrent().navigate(tabsHref.get(tabs.getSelectedTab()));
      }
    }
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    users.setVisible(authenticatedUser.isAuthorized(UsersView.class));
    exitSwitchUser
        .setVisible(authenticatedUser.hasRole(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR));
    currentHref = event.getLocation().getFirstSegment();
    Optional<Tab> currentTab = tabsHref.entrySet().stream()
        .filter(e -> e.getValue().equals(currentHref)).map(e -> e.getKey()).findFirst();
    currentTab.ifPresent(tab -> tabs.setSelectedTab(tab));
  }
}
