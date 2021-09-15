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

package ca.qc.ircm.lanaseq.analysis.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.ANALYZE;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.DATASETS;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.HEADER;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.ID;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.ROBTOOLS;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.ROBTOOLS_LINK;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.web.DatasetGrid;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link AnalysisView}.
 */
@ServiceTestAnnotations
@WithMockUser
public class AnalysisViewTest extends AbstractKaribuTestCase {
  private AnalysisView view;
  @Mock
  private AnalysisViewPresenter presenter;
  @Autowired
  private DatasetRepository repository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(AnalysisView.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  @Captor
  private ArgumentCaptor<Set<Dataset>> datasetsCaptor;
  @Mock
  private Dataset dataset;
  private List<Dataset> datasets = new ArrayList<>();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    view = new AnalysisView(presenter, new DatasetGrid(), new AnalysisDialog());
    view.init();
    datasets.add(repository.findById(2L).get());
    datasets.add(repository.findById(7L).get());
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
    assertEquals(ANALYZE, view.analyze.getId().orElse(""));
    assertEquals(ROBTOOLS, view.robtools.getId().orElse(""));
  }

  @Test
  public void labels() {
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals("", view.error.getText());
    assertEquals(resources.message(ANALYZE), view.analyze.getText());
    assertEquals(resources.message(ROBTOOLS), view.robtools.getText());
    verify(presenter).localChange(locale);
  }

  @Test
  public void localeChange() {
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(AnalysisView.class, locale);
    ui.setLocale(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals("", view.error.getText());
    assertEquals(resources.message(ANALYZE), view.analyze.getText());
    assertEquals(resources.message(ROBTOOLS), view.robtools.getText());
    verify(presenter).localChange(locale);
  }

  @Test
  public void datasets() {
    assertTrue(view.datasets.getSelectionModel() instanceof SelectionModel.Multi);
  }

  @Test
  public void analyze_One() {
    doubleClickItem(view.datasets, dataset);
    verify(presenter).analyze(dataset);
  }

  @Test
  public void analyze_Many() {
    view.datasets.setItems(datasets);
    datasets.forEach(ds -> view.datasets.select(ds));
    view.analyze.click();
    verify(presenter).analyze(datasetsCaptor.capture());
    Set<Dataset> datasets = datasetsCaptor.getValue();
    assertEquals(this.datasets.size(), datasets.size());
    this.datasets.forEach(ds -> assertTrue(datasets.contains(ds)));
  }

  @Test
  public void robtools() {
    assertEquals(ROBTOOLS_LINK, view.robtools.getHref());
    assertEquals("_blank", view.robtools.getTarget().orElse(""));
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }
}
