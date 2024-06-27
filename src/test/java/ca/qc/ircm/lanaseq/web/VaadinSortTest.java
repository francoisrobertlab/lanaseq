package ca.qc.ircm.lanaseq.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Tests for {@link VaadinSort}.
 */
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
