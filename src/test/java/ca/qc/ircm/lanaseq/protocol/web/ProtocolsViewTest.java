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

package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.DATE;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.OWNER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.HEADER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.ID;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.getFormattedValue;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.google.common.collect.Range;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link ProtocolsView}.
 */
@ServiceTestAnnotations
@WithMockUser
public class ProtocolsViewTest extends AbstractKaribuTestCase {
  private ProtocolsView view;
  @Mock
  private ProtocolsViewPresenter presenter;
  @Captor
  private ArgumentCaptor<ValueProvider<Protocol, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<LocalDateTimeRenderer<Protocol>> localDateTimeRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Protocol>> comparatorCaptor;
  @Autowired
  private ProtocolRepository protocolRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ProtocolsView.class, locale);
  private AppResources protocolResources = new AppResources(Protocol.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Protocol> protocols;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    view = new ProtocolsView(presenter, new ProtocolDialog(), new ProtocolHistoryDialog());
    view.init();
    protocols = protocolRepository.findAll();
  }

  @After
  public void afterTest() {
    MockVaadin.tearDown();
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    Element protocolsElement = view.protocols.getElement();
    view.protocols = mock(Grid.class);
    when(view.protocols.getElement()).thenReturn(protocolsElement);
    view.name = mock(Column.class);
    when(view.protocols.addColumn(any(ValueProvider.class), eq(NAME))).thenReturn(view.name);
    when(view.name.setKey(any())).thenReturn(view.name);
    when(view.name.setComparator(any(Comparator.class))).thenReturn(view.name);
    when(view.name.setHeader(any(String.class))).thenReturn(view.name);
    view.date = mock(Column.class);
    when(view.protocols.addColumn(any(LocalDateTimeRenderer.class), eq(DATE)))
        .thenReturn(view.date);
    when(view.date.setKey(any())).thenReturn(view.date);
    when(view.date.setHeader(any(String.class))).thenReturn(view.date);
    view.owner = mock(Column.class);
    when(view.protocols.addColumn(any(ValueProvider.class), eq(OWNER))).thenReturn(view.owner);
    when(view.owner.setKey(any())).thenReturn(view.owner);
    when(view.owner.setComparator(any(Comparator.class))).thenReturn(view.owner);
    when(view.owner.setHeader(any(String.class))).thenReturn(view.owner);
    HeaderRow filtersRow = mock(HeaderRow.class);
    when(view.protocols.appendHeaderRow()).thenReturn(filtersRow);
    HeaderCell nameFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.name)).thenReturn(nameFilterCell);
    HeaderCell dateFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.date)).thenReturn(dateFilterCell);
    HeaderCell ownerFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.owner)).thenReturn(ownerFilterCell);
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(view);
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(HEADER, view.header.getId().orElse(""));
    assertEquals(PROTOCOLS, view.protocols.getId().orElse(""));
    assertEquals(ADD, view.add.getId().orElse(""));
  }

  @Test
  public void labels() {
    mockColumns();
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.name).setHeader(protocolResources.message(NAME));
    verify(view.name).setFooter(protocolResources.message(NAME));
    verify(view.date).setHeader(protocolResources.message(DATE));
    verify(view.date).setFooter(protocolResources.message(DATE));
    verify(view.owner).setHeader(protocolResources.message(OWNER));
    verify(view.owner).setFooter(protocolResources.message(OWNER));
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(webResources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
  }

  @Test
  public void localeChange() {
    mockColumns();
    view.init();
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(ProtocolsView.class, locale);
    final AppResources protocolResources = new AppResources(Protocol.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.name, atLeastOnce()).setHeader(protocolResources.message(NAME));
    verify(view.name, atLeastOnce()).setFooter(protocolResources.message(NAME));
    verify(view.date, atLeastOnce()).setHeader(protocolResources.message(DATE));
    verify(view.date, atLeastOnce()).setFooter(protocolResources.message(DATE));
    verify(view.owner, atLeastOnce()).setHeader(protocolResources.message(OWNER));
    verify(view.owner, atLeastOnce()).setFooter(protocolResources.message(OWNER));
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(webResources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void protocols() {
    assertEquals(3, view.protocols.getColumns().size());
    assertNotNull(view.protocols.getColumnByKey(NAME));
    assertNotNull(view.protocols.getColumnByKey(DATE));
    assertNotNull(view.protocols.getColumnByKey(OWNER));
    assertTrue(view.protocols.getSelectionModel() instanceof SelectionModel.Single);
  }

  @Test
  public void protocols_ColumnsValueProvider() {
    mockColumns();
    view.init();
    verify(view.protocols).addColumn(valueProviderCaptor.capture(), eq(NAME));
    ValueProvider<Protocol, String> valueProvider = valueProviderCaptor.getValue();
    for (Protocol protocol : protocols) {
      assertEquals(protocol.getName(), valueProvider.apply(protocol));
    }
    verify(view.name).setComparator(comparatorCaptor.capture());
    Comparator<Protocol> comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Protocol protocol : protocols) {
      assertEquals(protocol.getName(),
          ((NormalizedComparator<Protocol>) comparator).getConverter().apply(protocol));
    }
    verify(view.protocols).addColumn(localDateTimeRendererCaptor.capture(), eq(DATE));
    LocalDateTimeRenderer<Protocol> localDateTimeRenderer = localDateTimeRendererCaptor.getValue();
    for (Protocol protocol : protocols) {
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(protocol.getDate()),
          getFormattedValue(localDateTimeRenderer, protocol));
    }
    verify(view.protocols).addColumn(valueProviderCaptor.capture(), eq(OWNER));
    valueProvider = valueProviderCaptor.getValue();
    for (Protocol protocol : protocols) {
      assertEquals(protocol.getOwner().getEmail(), valueProvider.apply(protocol));
    }
    verify(view.owner).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Protocol protocol : protocols) {
      assertEquals(protocol.getOwner().getEmail(),
          ((NormalizedComparator<Protocol>) comparator).getConverter().apply(protocol));
    }
  }

  @Test
  public void view() {
    Protocol protocol = protocols.get(0);
    doubleClickItem(view.protocols, protocol);

    verify(presenter).view(protocol);
  }

  @Test
  public void history() {
    Protocol protocol = protocols.get(0);
    clickItem(view.protocols, protocol, view.name, false, false, true, false);

    verify(presenter).history(protocol);
  }

  @Test
  public void filterName() {
    view.nameFilter.setValue("test");

    verify(presenter).filterName("test");
  }

  @Test
  public void filterRange() {
    Range<LocalDate> range =
        Range.closed(LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
    view.dateFilter.setValue(range);

    verify(presenter).filterDate(range);
  }

  @Test
  public void filterOwner() {
    view.ownerFilter.setValue("test");

    verify(presenter).filterOwner("test");
  }

  @Test
  public void add() {
    clickButton(view.add);
    verify(presenter).add();
  }
}
