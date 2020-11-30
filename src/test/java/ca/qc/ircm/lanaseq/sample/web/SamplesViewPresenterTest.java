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

import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGED;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGE_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES_MORE_THAN_ONE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES_REQUIRED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
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
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.google.common.collect.Range;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
@WithMockUser
public class SamplesViewPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private SamplesViewPresenter presenter;
  @Mock
  private SamplesView view;
  @MockBean
  private SampleService service;
  @MockBean
  private ProtocolService protocolService;
  @MockBean
  private DatasetService datasetService;
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
  private ArgumentCaptor<ComponentEventListener<DeletedEvent<SampleDialog>>> deletedListenerCaptor;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<SavedEvent<ProtocolDialog>>> protocolSavedListenerCaptor;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SamplesView.class, locale);
  private AppResources datasetResources = new AppResources(Dataset.class, locale);
  private List<Sample> samples;
  private User currentUser;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    view.header = new H2();
    view.samples = new Grid<>();
    view.samples.setSelectionMode(SelectionMode.MULTI);
    view.nameFilter = new TextField();
    view.protocolFilter = new TextField();
    view.dateFilter = mock(DateRangeField.class);
    view.ownerFilter = new TextField();
    view.error = new Div();
    view.add = new Button();
    view.merge = new Button();
    view.dialog = mock(SampleDialog.class);
    view.filesDialog = mock(SampleFilesDialog.class);
    view.protocolDialog = mock(ProtocolDialog.class);
    samples = repository.findAll();
    when(service.all()).thenReturn(new ArrayList<>(samples));
    currentUser = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(currentUser);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(view);
    presenter.localeChange(locale);
  }

  @Test
  public void samples() {
    List<Sample> samples = items(view.samples);
    assertEquals(this.samples.size(), samples.size());
    for (Sample sample : this.samples) {
      assertTrue(sample.toString(), samples.contains(sample));
    }
    LocalDate date = samples.get(0).getDate();
    for (Sample sample : samples) {
      assertTrue(sample + " with date " + sample.getDate() + " <= " + date,
          date.compareTo(sample.getDate()) >= 0);
      date = sample.getDate();
    }
    assertEquals(0, view.samples.getSelectedItems().size());
    samples.forEach(dataset -> view.samples.select(dataset));
    assertEquals(samples.size(), view.samples.getSelectedItems().size());
  }

  @Test
  public void ownerFilter_User() {
    assertEquals(currentUser.getEmail(), view.ownerFilter.getValue());
    verify(authorizationService).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void ownerFilter_ManagerOrAdmin() {
    view.ownerFilter.setValue("");
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    presenter.init(view);
    assertEquals("", view.ownerFilter.getValue());
    verify(authorizationService, times(2)).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void filterName() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterName("test");

    assertEquals("test", presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterName("");

    assertEquals(null, presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProtocol() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterProtocol("test");

    assertEquals("test", presenter.filter().protocolContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProtocol_Empty() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterProtocol("");

    assertEquals(null, presenter.filter().protocolContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate() {
    view.samples.setDataProvider(dataProvider);
    Range<LocalDate> range = Range.closed(LocalDate.now().minusDays(10), LocalDate.now());

    presenter.filterDate(range);

    assertEquals(range, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterDate_Null() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterDate(null);

    assertEquals(null, presenter.filter().dateRange);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterOwner("test");

    assertEquals("test", presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    view.samples.setDataProvider(dataProvider);

    presenter.filterOwner("");

    assertEquals(null, presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void view() {
    Sample sample = new Sample();
    sample.setId(2L);
    Sample databaseSample = mock(Sample.class);
    when(service.get(any())).thenReturn(databaseSample);
    presenter.view(sample);
    verify(service).get(2L);
    verify(view.dialog).setSample(databaseSample);
    verify(view.dialog).open();
  }

  @Test
  public void viewFiles() {
    Sample sample = samples.get(0);
    view.samples.select(sample);
    presenter.viewFiles();
    assertFalse(view.error.isVisible());
    verify(view.filesDialog).setSample(sample);
    verify(view.filesDialog).open();
  }

  @Test
  public void viewFiles_NoSelection() {
    presenter.viewFiles();
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(SAMPLES_REQUIRED), view.error.getText());
    verify(view.filesDialog, never()).setSample(any());
    verify(view.filesDialog, never()).open();
  }

  @Test
  public void viewFiles_MoreThanOneSampleSelected() {
    view.samples.select(samples.get(0));
    view.samples.select(samples.get(1));
    presenter.viewFiles();
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(SAMPLES_MORE_THAN_ONE), view.error.getText());
    verify(view.filesDialog, never()).setSample(any());
    verify(view.filesDialog, never()).open();
  }

  @Test
  public void viewFiles_Sample() {
    Sample sample = samples.get(0);
    presenter.viewFiles(sample);
    verify(view.filesDialog).setSample(sample);
    verify(view.filesDialog).open();
  }

  @Test
  public void viewProtocol() {
    Protocol protocol = new Protocol();
    protocol.setId(1L);
    Protocol databaseProtocol = mock(Protocol.class);
    when(protocolService.get(any())).thenReturn(databaseProtocol);
    presenter.viewProtocol(protocol);
    verify(protocolService).get(1L);
    verify(view.protocolDialog).setProtocol(databaseProtocol);
    verify(view.protocolDialog).open();
  }

  @Test
  public void add() {
    presenter.add();
    verify(view.dialog).setSample(null);
    verify(view.dialog).open();
  }

  @Test
  public void merge() {
    when(service.isMergable(any())).thenReturn(true);
    view.samples.select(samples.get(0));
    view.samples.select(samples.get(1));
    presenter.merge();
    assertFalse(view.error.isVisible());
    verify(service).isMergable(samplesCaptor.capture());
    assertEquals(2, samplesCaptor.getValue().size());
    assertTrue(samplesCaptor.getValue().contains(samples.get(0)));
    assertTrue(samplesCaptor.getValue().contains(samples.get(1)));
    verify(datasetService).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getId());
    assertTrue(dataset.getTags().isEmpty());
    assertEquals(2, dataset.getSamples().size());
    assertEquals(samples.get(0), dataset.getSamples().get(0));
    assertEquals(samples.get(1), dataset.getSamples().get(1));
    assertEquals(samples.get(0).getDate(), dataset.getDate());
    verify(view).showNotification(resources.message(MERGED, dataset.getName()));
  }

  @Test
  public void merge_SortById() {
    when(service.isMergable(any())).thenReturn(true);
    view.samples.select(samples.get(1));
    view.samples.select(samples.get(0));
    presenter.merge();
    assertFalse(view.error.isVisible());
    verify(service).isMergable(samplesCaptor.capture());
    assertEquals(2, samplesCaptor.getValue().size());
    assertTrue(samplesCaptor.getValue().contains(samples.get(0)));
    assertTrue(samplesCaptor.getValue().contains(samples.get(1)));
    verify(datasetService).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getId());
    assertTrue(dataset.getTags().isEmpty());
    assertEquals(2, dataset.getSamples().size());
    assertEquals(samples.get(0), dataset.getSamples().get(0));
    assertEquals(samples.get(1), dataset.getSamples().get(1));
    assertEquals(samples.get(0).getDate(), dataset.getDate());
    verify(view).showNotification(resources.message(MERGED, dataset.getName()));
  }

  @Test
  public void merge_NoSamples() {
    presenter.merge();
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(SAMPLES_REQUIRED), view.error.getText());
    verify(service, never()).isMergable(any());
    verify(datasetService, never()).save(any());
    verify(view, never()).showNotification(any());
  }

  @Test
  public void merge_NotMergeable() {
    when(service.isMergable(any())).thenReturn(false);
    view.samples.select(samples.get(0));
    view.samples.select(samples.get(1));
    presenter.merge();
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(MERGE_ERROR), view.error.getText());
    verify(service).isMergable(samplesCaptor.capture());
    assertEquals(2, samplesCaptor.getValue().size());
    assertTrue(samplesCaptor.getValue().contains(samples.get(0)));
    assertTrue(samplesCaptor.getValue().contains(samples.get(1)));
    verify(datasetService, never()).save(any());
    verify(view, never()).showNotification(any());
  }

  @Test
  public void merge_NameExists() {
    when(service.isMergable(any())).thenReturn(true);
    when(datasetService.exists(any())).thenReturn(true);
    view.samples.select(samples.get(0));
    view.samples.select(samples.get(1));
    presenter.merge();
    assertTrue(view.error.isVisible());
    assertEquals(datasetResources.message(NAME_ALREADY_EXISTS,
        "MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2_20181020"), view.error.getText());
    verify(datasetService).exists("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2_20181020");
    verify(datasetService, never()).save(any());
    verify(view, never()).showNotification(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshSamplesOnSaved() {
    verify(view.dialog).addSavedListener(savedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<SampleDialog>> savedListener = savedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(service, times(2)).all();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshSamplesOnDeleted() {
    verify(view.dialog).addDeletedListener(deletedListenerCaptor.capture());
    ComponentEventListener<DeletedEvent<SampleDialog>> deletedListener =
        deletedListenerCaptor.getValue();
    deletedListener.onComponentEvent(mock(DeletedEvent.class));
    verify(service, times(2)).all();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshSamplesOnProtocolSaved() {
    verify(view.protocolDialog).addSavedListener(protocolSavedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<ProtocolDialog>> savedListener =
        protocolSavedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(service, times(2)).all();
  }
}
