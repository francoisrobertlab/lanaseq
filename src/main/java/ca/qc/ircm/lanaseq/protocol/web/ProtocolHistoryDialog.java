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

import static ca.qc.ircm.lanaseq.protocol.ProtocolFileProperties.FILENAME;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.ByteArrayStreamResourceWriter;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import javax.annotation.PostConstruct;
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
      + ButtonVariant.LUMO_SUCCESS.getVariantName() + "' on-click='recoverFile'>"
      + "<iron-icon icon='vaadin:ambulance' slot='prefix'></iron-icon>" + "</vaadin-button>";
  public static final String RECOVERED = "recovered";
  private static final long serialVersionUID = -7797831034001410430L;
  protected H3 header = new H3();
  protected Grid<ProtocolFile> files = new Grid<>();
  protected Column<ProtocolFile> filename;
  protected Column<ProtocolFile> recover;
  @Autowired
  private ProtocolHistoryDialogPresenter presenter;

  public ProtocolHistoryDialog() {
  }

  ProtocolHistoryDialog(ProtocolHistoryDialogPresenter presenter) {
    this.presenter = presenter;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    setId(ID);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.setMaxWidth("60em");
    layout.setMinWidth("22em");
    layout.add(header, files);
    header.setId(id(HEADER));
    files.setId(id(FILES));
    files.setHeight("15em");
    files.setMinHeight("15em");
    files.setWidth("45em");
    files.setMinWidth("45em");
    filename = files.addColumn(new ComponentRenderer<>(file -> filenameAnchor(file)), FILENAME)
        .setKey(FILENAME).setComparator(NormalizedComparator.of(ProtocolFile::getFilename));
    recover =
        files
            .addColumn(TemplateRenderer.<ProtocolFile>of(RECOVER_BUTTON)
                .withEventHandler("recoverFile", file -> presenter.recoverFile(file)), RECOVER)
            .setKey(RECOVER);
    presenter.init(this);
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
    AppResources resources = new AppResources(ProtocolHistoryDialog.class, getLocale());
    AppResources protocolFileResources = new AppResources(ProtocolFile.class, getLocale());
    updateHeader();
    filename.setHeader(protocolFileResources.message(FILENAME));
    recover.setHeader(resources.message(RECOVER));
    presenter.localeChange(getLocale());
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(ProtocolHistoryDialog.class, getLocale());
    Protocol protocol = presenter.getProtocol();
    header.setText(resources.message(HEADER,
        protocol != null && protocol.getId() != null ? protocol.getName() : ""));
  }

  public Protocol getProtocol() {
    return presenter.getProtocol();
  }

  public void setProtocol(Protocol protocol) {
    presenter.setProtocol(protocol);
    updateHeader();
  }
}
