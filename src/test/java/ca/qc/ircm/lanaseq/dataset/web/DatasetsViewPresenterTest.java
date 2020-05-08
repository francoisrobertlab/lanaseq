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

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS_REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.PERMISSIONS_DENIED;
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
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class DatasetsViewPresenterTest extends AbstractViewTestCase {
  private DatasetsViewPresenter presenter;
  @Mock
  private DatasetsView view;
  @Mock
  private DatasetService service;
  @Mock
  private ProtocolService protocolService;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private DataProvider<Dataset, ?> dataProvider;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<DatasetDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<ProtocolDialog>>> protocolSavedListenerCaptor;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetsView.class, locale);
  private List<Dataset> datasets;
  private User currentUser;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new DatasetsViewPresenter(service, protocolService, authorizationService);
    view.header = new H2();
    view.datasets = new Grid<>();
    view.datasets.setSelectionMode(SelectionMode.MULTI);
    view.nameFilter = new TextField();
    view.projectFilter = new TextField();
    view.ownerFilter = new TextField();
    view.error = new Div();
    view.add = new Button();
    view.permissions = new Button();
    view.datasetDialog = mock(DatasetDialog.class);
    view.protocolDialog = mock(ProtocolDialog.class);
    view.datasetPermissionsDialog = mock(DatasetPermissionsDialog.class);
    datasets = repository.findAll();
    when(service.all()).thenReturn(datasets);
    currentUser = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(currentUser);
  }

  @Test
  public void datasets() {
    presenter.init(view);
    presenter.localeChange(locale);
    List<Dataset> datasets = items(view.datasets);
    assertEquals(this.datasets.size(), datasets.size());
    for (Dataset dataset : this.datasets) {
      assertTrue(dataset.toString(), datasets.contains(dataset));
    }
    assertEquals(0, view.datasets.getSelectedItems().size());
    datasets.forEach(dataset -> view.datasets.select(dataset));
    assertEquals(datasets.size(), view.datasets.getSelectedItems().size());
  }

  @Test
  public void ownerFilter_User() {
    presenter.init(view);
    presenter.localeChange(locale);

    assertEquals(currentUser.getEmail(), view.ownerFilter.getValue());
    verify(authorizationService).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void ownerFilter_ManagerOrAdmin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);

    presenter.init(view);
    presenter.localeChange(locale);

    assertEquals("", view.ownerFilter.getValue());
    verify(authorizationService).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void filterFilename() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.setDataProvider(dataProvider);

    presenter.filterFilename("test");

    assertEquals("test", presenter.filter().filenameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterFilename_Empty() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.setDataProvider(dataProvider);

    presenter.filterFilename("");

    assertEquals(null, presenter.filter().filenameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.setDataProvider(dataProvider);

    presenter.filterName("test");

    assertEquals("test", presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.setDataProvider(dataProvider);

    presenter.filterName("");

    assertEquals(null, presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProject() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.setDataProvider(dataProvider);

    presenter.filterProject("test");

    assertEquals("test", presenter.filter().projectContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProject_Empty() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.setDataProvider(dataProvider);

    presenter.filterProject("");

    assertEquals(null, presenter.filter().projectContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProtocol() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.setDataProvider(dataProvider);

    presenter.filterProtocol("test");

    assertEquals("test", presenter.filter().protocolContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterProtocol_Empty() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.setDataProvider(dataProvider);

    presenter.filterProtocol("");

    assertEquals(null, presenter.filter().protocolContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.setDataProvider(dataProvider);

    presenter.filterOwner("test");

    assertEquals("test", presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.setDataProvider(dataProvider);

    presenter.filterOwner("");

    assertEquals(null, presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void error() {
    presenter.init(view);
    presenter.localeChange(locale);
    assertFalse(view.error.isVisible());
  }

  @Test
  public void view() {
    presenter.init(view);
    presenter.localeChange(locale);
    Dataset dataset = new Dataset();
    dataset.setId(2L);
    Dataset databaseDataset = new Dataset();
    when(service.get(any())).thenReturn(databaseDataset);
    presenter.view(dataset);
    verify(service).get(2L);
    verify(view.datasetDialog).setDataset(databaseDataset);
    verify(view.datasetDialog).open();
  }

  @Test
  public void view_Protocol() {
    presenter.init(view);
    presenter.localeChange(locale);
    Protocol protocol = new Protocol();
    protocol.setId(1L);
    Protocol databaseProtocol = new Protocol();
    when(protocolService.get(any())).thenReturn(databaseProtocol);
    presenter.view(protocol);
    verify(protocolService).get(1L);
    verify(view.protocolDialog).setProtocol(databaseProtocol);
    verify(view.protocolDialog).open();
  }

  @Test
  public void add() {
    presenter.init(view);
    presenter.localeChange(locale);
    presenter.add();
    verify(view.datasetDialog).setDataset(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getId());
    assertNull(dataset.getName());
    verify(view.datasetDialog).open();
  }

  @Test
  public void permissions() {
    final Dataset dataset = datasets.get(2);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.select(dataset);
    presenter.permissions();
    assertFalse(view.error.isVisible());
    verify(view.datasetPermissionsDialog).setDataset(dataset);
    verify(view.datasetPermissionsDialog).open();
  }

  @Test
  public void permissions_NoDataset() {
    presenter.init(view);
    presenter.localeChange(locale);
    presenter.permissions();
    assertEquals(resources.message(DATASETS_REQUIRED), view.error.getText());
    assertTrue(view.error.isVisible());
    verify(view.datasetPermissionsDialog, never()).setDataset(any());
    verify(view.datasetPermissionsDialog, never()).open();
  }

  @Test
  public void permissions_Denied() {
    Dataset dataset = datasets.get(2);
    presenter.init(view);
    presenter.localeChange(locale);
    view.datasets.select(dataset);
    presenter.permissions();
    assertEquals(resources.message(PERMISSIONS_DENIED), view.error.getText());
    assertTrue(view.error.isVisible());
    verify(view.datasetPermissionsDialog, never()).setDataset(any());
    verify(view.datasetPermissionsDialog, never()).open();
  }

  @Test
  public void permissions_ErrorThenView() {
    presenter.init(view);
    presenter.localeChange(locale);
    presenter.permissions();
    presenter.view(datasets.get(1));
    assertFalse(view.error.isVisible());
  }

  @Test
  public void permissions_ErrorThenAdd() {
    presenter.init(view);
    presenter.localeChange(locale);
    presenter.permissions();
    presenter.add();
    assertFalse(view.error.isVisible());
  }

  @Test
  public void permissions_ErrorThenFilterName() {
    presenter.init(view);
    presenter.localeChange(locale);
    presenter.permissions();
    presenter.filterName("");
    assertFalse(view.error.isVisible());
  }

  @Test
  public void permissions_ErrorThenFilterOwner() {
    presenter.init(view);
    presenter.localeChange(locale);
    presenter.permissions();
    presenter.filterOwner("");
    assertFalse(view.error.isVisible());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshDatasetsOnSaved() {
    presenter.init(view);
    presenter.localeChange(locale);
    verify(view.datasetDialog).addSavedListener(savedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<DatasetDialog>> savedListener =
        savedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(service, times(2)).all();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshDatasetsOnProtocolSaved() {
    presenter.init(view);
    presenter.localeChange(locale);
    verify(view.protocolDialog).addSavedListener(protocolSavedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<ProtocolDialog>> savedListener =
        protocolSavedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(service, times(2)).all();
  }
}
