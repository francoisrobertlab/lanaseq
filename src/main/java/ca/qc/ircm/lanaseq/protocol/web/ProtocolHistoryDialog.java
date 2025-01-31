package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.ProtocolFileProperties.FILENAME;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.ByteArrayStreamResourceWriter;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.PostConstruct;
import java.io.Serial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Protocol history dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProtocolHistoryDialog extends Dialog
    implements LocaleChangeObserver, NotificationComponent {

  public static final String ID = "protocols-history-dialog";
  public static final String HEADER = "header";
  public static final String FILES = "files";
  public static final String RECOVER = "recover";
  public static final String RECOVER_BUTTON = "<vaadin-button class='" + RECOVER + "' theme='"
      + ButtonVariant.LUMO_SUCCESS.getVariantName() + "' @click='${recoverFile}'>"
      + "<vaadin-icon icon='vaadin:ambulance' slot='prefix'></vaadin-icon>" + "</vaadin-button>";
  public static final String RECOVERED = "recovered";
  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolHistoryDialog.class);
  private static final String PROTOCOL_FILE_PREFIX = messagePrefix(ProtocolFile.class);
  private static final Logger logger = LoggerFactory.getLogger(ProtocolHistoryDialog.class);
  @Serial
  private static final long serialVersionUID = -7797831034001410430L;
  protected Grid<ProtocolFile> files = new Grid<>();
  protected Column<ProtocolFile> filename;
  protected Column<ProtocolFile> recover;
  private Protocol protocol;
  private final transient ProtocolService service;

  @Autowired
  protected ProtocolHistoryDialog(ProtocolService service) {
    this.service = service;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    setId(ID);
    setWidth("1000px");
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.add(files);
    layout.setSizeFull();
    layout.expand(files);
    files.setId(id(FILES));
    filename = files.addColumn(new ComponentRenderer<>(this::filenameAnchor)).setKey(FILENAME)
        .setSortProperty(FILENAME).setComparator(NormalizedComparator.of(ProtocolFile::getFilename))
        .setFlexGrow(10);
    recover = files.addColumn(
            LitRenderer.<ProtocolFile>of(RECOVER_BUTTON).withFunction("recoverFile", this::recoverFile))
        .setKey(RECOVER);
  }

  private Anchor filenameAnchor(ProtocolFile file) {
    Anchor link = new Anchor();
    link.getElement().setAttribute("download", file.getFilename());
    link.setText(file.getFilename());
    link.setHref(new StreamResource(file.getFilename(),
        new ByteArrayStreamResourceWriter(file.getContent())));
    return link;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    updateHeader();
    filename.setHeader(getTranslation(PROTOCOL_FILE_PREFIX + FILENAME));
    recover.setHeader(getTranslation(MESSAGE_PREFIX + RECOVER));
  }

  private void updateHeader() {
    setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER,
        protocol != null && protocol.getId() != 0 ? protocol.getName() : ""));
  }

  void recoverFile(ProtocolFile file) {
    logger.debug("save recovered protocol files for protocol {}", protocol);
    service.recover(file);
    showNotification(getTranslation(MESSAGE_PREFIX + RECOVERED, file.getFilename()));
    files.getListDataView().removeItem(file);
    files.getListDataView().refreshAll();
  }

  public long getProtocolId() {
    return protocol.getId();
  }

  public void setProtocolId(long id) {
    protocol = service.get(id).orElseThrow();
    files.setItems(service.deletedFiles(protocol));
    updateHeader();
  }
}
