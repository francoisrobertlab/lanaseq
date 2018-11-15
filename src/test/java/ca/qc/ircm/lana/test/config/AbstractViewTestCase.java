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

package ca.qc.ircm.lana.test.config;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.server.VaadinSession;
import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Configures a mock UI.
 */
public abstract class AbstractViewTestCase {
  @Mock
  protected UI ui;
  @Mock
  protected VaadinSession session;
  @Mock
  protected Page page;
  @Captor
  private ArgumentCaptor<SerializableConsumer<ExecutionContext>> openDialogCaptor;
  @Captor
  private ArgumentCaptor<Dialog> dialogCaptor;
  @Captor
  private ArgumentCaptor<Notification> notificationCaptor;

  /**
   * Sets ui as current UI instance.
   */
  @Before
  public void setUi() {
    when(ui.getSession()).thenReturn(session);
    when(ui.getPage()).thenReturn(page);
    CurrentInstance.setCurrent(ui);
  }

  @After
  public void removeUi() {
    CurrentInstance.set(UI.class, null);
  }

  private void processBeforeClientResponse() {
    verify(ui).beforeClientResponse(eq(ui), openDialogCaptor.capture());
    openDialogCaptor.getValue().accept(mock(ExecutionContext.class));
  }

  protected Dialog testOpenDialog() {
    processBeforeClientResponse();
    verify(ui).add(dialogCaptor.capture());
    return dialogCaptor.getValue();
  }

  protected Notification testOpenNotification() {
    processBeforeClientResponse();
    verify(ui).add(notificationCaptor.capture());
    return notificationCaptor.getValue();
  }
}
