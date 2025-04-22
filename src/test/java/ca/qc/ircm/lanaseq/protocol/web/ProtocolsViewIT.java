package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import com.vaadin.testbench.BrowserTest;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ProtocolsView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolsViewIT extends AbstractBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolsView.class);
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
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    assertTrue(optional(view::protocols).isPresent());
    assertTrue(optional(view::add).isPresent());
    assertTrue(optional(view::edit).isPresent());
    assertFalse(optional(view::history).isPresent());
  }

  @BrowserTest
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void fieldsExistence_Manager() {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    assertTrue(optional(view::protocols).isPresent());
    assertTrue(optional(view::add).isPresent());
    assertTrue(optional(view::edit).isPresent());
    assertTrue(optional(view::history).isPresent());
  }

  @BrowserTest
  public void edit() {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();

    view.protocols().select(0);
    view.edit().click();

    assertTrue(view.dialog().isOpen());
  }

  @BrowserTest
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void history() {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();

    view.protocols().select(2);
    view.history().click();

    assertTrue(view.historyDialog().isOpen());
  }

  @BrowserTest
  public void add() {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();

    view.add().click();

    assertTrue(view.dialog().isOpen());
  }
}
