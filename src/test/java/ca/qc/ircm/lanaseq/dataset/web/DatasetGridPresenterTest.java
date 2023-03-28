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

package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.google.common.collect.Range;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link DatasetGridPresenter}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetGridPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private DatasetGridPresenter presenter;
  @Mock
  private DatasetGrid grid;
  @MockBean
  private DatasetService service;
  @MockBean
  private ProtocolService protocolService;
  @MockBean
  private AuthenticatedUser authenticatedUser;
  @Mock
  private ListDataProvider<Dataset> dataProvider;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<DatasetDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<SavedEvent<ProtocolDialog>>> protocolSavedListenerCaptor;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private UserRepository userRepository;
  private List<Dataset> datasets;
  private User currentUser;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    grid = new DatasetGrid();
    grid.ownerFilter = new TextField();
    datasets = repository.findAll();
    when(service.all(any())).thenReturn(new ArrayList<>(datasets));
    when(service.count(any())).thenReturn((long) datasets.size());
    currentUser = userRepository.findById(3L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(currentUser));
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.init(grid);
  }

  @Test
  public void datasets() {
    List<Dataset> datasets = items(grid);
    assertEquals(this.datasets.size(), datasets.size());
    for (Dataset dataset : this.datasets) {
      assertTrue(datasets.contains(dataset), () -> dataset.toString());
    }
  }

  @Test
  public void ownerFilter_User() {
    assertEquals(currentUser.getEmail(), grid.ownerFilter.getValue());
    verify(authenticatedUser).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void ownerFilter_ManagerOrAdmin() {
    grid.ownerFilter.setValue("");
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    presenter.init(grid);
    assertEquals("", grid.ownerFilter.getValue());
    verify(authenticatedUser, times(2)).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void filterName() {
    grid.setItems(dataProvider);

    presenter.filterName("test");

    assertEquals("test", presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    grid.setItems(dataProvider);

    presenter.filterName("");

    assertEquals(null, presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterTags() {
    grid.setItems(dataProvider);

    presenter.filterTags("test");

    assertEquals("test", presenter.filter().tagsContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterTags_Empty() {
    grid.setItems(dataProvider);

    presenter.filterTags("");

    assertEquals(null, presenter.filter().tagsContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProtocol() {
    grid.setItems(dataProvider);

    presenter.filterProtocol("test");

    assertEquals("test", presenter.filter().protocolContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProtocol_Empty() {
    grid.setItems(dataProvider);

    presenter.filterProtocol("");

    assertEquals(null, presenter.filter().protocolContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate() {
    grid.setItems(dataProvider);
    Range<LocalDate> range =
        Range.closed(LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));

    presenter.filterDate(range);

    assertEquals(range, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate_Null() {
    grid.setItems(dataProvider);

    presenter.filterDate(null);

    assertEquals(null, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner() {
    grid.setItems(dataProvider);

    presenter.filterOwner("test");

    assertEquals("test", presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    grid.setItems(dataProvider);

    presenter.filterOwner("");

    assertEquals(null, presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void refreshDatasets() {
    grid.setItems(dataProvider);
    presenter.refreshDatasets();
    verify(dataProvider).refreshAll();
  }
}
