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

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGE;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.EditEvent;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link DatasetsView}.
 */
@ServiceTestAnnotations
@WithMockUser
public class DatasetsViewTest extends AbstractKaribuTestCase {
  private DatasetsView view;
  @MockBean
  private DatasetsViewPresenter presenter;
  @MockBean
  private DatasetGridPresenter datasetGridPresenter;
  private DatasetGrid datasetGrid;
  @Captor
  private ArgumentCaptor<ValueProvider<Dataset, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<LocalDateRenderer<Dataset>> localDateRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Dataset>> comparatorCaptor;
  @Autowired
  private DatasetRepository datasetRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetsView.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Dataset> datasets;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    datasetGrid = new DatasetGrid(datasetGridPresenter);
    datasetGrid.init();
    view = new DatasetsView(presenter, datasetGrid, new DatasetDialog(), new DatasetFilesDialog(),
        new ProtocolDialog());
    view.init();
    view.datasets.protocol = view.datasets.addColumn(dataset -> dataset.getName());
    datasets = datasetRepository.findAll();
  }

  private Protocol protocol(Dataset dataset) {
    return dataset.getSamples() != null
        ? dataset.getSamples().stream().findFirst().map(s -> s.getProtocol()).orElse(new Protocol())
        : new Protocol();
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(view);
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(HEADER, view.header.getId().orElse(""));
    assertEquals(DATASETS, view.datasets.getId().orElse(""));
    assertEquals(ERROR_TEXT, view.error.getId().orElse(""));
    assertTrue(view.error.getClassNames().contains(ERROR_TEXT));
    assertEquals(ADD, view.add.getId().orElse(""));
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
    assertEquals(MERGE, view.merge.getId().orElse(""));
    validateIcon(VaadinIcon.CONNECT.create(), view.merge.getIcon());
    assertEquals(FILES, view.files.getId().orElse(""));
    validateIcon(VaadinIcon.FILE_O.create(), view.files.getIcon());
  }

  @Test
  public void labels() {
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals(webResources.message(ADD), view.add.getText());
    assertEquals(resources.message(MERGE), view.merge.getText());
    assertEquals(resources.message(FILES), view.files.getText());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(DatasetsView.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals(webResources.message(ADD), view.add.getText());
    assertEquals(resources.message(MERGE), view.merge.getText());
    assertEquals(resources.message(FILES), view.files.getText());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void datasets() {
    assertTrue(view.datasets.getSelectionModel() instanceof SelectionModel.Multi);
  }

  @Test
  public void view() {
    Dataset dataset = datasets.get(0);
    doubleClickItem(view.datasets, dataset);

    verify(presenter).view(dataset);
  }

  @Test
  public void addFiles_Control() {
    Dataset dataset = datasets.get(0);
    clickItem(view.datasets, dataset, view.datasets.name, true, false, false, false);

    verify(presenter).viewFiles(dataset);
  }

  @Test
  public void addFiles_Meta() {
    Dataset dataset = datasets.get(0);
    clickItem(view.datasets, dataset, view.datasets.name, false, false, false, true);

    verify(presenter).viewFiles(dataset);
  }

  @Test
  public void viewProtocol() {
    Dataset dataset = datasets.get(0);
    doubleClickItem(view.datasets, dataset, view.datasets.protocol);

    verify(presenter).viewProtocol(protocol(dataset));
  }

  @Test
  public void viewProtocol_NoProtocol() {
    Dataset dataset = find(datasets, 3L).get();
    doubleClickItem(view.datasets, dataset, view.datasets.protocol);

    verify(presenter).view(dataset);
  }

  @Test
  public void edit() {
    Dataset dataset = datasets.get(0);
    fireEvent(view.datasets, new EditEvent<>(view.datasets, false, dataset));
    verify(presenter).view(dataset);
  }

  @Test
  public void add() {
    clickButton(view.add);
    verify(presenter).add();
  }

  @Test
  public void merge() {
    clickButton(view.merge);
    verify(presenter).merge();
  }

  @Test
  public void files() {
    clickButton(view.files);
    verify(presenter).viewFiles();
  }
}
