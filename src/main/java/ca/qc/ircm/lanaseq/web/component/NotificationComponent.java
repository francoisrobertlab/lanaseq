package ca.qc.ircm.lanaseq.web.component;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;

/**
 * Shows notification.
 */
public interface NotificationComponent {
  int DEFAULT_DURATION = 5000;

  default void showNotification(String text) {
    new Notification(text, DEFAULT_DURATION).open();
  }

  default void showNotification(String text, int duration) {
    new Notification(text, duration).open();
  }

  default void showNotification(String text, int duration, Position position) {
    new Notification(text, duration, position).open();
  }
}
