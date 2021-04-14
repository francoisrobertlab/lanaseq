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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.google.common.collect.Range;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

@ServiceTestAnnotations
@WithMockUser
public class SelectSampleDialogPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private SelectSampleDialogPresenter presenter;
  @Mock
  private SelectSampleDialog dialog;
  @MockBean
  private SampleService service;
  @MockBean
  private AuthorizationService authorizationService;
  @Mock
  private DataProvider<Sample, ?> dataProvider;
  @Captor
  private ArgumentCaptor<Sample> sampleCaptor;
  @Captor
  private ArgumentCaptor<Collection<Sample>> samplesCaptor;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<SampleDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<SavedEvent<ProtocolDialog>>> protocolSavedListenerCaptor;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private UserRepository userRepository;
  private List<Sample> samples;
  private User currentUser;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    dialog.samples = new Grid<>();
    dialog.nameFilter = new TextField();
    dialog.dateFilter = mock(DateRangeField.class);
    dialog.ownerFilter = new TextField();
    samples = repository.findAll();
    when(service.all()).thenReturn(samples);
    currentUser = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(currentUser);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(dialog);
  }

  @Test
  public void samples() {
    List<Sample> samples = items(dialog.samples);
    assertEquals(this.samples.size(), samples.size());
    for (Sample sample : this.samples) {
      assertTrue(sample.toString(), samples.contains(sample));
    }
  }

  @Test
  public void ownerFilter_User() {
    assertEquals(currentUser.getEmail(), dialog.ownerFilter.getValue());
    verify(authorizationService).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void ownerFilter_ManagerOrAdmin() {
    dialog.ownerFilter.setValue("");
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    presenter.init(dialog);
    assertEquals("", dialog.ownerFilter.getValue());
    verify(authorizationService, times(2)).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void filterName() {
    dialog.samples.setDataProvider(dataProvider);

    presenter.filterName("test");

    assertEquals("test", presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    dialog.samples.setDataProvider(dataProvider);

    presenter.filterName("");

    assertEquals(null, presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate() {
    dialog.samples.setDataProvider(dataProvider);
    Range<LocalDate> range = Range.closed(LocalDate.now().minusDays(10), LocalDate.now());

    presenter.filterDate(range);

    assertEquals(range, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate_Null() {
    dialog.samples.setDataProvider(dataProvider);

    presenter.filterDate(null);

    assertEquals(null, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner() {
    dialog.samples.setDataProvider(dataProvider);

    presenter.filterOwner("test");

    assertEquals("test", presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    dialog.samples.setDataProvider(dataProvider);

    presenter.filterOwner("");

    assertEquals(null, presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void select() {
    Sample sample = samples.get(0);
    presenter.select(sample);
    verify(dialog).fireSelectedEvent(sample);
    verify(dialog).close();
  }
}
