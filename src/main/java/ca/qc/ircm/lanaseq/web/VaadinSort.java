package ca.qc.ircm.lanaseq.web;

import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

/**
 * Vaadin sort utilities.
 */
public class VaadinSort {

  /**
   * Converts Vaadin sort order into Spring Data sort order.
   *
   * @param vaadinSortOrders Vaadin sort order
   * @return Spring Data sort order
   */
  public static Sort springDataSort(List<QuerySortOrder> vaadinSortOrders) {
    return Sort.by(vaadinSortOrders.stream()
        .map(sortOrder -> sortOrder.getDirection() == SortDirection.ASCENDING
            ? Sort.Order.asc(sortOrder.getSorted())
            : Sort.Order.desc(sortOrder.getSorted()))
        .collect(Collectors.toList()));
  }
}
