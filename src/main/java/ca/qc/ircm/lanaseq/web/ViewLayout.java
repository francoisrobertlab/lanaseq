package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.files.web.PublicFilesView;
import ca.qc.ircm.lanaseq.message.Message;
import ca.qc.ircm.lanaseq.message.MessageService;
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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;
import jakarta.annotation.PostConstruct;
import java.io.Serial;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

/**
 * Main layout.
 */
public class ViewLayout extends AppLayout implements RouterLayout, LocaleChangeObserver,
    AfterNavigationObserver, UrlComponent {

  public static final String ID = "view-layout";
  public static final String HEADER = "header";
  public static final String LABORATORY = "laboratory";
  public static final String DRAWER_TOGGLE = "drawerToggle";
  public static final String SIDE_NAV = styleName(ID, "sidenav");
  public static final String DATASETS = "datasets";
  public static final String SAMPLES = "samples";
  public static final String PUBLIC_FILES = "publicFiles";
  public static final String PROTOCOLS = "protocols";
  public static final String PROFILE = "profile";
  public static final String USERS = "users";
  public static final String EXIT_SWITCH_USER = "exitSwitchUser";
  public static final String EXIT_SWITCH_USER_FORM = "exitSwitchUserform";
  public static final String SIGNOUT = "signout";
  public static final String NAV = "nav";
  public static final String NOTIFICATIONS = "notifications";
  public static final String NOTIFICATIONS_COUNT = property(NOTIFICATIONS, "count");
  public static final String NOTIFICATIONS_POPOVER = property(NOTIFICATIONS, "popover");
  public static final String NOTIFICATIONS_HEADER = property(NOTIFICATIONS, "header");
  public static final String NOTIFICATIONS_MARK_AS_READ = property(NOTIFICATIONS, "markAsRead");
  public static final String NOTIFICATIONS_LIST = property(NOTIFICATIONS, "list");
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
  protected SideNavItem publicFiles;
  protected SideNavItem profile;
  protected SideNavItem users;
  protected SideNavItem exitSwitchUser;
  protected SideNavItem signout;
  protected Button notifications = new Button();
  protected Span notificationsCount = new Span();
  protected Popover notificationsPopover = new Popover();
  protected H4 notificationsHeader = new H4();
  protected Button notificationsMarkAsRead = new Button();
  protected MessageList notificationsList = new MessageList();
  private List<Message> messages;
  private final transient AuthenticatedUser authenticatedUser;
  private final transient MessageService messageService;

  @Autowired
  protected ViewLayout(AuthenticatedUser authenticatedUser, MessageService messageService) {
    this.authenticatedUser = authenticatedUser;
    this.messageService = messageService;
  }

  @PostConstruct
  void init() {
    setId(ID);
    addToDrawer(applicationName, sideNav, notifications);
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
    publicFiles = new SideNavItem("Public files", PublicFilesView.class,
        VaadinIcon.UNLOCK.create());
    publicFiles.setId(styleName(PUBLIC_FILES, NAV));
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
    sideNav.addItem(datasets, samples, protocols, publicFiles, profile, users, exitSwitchUser,
        signout);
    notifications.setId(styleName(ID, NOTIFICATIONS));
    notifications.setIcon(VaadinIcon.BELL.create());
    notifications.setSuffixComponent(notificationsCount);
    notifications.setWidthFull();
    notifications.getStyle().setCursor("pointer").setMarginTop("2em");
    notifications.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_TERTIARY);
    notificationsCount.setId(styleName(ID, NOTIFICATIONS_COUNT));
    notificationsCount.getElement().getThemeList().add("badge pill");
    notificationsCount.getStyle().setMarginLeft("1em");
    notificationsPopover.setId(styleName(ID, NOTIFICATIONS_POPOVER));
    notificationsPopover.setTarget(notifications);
    notificationsPopover.setPosition(PopoverPosition.END);
    notificationsPopover.setModal(true);
    notificationsPopover.setBackdropVisible(true);
    notificationsPopover.setAriaLabelledBy(styleName(ID, NOTIFICATIONS));
    notificationsPopover.add(new VerticalLayout(
        new HorizontalLayout(Alignment.CENTER, notificationsHeader, notificationsMarkAsRead),
        notificationsList));
    notificationsHeader.setId(styleName(ID, NOTIFICATIONS_HEADER));
    notificationsMarkAsRead.setId(styleName(ID, NOTIFICATIONS_MARK_AS_READ));
    notificationsMarkAsRead.addClickListener(e -> markNotificationsAsRead());
    notificationsList.setId(styleName(ID, NOTIFICATIONS_LIST));
    updateNotifications();
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    applicationName.setText(getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
    laboratory.setText(getTranslation(MESSAGE_PREFIX + LABORATORY));
    datasets.setLabel(getTranslation(MESSAGE_PREFIX + DATASETS));
    samples.setLabel(getTranslation(MESSAGE_PREFIX + SAMPLES));
    protocols.setLabel(getTranslation(MESSAGE_PREFIX + PROTOCOLS));
    publicFiles.setLabel(getTranslation(MESSAGE_PREFIX + PUBLIC_FILES));
    profile.setLabel(getTranslation(MESSAGE_PREFIX + PROFILE));
    users.setLabel(getTranslation(MESSAGE_PREFIX + USERS));
    exitSwitchUser.setLabel(getTranslation(MESSAGE_PREFIX + EXIT_SWITCH_USER));
    signout.setLabel(getTranslation(MESSAGE_PREFIX + SIGNOUT));
    notifications.setText(getTranslation(MESSAGE_PREFIX + NOTIFICATIONS));
    notificationsHeader.setText(getTranslation(MESSAGE_PREFIX + NOTIFICATIONS_HEADER));
    notificationsMarkAsRead.setText(getTranslation(MESSAGE_PREFIX + NOTIFICATIONS_MARK_AS_READ));
  }

  /**
   * Updates notifications count to show to user.
   */
  public void updateNotifications() {
    messages = messageService.allUnread();
    notificationsList.setItems(messages.stream().map(this::messageComponent).toList());
    notificationsCount.getElement().getThemeList().remove("error contrast");
    notificationsCount.setText(String.valueOf(messages.size()));
    notificationsCount.getElement().getThemeList().add(messages.isEmpty() ? "contrast" : "error");
  }

  private MessageListItem messageComponent(Message message) {
    MessageListItem item = new MessageListItem(message.getMessage(),
        message.getDate().atZone(ZoneOffset.systemDefault()).toInstant(),
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
    if (message.getColor() != null) {
      item.addClassNames(message.getColor());
    }
    return item;
  }

  private void markNotificationsAsRead() {
    messages.forEach(message -> {
      message.setUnread(false);
      messageService.save(message);
    });
    updateNotifications();
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    users.setVisible(authenticatedUser.isAuthorized(UsersView.class));
    exitSwitchUser.setVisible(
        authenticatedUser.hasRole(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR));
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
    } else if (view instanceof PublicFilesView) {
      return Optional.of(publicFiles);
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
