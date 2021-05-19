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
   * @param vaadinSortOrders
   *          Vaadin sort order
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
