package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.CREATION_DATE;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.OWNER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.EDIT_BUTTON;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.HISTORY;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.ID;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS_REQUIRED;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.functions;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.rendererTemplate;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.google.common.collect.Range;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.testbench.unit.MetaKeys;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link ProtocolsView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolsViewTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolsView.class);
  private static final String PROTOCOL_PREFIX = messagePrefix(Protocol.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private ProtocolsView view;
  @MockBean
  private ProtocolService service;
  @Captor
  private ArgumentCaptor<ValueProvider<Protocol, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<LocalDateTimeRenderer<Protocol>> localDateTimeRendererCaptor;
  @Captor
  private ArgumentCaptor<LitRenderer<Protocol>> litRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Protocol>> comparatorCaptor;
  @Autowired
  private ProtocolRepository repository;
  private Locale locale = Locale.ENGLISH;
  private List<Protocol> protocols;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    protocols = repository.findAll();
    when(service.all()).thenReturn(protocols);
    when(service.get(any())).then(
        i -> i.getArgument(0) != null ? repository.findById(i.getArgument(0)) : Optional.empty());
    UI.getCurrent().setLocale(locale);
    view = navigate(ProtocolsView.class);
  }

  private Protocol name(String name) {
    Protocol protocol = new Protocol();
    protocol.setName(name);
    return protocol;
  }

  private Protocol owner(String email) {
    Protocol protocol = new Protocol();
    protocol.setOwner(new User());
    protocol.getOwner().setEmail(email);
    return protocol;
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(PROTOCOLS, view.protocols.getId().orElse(""));
    assertTrue(
        view.dateFilter.getThemeNames().contains(CustomFieldVariant.LUMO_SMALL.getVariantName()));
    assertEquals(ERROR_TEXT, view.error.getId().orElse(""));
    assertTrue(view.error.getClassNames().contains(ERROR_TEXT));
    assertEquals(ADD, view.add.getId().orElse(""));
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
    assertEquals(HISTORY, view.history.getId().orElse(""));
    validateIcon(VaadinIcon.ARCHIVE.create(), view.history.getIcon());
  }

  @Test
  public void labels() {
    HeaderRow headerRow = view.protocols.getHeaderRows().get(0);
    FooterRow footerRow = view.protocols.getFooterRows().get(0);
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + NAME),
        headerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + NAME),
        footerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + CREATION_DATE),
        headerRow.getCell(view.date).getText());
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + CREATION_DATE),
        footerRow.getCell(view.date).getText());
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + OWNER),
        headerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + OWNER),
        footerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + EDIT),
        headerRow.getCell(view.edit).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + EDIT),
        footerRow.getCell(view.edit).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.nameFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.ownerFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ADD), view.add.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HISTORY), view.history.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    HeaderRow headerRow = view.protocols.getHeaderRows().get(0);
    FooterRow footerRow = view.protocols.getFooterRows().get(0);
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + NAME),
        headerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + NAME),
        footerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + CREATION_DATE),
        headerRow.getCell(view.date).getText());
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + CREATION_DATE),
        footerRow.getCell(view.date).getText());
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + OWNER),
        headerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(PROTOCOL_PREFIX + OWNER),
        footerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + EDIT),
        headerRow.getCell(view.edit).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + EDIT),
        footerRow.getCell(view.edit).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.nameFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.ownerFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ADD), view.add.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + HISTORY), view.history.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TITLE,
        view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME)), view.getPageTitle());
  }

  @Test
  public void protocols() {
    assertEquals(4, view.protocols.getColumns().size());
    assertNotNull(view.protocols.getColumnByKey(NAME));
    assertTrue(view.name.isSortable());
    assertNotNull(view.protocols.getColumnByKey(CREATION_DATE));
    assertTrue(view.date.isSortable());
    assertNotNull(view.protocols.getColumnByKey(OWNER));
    assertTrue(view.owner.isSortable());
    assertNotNull(view.protocols.getColumnByKey(EDIT));
    assertFalse(view.edit.isSortable());
    assertTrue(view.protocols.getSelectionModel() instanceof SelectionModel.Single);
    List<Protocol> protocols = items(view.protocols);
    verify(service).all();
    assertEquals(this.protocols.size(), protocols.size());
    for (Protocol protocol : this.protocols) {
      assertTrue(protocols.contains(protocol), () -> protocol.toString());
    }
    view.ownerFilter.setValue("jonh.smith@ircm.qc.ca");
    assertEquals(1, view.protocols.getListDataView().getItemCount());
    protocols = view.protocols.getListDataView().getItems().toList();
    assertTrue(find(protocols, 1L).isPresent());
    assertFalse(find(protocols, 2L).isPresent());
  }

  @Test
  public void protocols_ColumnsValueProvider() {
    for (int i = 0; i < protocols.size(); i++) {
      Protocol protocol = protocols.get(i);
      assertEquals(protocol.getName(),
          test(view.protocols).getCellText(i, view.protocols.getColumns().indexOf(view.name)));
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(protocol.getCreationDate()),
          test(view.protocols).getCellText(i, view.protocols.getColumns().indexOf(view.date)));
      assertEquals(protocol.getOwner().getEmail(),
          test(view.protocols).getCellText(i, view.protocols.getColumns().indexOf(view.owner)));
      LitRenderer<Protocol> editRenderer = (LitRenderer<Protocol>) view.edit.getRenderer();
      assertEquals(EDIT_BUTTON, rendererTemplate(editRenderer));
      assertTrue(functions(editRenderer).containsKey("edit"));
      assertNotNull(functions(editRenderer).get("edit"));
    }
  }

  @Test
  public void protocols_NameColumnComparator() {
    Comparator<Protocol> comparator = view.name.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(name("éê"), name("ee")));
    assertTrue(comparator.compare(name("a"), name("e")) < 0);
    assertTrue(comparator.compare(name("a"), name("é")) < 0);
    assertTrue(comparator.compare(name("e"), name("a")) > 0);
    assertTrue(comparator.compare(name("é"), name("a")) > 0);
  }

  @Test
  public void protocols_OwnerColumnComparator() {
    Comparator<Protocol> comparator = view.owner.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(owner("éê"), owner("ee")));
    assertTrue(comparator.compare(owner("a"), owner("e")) < 0);
    assertTrue(comparator.compare(owner("a"), owner("é")) < 0);
    assertTrue(comparator.compare(owner("e"), owner("a")) > 0);
    assertTrue(comparator.compare(owner("é"), owner("a")) > 0);
  }

  @Test
  public void protocols_EditAndSave() {
    Protocol protocol = repository.findById(1L).get();
    LitRenderer<Protocol> editRenderer = (LitRenderer<Protocol>) view.edit.getRenderer();

    functions(editRenderer).get("edit").accept(protocol, null);

    verify(service).get(protocol.getId());
    ProtocolDialog dialog = $(ProtocolDialog.class).first();
    assertEquals(protocol.getId(), dialog.getProtocolId());
    dialog.fireSavedEvent();
    verify(service, times(2)).all();
  }

  @Test
  public void protocols_EditAndDelete() {
    Protocol protocol = repository.findById(1L).get();
    LitRenderer<Protocol> editRenderer = (LitRenderer<Protocol>) view.edit.getRenderer();

    functions(editRenderer).get("edit").accept(protocol, null);

    verify(service).get(protocol.getId());
    ProtocolDialog dialog = $(ProtocolDialog.class).first();
    assertEquals(protocol.getId(), dialog.getProtocolId());
    dialog.fireDeletedEvent();
    verify(service, times(2)).all();
  }

  @Test
  public void ownerFilter_User() {
    assertEquals("", view.ownerFilter.getValue());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void ownerFilter_Manager() {
    assertEquals("", view.ownerFilter.getValue());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void ownerFilter_Admin() {
    assertEquals("", view.ownerFilter.getValue());
  }

  @Test
  public void doubleClick_Save() {
    Protocol protocol = repository.findById(1L).get();

    test(view.protocols).doubleClickRow(0);

    verify(service).get(protocol.getId());
    ProtocolDialog dialog = $(ProtocolDialog.class).first();
    assertEquals(protocol.getId(), dialog.getProtocolId());
    dialog.fireSavedEvent();
    verify(service, times(2)).all();
  }

  @Test
  public void doubleClick_Delete() {
    Protocol protocol = repository.findById(1L).get();

    test(view.protocols).doubleClickRow(0);

    verify(service).get(protocol.getId());
    ProtocolDialog dialog = $(ProtocolDialog.class).first();
    assertEquals(protocol.getId(), dialog.getProtocolId());
    dialog.fireDeletedEvent();
    verify(service, times(2)).all();
  }

  @Test
  public void altClick_User() {
    test(view.protocols).clickRow(0, new MetaKeys().alt());

    assertFalse($(ProtocolHistoryDialog.class).exists());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void altClick_Manager() {
    Protocol protocol = repository.findById(1L).get();

    test(view.protocols).clickRow(0, new MetaKeys().alt());

    verify(service).get(protocol.getId());
    ProtocolHistoryDialog dialog = $(ProtocolHistoryDialog.class).first();
    assertEquals(protocol.getId(), dialog.getProtocolId());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void altClick_Admin() {
    Protocol protocol = repository.findById(1L).get();

    test(view.protocols).clickRow(0, new MetaKeys().alt());

    verify(service).get(protocol.getId());
    ProtocolHistoryDialog dialog = $(ProtocolHistoryDialog.class).first();
    assertEquals(protocol.getId(), dialog.getProtocolId());
  }

  @Test
  public void filterName() {
    view.protocols.setItems(mock(DataProvider.class));

    view.nameFilter.setValue("test");

    assertEquals("test", view.filter().nameContains);
    verify(view.protocols.getDataProvider()).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    view.protocols.setItems(mock(DataProvider.class));
    view.nameFilter.setValue("test");

    view.nameFilter.setValue("");

    assertNull(view.filter().nameContains);
    verify(view.protocols.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void filterRange() {
    view.protocols.setItems(mock(DataProvider.class));

    Range<LocalDate> range =
        Range.closed(LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
    view.dateFilter.setValue(range);

    assertEquals(range, view.filter().dateRange);
    verify(view.protocols.getDataProvider()).refreshAll();
  }

  @Test
  public void filterRange_Empty() {
    view.protocols.setItems(mock(DataProvider.class));
    Range<LocalDate> range =
        Range.closed(LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
    view.dateFilter.setValue(range);

    view.dateFilter.setValue(null);

    assertNull(view.filter().dateRange);
    verify(view.protocols.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void filterOwner() {
    view.protocols.setItems(mock(DataProvider.class));

    view.ownerFilter.setValue("test");

    assertEquals("test", view.filter().ownerContains);
    verify(view.protocols.getDataProvider()).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    view.protocols.setItems(mock(DataProvider.class));
    view.ownerFilter.setValue("test");

    view.ownerFilter.setValue("");

    assertNull(view.filter().ownerContains);
    verify(view.protocols.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void add() {
    clickButton(view.add);

    ProtocolDialog dialog = $(ProtocolDialog.class).first();
    assertNull(dialog.getProtocolId());
    dialog.fireSavedEvent();
    verify(service, times(2)).all();
  }

  @Test
  public void history_User() {
    Protocol protocol = repository.findById(1L).get();
    view.protocols.select(protocol);
    assertFalse(view.history.isVisible());

    clickButton(view.history);

    assertFalse(view.error.isVisible());
    assertFalse($(ProtocolHistoryDialog.class).exists());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void history_Manager() {
    Protocol protocol = repository.findById(1L).get();
    view.protocols.select(protocol);

    clickButton(view.history);

    verify(service).get(protocol.getId());
    assertFalse(view.error.isVisible());
    ProtocolHistoryDialog dialog = $(ProtocolHistoryDialog.class).first();
    assertEquals(protocol.getId(), dialog.getProtocolId());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void history_Admin() {
    Protocol protocol = repository.findById(1L).get();
    view.protocols.select(protocol);

    clickButton(view.history);

    verify(service).get(protocol.getId());
    assertFalse(view.error.isVisible());
    ProtocolHistoryDialog dialog = $(ProtocolHistoryDialog.class).first();
    assertEquals(protocol.getId(), dialog.getProtocolId());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void history_NoSelection() {
    clickButton(view.history);

    assertTrue(view.error.isVisible());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PROTOCOLS_REQUIRED), view.error.getText());
    assertFalse($(ProtocolHistoryDialog.class).exists());
  }
}
