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

package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import com.vaadin.flow.router.BeforeEnterEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * Tests for {@link MainView}.
 */
@NonTransactionalTestAnnotations
public class MainViewTest {
  private MainView view;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private BeforeEnterEvent event;

  @BeforeEach
  public void beforeTest() {
    view = new MainView(authenticatedUser);
  }

  @Test
  public void beforeEnter_User() {
    view.beforeEnter(event);

    verify(event).forwardTo(DatasetsView.class);
  }

  @Test
  public void beforeEnter_Admin() {
    when(authenticatedUser.hasRole(ADMIN)).thenReturn(true);

    view.beforeEnter(event);

    verify(event).forwardTo(DatasetsView.class);
  }

  @Test
  public void beforeEnter_Manager() {
    when(authenticatedUser.hasRole(MANAGER)).thenReturn(true);

    view.beforeEnter(event);

    verify(event).forwardTo(DatasetsView.class);
  }

  @Test
  public void beforeEnter_NoRole() {
    view.beforeEnter(event);
  }
}
