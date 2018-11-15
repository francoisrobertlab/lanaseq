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

package ca.qc.ircm.lana.test.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.mockito.ArgumentCaptor;

/**
 * Utility methods for presenter testign.
 */
public class VaadinTestUtils {
  /**
   * Simulates a click on a previously mocked button.
   *
   * @param button
   *          mocked button
   */
  @SuppressWarnings("unchecked")
  public static void clickMockButton(Button button) {
    ArgumentCaptor<ComponentEventListener<ClickEvent<Button>>> clickListenerCaptor =
        ArgumentCaptor.forClass(ComponentEventListener.class);
    verify(button).addClickListener(clickListenerCaptor.capture());
    ComponentEventListener<ClickEvent<Button>> listener = clickListenerCaptor.getValue();
    listener.onComponentEvent(mock(ClickEvent.class));
  }

  /**
   * Returns items in grid, unsorted and non-filtered.
   *
   * @param grid
   *          grid
   * @return items in grid, unsorted and non-filtered
   */
  @SuppressWarnings("unchecked")
  public static <V> List<V> items(Grid<V> grid) {
    if (grid.getDataProvider() instanceof ListDataProvider) {
      return new ArrayList<>(((ListDataProvider<V>) grid.getDataProvider()).getItems());
    } else {
      return grid.getDataProvider().fetch(new Query<>(0, Integer.MAX_VALUE, null, null, null))
          .collect(Collectors.toList());
    }
  }
}
