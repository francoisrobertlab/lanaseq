package ca.qc.ircm.lanaseq.test.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.ValueProvider;
import elemental.json.JsonArray;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * Utility methods for presenter testign.
 */
public class VaadinTestUtils {

  private static final String ICON_ATTRIBUTE = "icon";
  private static final Logger logger = LoggerFactory.getLogger(VaadinTestUtils.class);

  /**
   * Fires an event on component.
   *
   * @param component component
   * @param event     event
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
   * @param button button
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
   * @param grid     grid
   * @param item     item
   * @param column   grid column
   * @param ctrlKey  <code>true</code> if the control key was down when the event was fired,
   *                 <code>false</code> otherwise
   * @param shiftKey <code>true</code> if the shift key was down when the event was fired,
   *                 <code>false</code> otherwise
   * @param altKey   <code>true</code> if the alt key was down when the event was fired,
   *                 <code>false</code>
   *                 otherwise
   * @param metaKey  <code>true</code> if the meta key was down when the event was fired,
   *                 <code>false</code> otherwise
   */
  public static <E> void clickItem(Grid<E> grid, E item, @Nullable Grid.Column<E> column,
      boolean ctrlKey,
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
   * @param grid grid
   * @param item item
   */
  public static <E> void doubleClickItem(Grid<E> grid, E item) {
    doubleClickItem(grid, item, null);
  }

  /**
   * Simulates an item click on grid.
   *
   * @param grid grid
   * @param item item
   */
  public static <E> void doubleClickItem(Grid<E> grid, E item, @Nullable Grid.Column<E> column) {
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
   * @param renderer renderer
   * @return renderer's template
   */
  public static String rendererTemplate(LitRenderer<?> renderer) {
    try {
      Field field = LitRenderer.class.getDeclaredField("templateExpression");
      field.setAccessible(true);
      return (String) field.get(renderer);
    } catch (SecurityException | NoSuchFieldException | IllegalArgumentException
             | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Returns all registered properties of this renderer.
   *
   * @param <SOURCE> renderer source type
   * @param renderer renderer
   * @return all registered properties of this renderer
   */
  public static <SOURCE> Map<String, ValueProvider<SOURCE, ?>>
  properties(LitRenderer<SOURCE> renderer) {
    try {
      Field field = LitRenderer.class.getDeclaredField("valueProviders");
      field.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, ValueProvider<SOURCE, ?>> properties =
          (Map<String, ValueProvider<SOURCE, ?>>) field.get(renderer);
      return properties;
    } catch (SecurityException | NoSuchFieldException | IllegalArgumentException
             | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Returns all registered functions of this renderer.
   *
   * @param <SOURCE> renderer source type
   * @param renderer renderer
   * @return all registered functions of this renderer
   */
  public static <SOURCE> Map<String, SerializableBiConsumer<SOURCE, JsonArray>>
  functions(LitRenderer<SOURCE> renderer) {
    try {
      Field field = LitRenderer.class.getDeclaredField("clientCallables");
      field.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, SerializableBiConsumer<SOURCE, JsonArray>> functions =
          (Map<String, SerializableBiConsumer<SOURCE, JsonArray>>) field.get(renderer);
      return functions;
    } catch (SecurityException | NoSuchFieldException | IllegalArgumentException
             | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Returns items in grid, unsorted and non-filtered.
   *
   * @param grid grid
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
   * @param comboBox combo box
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
   * @param select select
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
   * @param expected expected icon
   * @param actual   actual icon
   */
  public static void validateIcon(Icon expected, Component actual) {
    assertEquals(expected.getElement().getAttribute(ICON_ATTRIBUTE),
        actual.getElement().getAttribute(ICON_ATTRIBUTE));
  }

  /**
   * Returns the renderer's formatted value for item.
   *
   * @param <T>      item's type
   * @param renderer renderer
   * @param item     item
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
      logger.warn("Cannot get formatted value for renderer {} and item {}", renderer, item, e);
      throw new IllegalArgumentException(
          "Cannot get formatted value for renderer " + renderer + " and item " + item, e);
    }
  }

  /**
   * Validates that two {@link DatePickerI18n} are identical.
   *
   * @param expected expected
   * @param actual   actual
   */
  public static void validateEquals(DatePickerI18n expected, DatePickerI18n actual) {
    assertEquals(expected.getToday(), actual.getToday());
    assertEquals(expected.getCancel(), actual.getCancel());
    assertEquals(expected.getFirstDayOfWeek(), actual.getFirstDayOfWeek());
    assertEquals(expected.getMonthNames(), actual.getMonthNames());
    assertEquals(expected.getWeekdays(), actual.getWeekdays());
    assertEquals(expected.getWeekdaysShort(), actual.getWeekdaysShort());
  }

  /**
   * Validates that two {@link UploadI18N} are identical.
   *
   * @param expected expected
   * @param actual   actual
   */
  public static void validateEquals(UploadI18N expected, UploadI18N actual) {
    assertEquals(expected.getAddFiles().getOne(), actual.getAddFiles().getOne());
    assertEquals(expected.getAddFiles().getMany(), actual.getAddFiles().getMany());
    assertEquals(expected.getDropFiles().getOne(), actual.getDropFiles().getOne());
    assertEquals(expected.getDropFiles().getMany(), actual.getDropFiles().getMany());
    assertEquals(expected.getError().getFileIsTooBig(), actual.getError().getFileIsTooBig());
    assertEquals(expected.getError().getIncorrectFileType(),
        actual.getError().getIncorrectFileType());
    assertEquals(expected.getError().getTooManyFiles(), actual.getError().getTooManyFiles());
    assertEquals(expected.getUploading().getError().getForbidden(),
        actual.getUploading().getError().getForbidden());
    assertEquals(expected.getUploading().getRemainingTime().getPrefix(),
        actual.getUploading().getRemainingTime().getPrefix());
    assertEquals(expected.getUploading().getStatus().getConnecting(),
        actual.getUploading().getStatus().getConnecting());
    assertArrayEquals(expected.getUnits().getSize().toArray(),
        actual.getUnits().getSize().toArray());
  }
}
