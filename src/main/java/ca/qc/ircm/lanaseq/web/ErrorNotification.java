package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Notification with {@code LUMO_ERROR} variant and a close button on the right.
 */
public class ErrorNotification extends Notification {

  public static final String STYLE = "error-notification";
  public static final String ERROR = "error";
  public static final String CLOSE = "close";
  private static final String MESSAGE_PREFIX = messagePrefix(ErrorNotification.class);
  protected Div error = new Div();
  protected Button close = new Button();

  public ErrorNotification(String text) {
    addClassName(STYLE);
    addThemeVariants(NotificationVariant.LUMO_ERROR);
    setDuration(0);
    setPosition(Position.MIDDLE);
    HorizontalLayout layout = new HorizontalLayout(error, close);
    add(layout);
    error.addClassName(styleName(STYLE, ERROR));
    error.setText(text);
    close.addClassName(styleName(STYLE, CLOSE));
    close.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
    close.setAriaLabel(getTranslation(MESSAGE_PREFIX + CLOSE));
    close.setIcon(VaadinIcon.CLOSE.create());
    close.addClickListener(e -> close());
  }

  public String getText() {
    return error.getText();
  }

  public void setText(String text) {
    error.setText(text);
  }
}
