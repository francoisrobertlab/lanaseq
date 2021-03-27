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

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class VaadinSortTest {
  @Test
  public void sort() {
    List<QuerySortOrder> vaadinSorts = new ArrayList<>();
    vaadinSorts.add(new QuerySortOrder("name", SortDirection.ASCENDING));
    vaadinSorts.add(new QuerySortOrder("owner.email", SortDirection.DESCENDING));
    vaadinSorts.add(new QuerySortOrder("protocol.name", SortDirection.ASCENDING));
    Sort sort = VaadinSort.springDataSort(vaadinSorts);
    assertEquals(vaadinSorts.size(), sort.toList().size());
    assertEquals("name", sort.toList().get(0).getProperty());
    assertEquals(Direction.ASC, sort.toList().get(0).getDirection());
    assertEquals("owner.email", sort.toList().get(1).getProperty());
    assertEquals(Direction.DESC, sort.toList().get(1).getDirection());
    assertEquals("protocol.name", sort.toList().get(2).getProperty());
    assertEquals(Direction.ASC, sort.toList().get(2).getDirection());
  }
}
