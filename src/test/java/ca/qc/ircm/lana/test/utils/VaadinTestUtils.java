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

import static org.junit.Assert.assertEquals;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.dom.Element;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility methods for presenter testign.
 */
public class VaadinTestUtils {
  private static final String ICON_ATTRIBUTE = "icon";

  /**
   * Simulates a click on button.
   *
   * @param button
   *          button
   */
  public static void clickButton(Button button) {
    try {
      Method method = Component.class.getDeclaredMethod("getEventBus");
      method.setAccessible(true);
      ComponentEventBus eventBus = (ComponentEventBus) method.invoke(button);
      eventBus.fireEvent(new ClickEvent<>(button));
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Simulates an item click on grid.
   *
   * @param grid
   *          grid
   * @param item
   *          item
   */
  public static <E> void doubleClickItem(Grid<E> grid, E item) {
    try {
      String key = grid.getDataCommunicator().getKeyMapper().key(item);
      Method method = Component.class.getDeclaredMethod("getEventBus");
      method.setAccessible(true);
      ComponentEventBus eventBus = (ComponentEventBus) method.invoke(grid);
      eventBus.fireEvent(new ItemDoubleClickEvent<>(grid, false, key, -1, -1, -1, -1, 2, 0, false,
          false, false, false));
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
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

  /**
   * Returns items in combo box, unsorted and non-filtered.
   *
   * @param comboBox
   *          combo box
   * @return items in combo box, unsorted and non-filtered
   */
  @SuppressWarnings("unchecked")
  public static <V> List<V> items(ComboBox<V> comboBox) {
    if (comboBox.getDataProvider() instanceof ListDataProvider) {
      return new ArrayList<>(((ListDataProvider<V>) comboBox.getDataProvider()).getItems());
    } else {
      return comboBox.getDataProvider().fetch(new Query<>(0, Integer.MAX_VALUE, null, null, null))
          .collect(Collectors.toList());
    }
  }

  public static Optional<BindingValidationStatus<?>>
      findValidationStatusByField(BinderValidationStatus<?> statuses, HasValue<?, ?> field) {
    return findValidationStatusByField(statuses.getFieldValidationErrors(), field);
  }

  public static Optional<BindingValidationStatus<?>>
      findValidationStatusByField(ValidationException e, HasValue<?, ?> field) {
    return findValidationStatusByField(e.getFieldValidationErrors(), field);
  }

  public static Optional<BindingValidationStatus<?>>
      findValidationStatusByField(List<BindingValidationStatus<?>> statuses, HasValue<?, ?> field) {
    return statuses.stream().filter(ve -> ve.getField().equals(field)).findFirst();
  }

  /**
   * Validates that icon is present on button.
   *
   * @param button
   *          button
   * @param icon
   *          icon
   */
  public static void validateIcon(Icon icon, Button button) {
    Element iconOnComponent;
    if (button.isIconAfterText()) {
      iconOnComponent = button.getElement().getChild(button.getElement().getChildCount() - 1);
    } else {
      iconOnComponent = button.getElement().getChild(0);
    }
    assertEquals(icon.getElement().getAttribute(ICON_ATTRIBUTE),
        iconOnComponent.getAttribute(ICON_ATTRIBUTE));
  }
}
