/*
 * Copyright (c) 2010 Institut de recherches cliniques de Montreal (IRCM)
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

package ca.qc.ircm.lana.web;

import ca.qc.ircm.lana.user.web.SigninView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redirect to {@link JobsView}.
 */
@Route(MainView.VIEW_NAME)
public class MainView extends VerticalLayout {
  public static final String VIEW_NAME = "";
  private static final long serialVersionUID = 710800815636494374L;
  private static final Logger logger = LoggerFactory.getLogger(MainView.class);

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    getUI().ifPresent(ui -> ui.beforeClientResponse(this, context -> {
      logger.debug("Redirect to {}", SigninView.class.getSimpleName());
      getUI().get().navigate(SigninView.class);
    }));
  }
}
