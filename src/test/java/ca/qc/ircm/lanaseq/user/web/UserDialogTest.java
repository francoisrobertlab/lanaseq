package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.HEADER;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.ID;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.SAVED;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link UserDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UserDialogTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(UserDialog.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private UserDialog dialog;
  @MockitoBean
  private UserService service;
  @Mock
  private ComponentEventListener<SavedEvent<UserDialog>> savedListener;
  @Captor
  private ArgumentCaptor<User> userCaptor;
  @Autowired
  private UserRepository repository;
  private Locale locale = Locale.ENGLISH;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(service.get(anyLong())).then(
        i -> i.getArgument(0) != null ? repository.findById(i.getArgument(0)) : Optional.empty());
    UI.getCurrent().setLocale(locale);
    UsersView view = navigate(UsersView.class);
    User user = repository.findById(2L).orElseThrow();
    doubleClickItem(view.users, user);
    dialog = $(UserDialog.class).first();
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(SAVE), dialog.save.getId().orElse(""));
    assertTrue(dialog.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    assertEquals(id(CANCEL), dialog.cancel.getId().orElse(""));
  }

  @Test
  public void labels() {
    User user = repository.findById(dialog.getUserId()).orElseThrow();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, user.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + SAVE), dialog.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + CANCEL), dialog.cancel.getText());
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    User user = repository.findById(dialog.getUserId()).orElseThrow();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, user.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + SAVE), dialog.save.getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + CANCEL), dialog.cancel.getText());
  }

  @Test
  public void savedListener() {
    dialog.addSavedListener(savedListener);
    dialog.fireSavedEvent();
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void savedListener_Remove() {
    dialog.addSavedListener(savedListener).remove();
    dialog.fireSavedEvent();
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void getUser() {
    User user = mock(User.class);
    dialog.form = mock(UserForm.class);
    when(dialog.form.getUser()).thenReturn(user);
    assertEquals(user.getId(), dialog.getUserId());
    verify(dialog.form).getUser();
  }

  @Test
  public void setUser_User() {
    User user = repository.findById(2L).orElseThrow();
    dialog.form = mock(UserForm.class);
    when(dialog.form.getUser()).thenReturn(user);

    dialog.setUserId(2L);

    verify(service, atLeastOnce()).get(2L);
    verify(dialog.form).setUser(user);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, user.getName()),
        dialog.getHeaderTitle());
  }

  @Test
  public void setUser_0() {
    dialog.form = mock(UserForm.class);
    when(dialog.form.getUser()).thenReturn(new User());

    dialog.setUserId(0);

    verify(dialog.form).setUser(userCaptor.capture());
    assertEquals(0, userCaptor.getValue().getId());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 0), dialog.getHeaderTitle());
  }

  @Test
  public void save_ValidationFalse() {
    dialog.form = mock(UserForm.class);
    dialog.addSavedListener(savedListener);

    dialog.save();

    verify(service, never()).save(any(), any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_ValidationTrue() {
    User user = mock(User.class);
    dialog.form = mock(UserForm.class);
    when(dialog.form.isValid()).thenReturn(true);
    when(dialog.form.getUser()).thenReturn(user);
    String password = "test_password";
    when(dialog.form.getPassword()).thenReturn(password);
    String email = "test@ircm.qc.ca";
    when(user.getEmail()).thenReturn(email);
    dialog.addSavedListener(savedListener);

    dialog.save();

    verify(service).save(user, password);
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, email),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_ValidationTrueNullPassword() {
    User user = mock(User.class);
    dialog.form = mock(UserForm.class);
    when(dialog.form.isValid()).thenReturn(true);
    when(dialog.form.getUser()).thenReturn(user);
    String email = "test@ircm.qc.ca";
    when(user.getEmail()).thenReturn(email);
    dialog.addSavedListener(savedListener);

    dialog.save();

    verify(service).save(user, null);
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, email),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void cancel_Close() {
    dialog.addSavedListener(savedListener);

    dialog.cancel();

    verify(service, never()).save(any(), any());
    assertFalse($(Notification.class).exists());
    assertFalse(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }
}
