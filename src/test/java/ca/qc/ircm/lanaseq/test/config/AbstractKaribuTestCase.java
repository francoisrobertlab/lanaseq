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

import static ca.qc.ircm.lanaseq.test.config.AnnotationFinder.findAnnotation;
import static org.junit.Assert.assertEquals;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import org.junit.jupiter.api.BeforeEach;

/**
 * Configures a mock UI.
 */
public abstract class AbstractKaribuTestCase {
  protected UI ui;

  /**
   * Gets UI instance.
   */
  @BeforeEach
  public void obtainUi() {
    ui = UI.getCurrent();
  }

  protected <C extends Component> void assertCurrentView(Class<C> view) {
    Route route = findAnnotation(view, Route.class).orElseThrow(() -> new IllegalStateException(
        "View " + view.getSimpleName() + " does not have a @Route annotation"));
    assertEquals(route.value(), ui.getInternals().getActiveViewLocation().getPath());
  }

  protected <T, C extends Component & HasUrlParameter<T>> void assertCurrentView(Class<C> view,
      T parameter) {
    Route route = findAnnotation(view, Route.class).orElseThrow(() -> new IllegalStateException(
        "View " + view.getSimpleName() + " does not have a @Route annotation"));
    if (parameter == null) {
      assertEquals(route.value(), ui.getInternals().getActiveViewLocation().getPath());
    }
    assertEquals(route.value() + "/" + parameter,
        ui.getInternals().getActiveViewLocation().getPath());
  }
}
