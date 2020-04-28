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

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.ERROR;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.PRIMARY;
import static ca.qc.ircm.lanaseq.Constants.REMOVE;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.protocol.ProtocolFileProperties.FILENAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.FILES;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.uploadI18N;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.ByteArrayStreamResourceWriter;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Protocols dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProtocolDialog extends Dialog implements LocaleChangeObserver, NotificationComponent {
  public static final String ID = "protocols-dialog";
  public static final String HEADER = "header";
  public static final String FILES_ERROR = property(FILES, ERROR);
  public static final String FILES_REQUIRED = property(FILES, REQUIRED);
  public static final String FILES_IOEXCEPTION = property(FILES, "ioexception");
  public static final String FILES_OVER_MAXIMUM = property(FILES, "overmaximum");
  public static final int MAXIMUM_FILES_SIZE = 20 * 1024 * 1024; // 20MB
  public static final int MAXIMUM_FILES_COUNT = 6;
  public static final String REMOVE_BUTTON =
      "<vaadin-button class='" + REMOVE + "' theme='" + ERROR + "' on-click='removeFile'>"
          + "<iron-icon icon='vaadin:trash' slot='prefix'></iron-icon>" + "</vaadin-button>";
  public static final String SAVED = "saved";
  private static final long serialVersionUID = -7797831034001410430L;
  protected H3 header = new H3();
  protected TextField name = new TextField();
  protected MultiFileMemoryBuffer uploadBuffer = new MultiFileMemoryBuffer();
  protected Upload upload = new Upload(uploadBuffer);
  protected Grid<ProtocolFile> files = new Grid<>();
  protected Column<ProtocolFile> filename;
  protected Column<ProtocolFile> remove;
  protected Div filesError = new Div();
  protected Button save = new Button();
  protected Button cancel = new Button();
  @Autowired
  private ProtocolDialogPresenter presenter;

  public ProtocolDialog() {
  }

  ProtocolDialog(ProtocolDialogPresenter presenter) {
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
    FormLayout form = new FormLayout(name);
    HorizontalLayout buttonsLayout = new HorizontalLayout(save, cancel);
    layout.add(header, form, upload, files, filesError, buttonsLayout);
    header.setId(id(HEADER));
    name.setId(id(NAME));
    upload.setId(id(UPLOAD));
    upload.setMaxFileSize(MAXIMUM_FILES_SIZE);
    upload.setMaxFiles(MAXIMUM_FILES_COUNT);
    upload.setMinHeight("2.5em");
    upload.addSucceededListener(event -> presenter.addFile(event.getFileName(),
        uploadBuffer.getInputStream(event.getFileName()), getLocale()));
    files.setId(id(FILES));
    files.setHeight("15em");
    files.setMinHeight("15em");
    files.setWidth("45em");
    files.setMinWidth("45em");
    filename = files.addColumn(new ComponentRenderer<>(file -> filenameAnchor(file)), FILENAME)
        .setKey(FILENAME).setComparator(NormalizedComparator.of(ProtocolFile::getFilename));
    remove =
        files
            .addColumn(TemplateRenderer.<ProtocolFile>of(REMOVE_BUTTON)
                .withEventHandler("removeFile", file -> presenter.removeFile(file)), REMOVE)
            .setKey(REMOVE);
    filesError.setId(id(FILES_ERROR));
    filesError.addClassName(ERROR_TEXT);
    save.setId(id(SAVE));
    save.setThemeName(PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save(getLocale()));
    cancel.setId(id(CANCEL));
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> presenter.cancel());
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
    AppResources protocolResources = new AppResources(Protocol.class, getLocale());
    AppResources protocolFileResources = new AppResources(ProtocolFile.class, getLocale());
    AppResources webResources = new AppResources(Constants.class, getLocale());
    updateHeader();
    name.setLabel(protocolResources.message(NAME));
    upload.setI18n(uploadI18N(getLocale()));
    filename.setHeader(protocolFileResources.message(FILENAME));
    remove.setHeader(webResources.message(REMOVE));
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
    presenter.localeChange(getLocale());
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(ProtocolDialog.class, getLocale());
    Protocol protocol = presenter.getProtocol();
    if (protocol != null && protocol.getId() != null) {
      header.setText(resources.message(HEADER, 1, protocol.getName()));
    } else {
      header.setText(resources.message(HEADER, 0));
    }
  }

  /**
   * Adds listener to be informed when a protocol was saved.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration
      addSavedListener(ComponentEventListener<SavedEvent<ProtocolDialog>> listener) {
    return addListener((Class) SavedEvent.class, listener);
  }

  void fireSavedEvent() {
    fireEvent(new SavedEvent<>(this, true));
  }

  public Protocol getProtocol() {
    return presenter.getProtocol();
  }

  public void setProtocol(Protocol protocol) {
    presenter.setProtocol(protocol);
    updateHeader();
  }
}
