package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.DELETED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.BrowserTest;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link ProtocolDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolDialogItTest extends AbstractBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolDialog.class);
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private MessageSource messageSource;

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void fieldsExistence() {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(0);
    view.edit().click();
    ProtocolDialogElement dialog = view.dialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::name).isPresent());
    assertTrue(optional(dialog::note).isPresent());
    assertTrue(optional(dialog::upload).isPresent());
    assertTrue(optional(dialog::files).isPresent());
    assertTrue(optional(dialog::save).isPresent());
    assertTrue(optional(dialog::cancel).isPresent());
    assertFalse(optional(dialog::delete).isPresent());
    assertTrue(optional(dialog::confirm).isPresent());
  }

  @BrowserTest
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void fieldsExistence_Deletable() {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(3);
    view.edit().click();
    ProtocolDialogElement dialog = view.dialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::name).isPresent());
    assertTrue(optional(dialog::note).isPresent());
    assertTrue(optional(dialog::upload).isPresent());
    assertTrue(optional(dialog::files).isPresent());
    assertTrue(optional(dialog::save).isPresent());
    assertTrue(optional(dialog::cancel).isPresent());
    assertTrue(optional(dialog::delete).isPresent());
    assertTrue(optional(dialog::confirm).isPresent());
  }

  @BrowserTest
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void delete() {
    open();
    Protocol protocol = repository.findById(4L).get();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(3);
    view.edit().click();
    ProtocolDialogElement dialog = view.dialog();
    final String name = protocol.getName();

    TestTransaction.flagForCommit();
    dialog.delete().click();
    dialog.confirm().getConfirmButton().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + DELETED, new Object[]{name}, currentLocale()),
        notification.getText());
    assertFalse(repository.findById(4L).isPresent());
  }
}
