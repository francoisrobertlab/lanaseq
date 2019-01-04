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

package ca.qc.ircm.lana.web.component;

import com.vaadin.flow.component.UI;
import java.util.Optional;

/**
 * A component connected to a UI.
 */
public interface UiComponent {
  /**
   * Gets the UI this component is attached to.
   * <p>
   * Copied from Vaadin
   * </p>
   *
   * @return an optional UI component, or an empty optional if this component is not attached to a
   *         UI
   */
  @SuppressWarnings("checkstyle:all")
  public Optional<UI> getUI();

  /**
   * Returns current UI.
   * 
   * @return current UI
   */
  public default UI getCurrentUi() {
    return UI.getCurrent();
  }
}
