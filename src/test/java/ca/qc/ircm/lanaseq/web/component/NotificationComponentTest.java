/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq.web.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.github.mvysny.kaributesting.v10.NotificationsKt;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link NotificationComponent}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class NotificationComponentTest extends AbstractKaribuTestCase {
  private NotificationComponentForTest notificationComponent = new NotificationComponentForTest();

  @Test
  public void showNotification_Text() {
    notificationComponent.showNotification("abc");

    Notification notification = NotificationsKt.getNotifications().get(0);
    validateNotificationText("abc", notification);
    validateNotificationDuration(NotificationComponent.DEFAULT_DURATION, notification);
    validateNotificationPosition(Position.BOTTOM_START, notification);
  }

  @Test
  public void showNotification_TextDuration() {
    notificationComponent.showNotification("abc", 100);

    Notification notification = NotificationsKt.getNotifications().get(0);
    validateNotificationText("abc", notification);
    validateNotificationDuration(100, notification);
    validateNotificationPosition(Position.BOTTOM_START, notification);
  }

  @Test
  public void showNotification_TextDurationPosition() {
    notificationComponent.showNotification("abc", 100, Position.TOP_END);

    Notification notification = NotificationsKt.getNotifications().get(0);
    validateNotificationText("abc", notification);
    validateNotificationDuration(100, notification);
    validateNotificationPosition(Position.TOP_END, notification);
  }

  private void validateNotificationText(String expectedText, Notification notification) {
    assertEquals(expectedText, notification.getElement().getChild(0).getProperty("innerHTML"));
  }

  private void validateNotificationDuration(int duration, Notification notification) {
    assertEquals(duration, Integer.parseInt(notification.getElement().getProperty("duration")));
  }

  private void validateNotificationPosition(Position position, Notification notification) {
    assertEquals(position.getClientName(), notification.getElement().getProperty("position"));
  }

  private class NotificationComponentForTest implements NotificationComponent {
  }
}
