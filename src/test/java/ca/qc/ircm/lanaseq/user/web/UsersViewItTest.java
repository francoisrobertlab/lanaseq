package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.UsersView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.security.web.AccessDeniedViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import ca.qc.ircm.lanaseq.web.ViewLayoutElement;
import com.vaadin.testbench.BrowserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link UsersView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UsersViewItTest extends AbstractBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(UsersView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Autowired
  private MessageSource messageSource;

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  @WithAnonymousUser
  public void security_Anonymous() {
    open();

    $(SigninViewElement.class).waitForFirst();
  }

  @BrowserTest
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void security_User() {
    open();

    $(AccessDeniedViewElement.class).waitForFirst();
  }

  @BrowserTest
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void security_Manager() {
    open();

    $(UsersViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void security_Admin() {
    open();

    $(UsersViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void title() {
    open();

    String applicationName = messageSource.getMessage(CONSTANTS_PREFIX + APPLICATION_NAME, null,
        currentLocale());
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + TITLE, new Object[]{applicationName},
            currentLocale()), getDriver().getTitle());
  }

  @BrowserTest
  public void fieldsExistence() {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();
    assertTrue(optional(view::users).isPresent());
    assertFalse(optional(view::switchFailed).isPresent());
    assertTrue(optional(view::add).isPresent());
    assertTrue(optional(view::edit).isPresent());
    assertTrue(optional(view::switchUser).isPresent());
  }

  @BrowserTest
  public void edit() {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();

    view.users().select(0);
    view.edit().click();

    assertTrue(view.dialog().isOpen());
  }

  @BrowserTest
  public void add() {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();

    view.add().click();

    assertTrue(view.dialog().isOpen());
  }

  @BrowserTest
  public void switchUser() {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();
    view.users().select(2);

    view.switchUser().click();

    $(DatasetsViewElement.class).waitForFirst();
    ViewLayoutElement viewReload = $(ViewLayoutElement.class).waitForFirst();
    assertTrue(optional(viewReload::exitSwitchUser).isPresent());
    assertFalse(optional(viewReload::users).isPresent());
  }

  @BrowserTest
  @Disabled("Admins are allowed to switch to another admin right now")
  public void switchUser_Fail() {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();
    view.users().select(0);

    view.switchUser().click();

    assertTrue(optional(view::switchFailed).isPresent());
  }
}
