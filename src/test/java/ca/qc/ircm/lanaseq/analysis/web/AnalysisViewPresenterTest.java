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

import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.DATASETS_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.html.Div;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link AnalysisViewPresenter}.
 */
@ServiceTestAnnotations
@WithMockUser
public class AnalysisViewPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private AnalysisViewPresenter presenter;
  @Mock
  private AnalysisView view;
  @Autowired
  private DatasetRepository repository;
  @Mock
  private Dataset dataset;
  private Set<Dataset> datasets = new HashSet<>();
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(AnalysisView.class, locale);

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    view.dialog = mock(AnalysisDialog.class);
    view.error = new Div();
    presenter.init(view);
    presenter.localChange(locale);
    datasets.add(repository.findById(2L).get());
    datasets.add(repository.findById(7L).get());
  }

  @Test
  public void analyze_One() {
    presenter.analyze(dataset);
    verify(view.dialog).setDataset(dataset);
    verify(view.dialog).open();
  }

  @Test
  public void analyze_Many() {
    presenter.analyze(datasets);
    verify(view.dialog).setDatasets(datasets.stream().collect(Collectors.toList()));
    verify(view.dialog).open();
    assertFalse(view.error.isVisible());
  }

  @Test
  public void analyze_ManyEmpty() {
    datasets.clear();
    presenter.analyze(datasets);
    verify(view.dialog, never()).setDatasets(any());
    verify(view.dialog, never()).open();
    assertTrue(view.error.isVisible());
    assertEquals(resources.message(DATASETS_REQUIRED), view.error.getText());
  }

  @Test
  public void analyze_ManyClearError() {
    presenter.analyze(Collections.emptySet());
    assertTrue(view.error.isVisible());
    presenter.analyze(datasets);
    assertFalse(view.error.isVisible());
  }
}
