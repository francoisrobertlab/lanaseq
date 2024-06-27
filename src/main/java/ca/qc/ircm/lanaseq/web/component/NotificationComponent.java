package ca.qc.ircm.lanaseq.web.component;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;

/**
 * Shows notification.
 */
public interface NotificationComponent {
  public static final int DEFAULT_DURATION = 5000;

  public default void showNotification(String text) {
    new Notification(text, DEFAULT_DURATION).open();
  }

  public default void showNotification(String text, int duration) {
    new Notification(text, duration).open();
  }

  public default void showNotification(String text, int duration, Position position) {
    new Notification(text, duration, position).open();
  }
}
