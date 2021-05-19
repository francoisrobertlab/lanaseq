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

package ca.qc.ircm.lanaseq.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.ValueProvider;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility methods for presenter testign.
 */
public class VaadinTestUtils {
  private static final String ICON_ATTRIBUTE = "icon";

  /**
   * Fires an event on component.
   *
   * @param component
   *          component
   * @param event
   *          event
   */
  public static <C extends Component> void fireEvent(C component, ComponentEvent<C> event) {
    try {
      Method method = Component.class.getDeclaredMethod("getEventBus");
      method.setAccessible(true);
      ComponentEventBus eventBus = (ComponentEventBus) method.invoke(component);
      eventBus.fireEvent(event);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

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
   * @param column
   *          grid column
   * @param ctrlKey
   *          <code>true</code> if the control key was down when the event was fired,
   *          <code>false</code> otherwise
   * @param shiftKey
   *          <code>true</code> if the shift key was down when the event was fired,
   *          <code>false</code> otherwise
   * @param altKey
   *          <code>true</code> if the alt key was down when the event was fired, <code>false</code>
   *          otherwise
   * @param metaKey
   *          <code>true</code> if the meta key was down when the event was fired,
   *          <code>false</code> otherwise
   */
  public static <E> void clickItem(Grid<E> grid, E item, Grid.Column<E> column, boolean ctrlKey,
      boolean shiftKey, boolean altKey, boolean metaKey) {
    try {
      String key = grid.getDataCommunicator().getKeyMapper().key(item);
      Method method = Component.class.getDeclaredMethod("getEventBus");
      method.setAccessible(true);
      ComponentEventBus eventBus = (ComponentEventBus) method.invoke(grid);
      eventBus.fireEvent(new ItemClickEvent<>(grid, false, key,
          column != null ? column.getElement().getProperty("_flowId") : null, -1, -1, -1, -1, 2, 0,
          ctrlKey, shiftKey, altKey, metaKey));
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
    doubleClickItem(grid, item, null);
  }

  /**
   * Simulates an item click on grid.
   *
   * @param grid
   *          grid
   * @param item
   *          item
   */
  public static <E> void doubleClickItem(Grid<E> grid, E item, Grid.Column<E> column) {
    try {
      String itemKey = grid.getDataCommunicator().getKeyMapper().key(item);
      Method method = Component.class.getDeclaredMethod("getEventBus");
      method.setAccessible(true);
      ComponentEventBus eventBus = (ComponentEventBus) method.invoke(grid);
      eventBus.fireEvent(new ItemDoubleClickEvent<>(grid, false, itemKey,
          column != null ? column.getElement().getProperty("_flowId") : null, -1, -1, -1, -1, 2, 0,
          false, false, false, false));
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Returns renderer's template.
   *
   * @param renderer
   *          renderer
   * @return renderer's template
   */
  public static String rendererTemplate(Renderer<?> renderer) {
    try {
      Field field = Renderer.class.getDeclaredField("template");
      field.setAccessible(true);
      return (String) field.get(renderer);
    } catch (SecurityException | NoSuchFieldException | IllegalArgumentException
        | IllegalAccessException e) {
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
      return grid.getDataProvider()
          .fetch(new Query<>(0, Integer.MAX_VALUE, Collections.emptyList(), null, null))
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

  /**
   * Returns items in select, unsorted and non-filtered.
   *
   * @param select
   *          select
   * @return items in select, unsorted and non-filtered
   */
  @SuppressWarnings("unchecked")
  public static <V> List<V> items(Select<V> select) {
    if (select.getDataProvider() instanceof ListDataProvider) {
      return new ArrayList<>(((ListDataProvider<V>) select.getDataProvider()).getItems());
    } else {
      return select.getDataProvider().fetch(new Query<>(0, Integer.MAX_VALUE, null, null, null))
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
   * Validates that actual icon is the same as the expected icon.
   *
   * @param expected
   *          expected icon
   * @param actual
   *          actual icon
   */
  public static void validateIcon(Icon expected, Component actual) {
    assertEquals(expected.getElement().getAttribute(ICON_ATTRIBUTE),
        actual.getElement().getAttribute(ICON_ATTRIBUTE));
  }

  /**
   * Returns the renderer's formatted value for item.
   *
   * @param <T>
   *          item's type
   * @param renderer
   *          renderer
   * @param item
   *          item
   * @return the renderer's formatted value for item
   */
  @SuppressWarnings("unchecked")
  public static <T> String getFormattedValue(BasicRenderer<T, ?> renderer, T item) {
    try {
      Method getValueProvider = BasicRenderer.class.getDeclaredMethod("getValueProvider");
      getValueProvider.setAccessible(true);
      ValueProvider<T, ?> vp = (ValueProvider<T, ?>) getValueProvider.invoke(renderer);
      Object value = vp.apply(item);
      Method getFormattedValue =
          BasicRenderer.class.getDeclaredMethod("getFormattedValue", Object.class);
      getFormattedValue.setAccessible(true);
      return (String) getFormattedValue.invoke(renderer, value);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Validates that two {@link DatePickerI18n} are identical.
   *
   * @param expected
   *          expected
   * @param actual
   *          actual
   */
  public static void validateEquals(DatePickerI18n expected, DatePickerI18n actual) {
    assertEquals(expected.getWeek(), actual.getWeek());
    assertEquals(expected.getCalendar(), actual.getCalendar());
    assertEquals(expected.getClear(), actual.getClear());
    assertEquals(expected.getToday(), actual.getToday());
    assertEquals(expected.getCancel(), actual.getCancel());
    assertEquals(expected.getFirstDayOfWeek(), actual.getFirstDayOfWeek());
    assertEquals(expected.getMonthNames(), actual.getMonthNames());
    assertEquals(expected.getWeekdays(), actual.getWeekdays());
    assertEquals(expected.getWeekdaysShort(), actual.getWeekdaysShort());
  }
}
