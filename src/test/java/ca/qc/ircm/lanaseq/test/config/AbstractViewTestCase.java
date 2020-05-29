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

package ca.qc.ircm.lanaseq.test.config;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import javax.servlet.ServletContext;
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
  protected WebBrowser browser;
  @Mock
  protected Page page;
  @Mock
  protected History history;
  @Mock
  protected VaadinServletService vaadinService;
  @Mock
  protected VaadinServlet servlet;
  @Mock
  protected ServletContext servletContext;
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
    when(session.getBrowser()).thenReturn(browser);
    when(ui.getPage()).thenReturn(page);
    when(page.getHistory()).thenReturn(history);
    when(vaadinService.getServlet()).thenReturn(servlet);
    when(servlet.getServletContext()).thenReturn(servletContext);
    when(servletContext.getContextPath()).thenReturn("");
    CurrentInstance.setCurrent(ui);
    CurrentInstance.set(VaadinService.class, vaadinService);
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
