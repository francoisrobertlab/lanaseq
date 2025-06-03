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
 * Notification with {@code LUMO_WARNING} variant and a close button on the right.
 */
public class WarningNotification extends Notification {

  public static final String STYLE = "warning-notification";
  public static final String WARNING = "warning";
  public static final String CLOSE = "close";
  private static final String MESSAGE_PREFIX = messagePrefix(WarningNotification.class);
  protected Div warning = new Div();
  protected Button close = new Button();

  public WarningNotification(String text) {
    addClassName(STYLE);
    addThemeVariants(NotificationVariant.LUMO_WARNING);
    setDuration(0);
    setPosition(Position.MIDDLE);
    HorizontalLayout layout = new HorizontalLayout(warning, close);
    add(layout);
    warning.addClassName(styleName(STYLE, WARNING));
    warning.setText(text);
    close.addClassName(styleName(STYLE, CLOSE));
    close.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
    close.setAriaLabel(getTranslation(MESSAGE_PREFIX + CLOSE));
    close.setIcon(VaadinIcon.CLOSE.create());
    close.addClickListener(e -> close());
  }

  public String getText() {
    return warning.getText();
  }

  public void setText(String text) {
    warning.setText(text);
  }
}
