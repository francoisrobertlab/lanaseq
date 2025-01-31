package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolsView;
import ca.qc.ircm.lanaseq.sample.web.SamplesView;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.web.ExitSwitchUserView;
import ca.qc.ircm.lanaseq.user.web.ProfileView;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import ca.qc.ircm.lanaseq.web.component.UrlComponent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;
import jakarta.annotation.PostConstruct;
import java.io.Serial;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

/**
 * Main layout.
 */
@JsModule("./styles/shared-styles.js")
public class ViewLayout extends AppLayout
    implements RouterLayout, LocaleChangeObserver, AfterNavigationObserver, UrlComponent {

  public static final String ID = "view-layout";
  public static final String HEADER = "header";
  public static final String LABORATORY = "laboratory";
  public static final String DRAWER_TOGGLE = "drawerToggle";
  public static final String SIDE_NAV = styleName(ID, "sidenav");
  public static final String DATASETS = "datasets";
  public static final String SAMPLES = "samples";
  public static final String PROTOCOLS = "protocols";
  public static final String PROFILE = "profile";
  public static final String USERS = "users";
  public static final String EXIT_SWITCH_USER = "exitSwitchUser";
  public static final String EXIT_SWITCH_USER_FORM = "exitSwitchUserform";
  public static final String SIGNOUT = "signout";
  public static final String NAV = "nav";
  private static final String MESSAGE_PREFIX = messagePrefix(ViewLayout.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Serial
  private static final long serialVersionUID = 710800815636494374L;
  private static final Logger logger = LoggerFactory.getLogger(ViewLayout.class);
  protected H1 applicationName = new H1();
  protected H2 header = new H2();
  protected H1 laboratory = new H1();
  protected DrawerToggle drawerToggle = new DrawerToggle();
  protected SideNav sideNav = new SideNav();
  protected SideNavItem datasets;
  protected SideNavItem samples;
  protected SideNavItem protocols;
  protected SideNavItem profile;
  protected SideNavItem users;
  protected SideNavItem exitSwitchUser;
  protected SideNavItem signout;
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected ViewLayout(AuthenticatedUser authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
  }

  @PostConstruct
  void init() {
    setId(ID);
    addToDrawer(applicationName, sideNav);
    addToNavbar(drawerToggle, header, laboratory);
    setPrimarySection(Section.DRAWER);
    applicationName.setId(styleName(APPLICATION_NAME));
    applicationName.getStyle().set("font-size", "var(--lumo-font-size-l)")
        .set("line-height", "var(--lumo-size-l)")
        .set("margin", "var(--lumo-space-s) var(--lumo-space-m)");
    header.setId(styleName(ID, HEADER));
    header.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");
    header.setWidthFull();
    laboratory.setId(styleName(ID, LABORATORY));
    laboratory.setMinWidth("15em");
    laboratory.getStyle().set("font-size", "var(--lumo-font-size-l)").set("text-align", "right")
        .set("margin", "0 var(--lumo-space-m)");
    drawerToggle.setId(DRAWER_TOGGLE);
    sideNav.setId(SIDE_NAV);
    datasets = new SideNavItem("Datasets", DatasetsView.class, VaadinIcon.FLASK.create());
    datasets.setId(styleName(DATASETS, NAV));
    samples = new SideNavItem("Samples", SamplesView.class, VaadinIcon.EYEDROPPER.create());
    samples.setId(styleName(SAMPLES, NAV));
    protocols = new SideNavItem("Protocols", ProtocolsView.class, VaadinIcon.BOOK.create());
    protocols.setId(styleName(PROTOCOLS, NAV));
    profile = new SideNavItem("Profile", ProfileView.class, VaadinIcon.USER.create());
    profile.setId(styleName(PROFILE, NAV));
    users = new SideNavItem("Users", UsersView.class, VaadinIcon.GROUP.create());
    users.setId(styleName(USERS, NAV));
    users.setVisible(false);
    exitSwitchUser = new SideNavItem("Exit switch user", ExitSwitchUserView.class,
        VaadinIcon.LEVEL_LEFT.create());
    exitSwitchUser.setId(styleName(EXIT_SWITCH_USER, NAV));
    exitSwitchUser.setVisible(false);
    signout = new SideNavItem("Signout", SignoutView.class, VaadinIcon.SIGN_OUT.create());
    signout.setId(styleName(SIGNOUT, NAV));
    sideNav.addItem(datasets, samples, protocols, profile, users, exitSwitchUser, signout);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    applicationName.setText(getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
    laboratory.setText(getTranslation(MESSAGE_PREFIX + LABORATORY));
    datasets.setLabel(getTranslation(MESSAGE_PREFIX + DATASETS));
    samples.setLabel(getTranslation(MESSAGE_PREFIX + SAMPLES));
    protocols.setLabel(getTranslation(MESSAGE_PREFIX + PROTOCOLS));
    profile.setLabel(getTranslation(MESSAGE_PREFIX + PROFILE));
    users.setLabel(getTranslation(MESSAGE_PREFIX + USERS));
    exitSwitchUser.setLabel(getTranslation(MESSAGE_PREFIX + EXIT_SWITCH_USER));
    signout.setLabel(getTranslation(MESSAGE_PREFIX + SIGNOUT));
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    users.setVisible(authenticatedUser.isAuthorized(UsersView.class));
    exitSwitchUser
        .setVisible(authenticatedUser.hasRole(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR));
    Optional<SideNavItem> currentNav = selectedSideNavItem();
    currentNav.ifPresentOrElse(item -> header.setText(item.getLabel()), () -> header.setText(""));
  }

  Optional<SideNavItem> selectedSideNavItem() {
    Component view = UI.getCurrent().getCurrentView();
    if (view instanceof DatasetsView) {
      return Optional.of(datasets);
    } else if (view instanceof SamplesView) {
      return Optional.of(samples);
    } else if (view instanceof ProtocolsView) {
      return Optional.of(protocols);
    } else if (view instanceof ProfileView) {
      return Optional.of(profile);
    } else if (view instanceof UsersView) {
      return Optional.of(users);
    } else if (view instanceof ExitSwitchUserView) {
      return Optional.of(exitSwitchUser);
    } else if (view instanceof SignoutView) {
      return Optional.of(signout);
    } else {
      return Optional.empty();
    }
  }
}
