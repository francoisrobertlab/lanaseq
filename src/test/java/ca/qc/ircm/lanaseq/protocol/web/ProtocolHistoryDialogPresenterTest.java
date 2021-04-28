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
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.RECOVER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.RECOVERED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolFileRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.data.provider.DataProviderListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link ProtocolHistoryDialogPresenter}.
 */
@ServiceTestAnnotations
@WithMockUser
public class ProtocolHistoryDialogPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private ProtocolHistoryDialogPresenter presenter;
  @Mock
  private ProtocolHistoryDialog dialog;
  @MockBean
  private ProtocolService service;
  @Mock
  private DataProviderListener<ProtocolFile> filesDataProviderListener;
  @Captor
  private ArgumentCaptor<Protocol> protocolCaptor;
  @Captor
  private ArgumentCaptor<Collection<ProtocolFile>> filesCaptor;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ProtocolHistoryDialog.class, locale);
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private ProtocolFileRepository fileRepository;
  private Protocol protocol;
  private byte[] fileContent = new byte[5120];
  private Random random = new Random();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    dialog.header = new H3();
    dialog.files = new Grid<>();
    dialog.filename = dialog.files.addColumn(file -> file.getId(), FILENAME);
    dialog.recover = dialog.files.addColumn(file -> file.getId(), RECOVER);
    presenter.init(dialog);
    presenter.localeChange(locale);
    protocol = repository.findById(3L).get();
    random.nextBytes(fileContent);
    when(service.deletedFiles(any())).then(i -> {
      Protocol protocol = i.getArgument(0);
      if (protocol != null && protocol.getId() != null) {
        return fileRepository.findByProtocolAndDeletedTrue(protocol);
      } else {
        return new ArrayList<>();
      }
    });
  }

  @Test
  public void recoverFile() {
    presenter.setProtocol(protocol);
    ProtocolFile file = fileRepository.findById(3L).get();
    dialog.files.getDataProvider().addDataProviderListener(filesDataProviderListener);
    presenter.recoverFile(file);
    verify(service).recover(file);
    dialog.showNotification(resources.message(RECOVERED, file.getFilename()));
    assertTrue(items(dialog.files).isEmpty());
    verify(filesDataProviderListener).onDataChange(any());
  }

  @Test
  public void getProtocol() {
    presenter.setProtocol(protocol);
    Protocol protocol = presenter.getProtocol();
    assertSame(this.protocol, protocol);
  }

  @Test
  public void setProtocol() {
    presenter.setProtocol(protocol);
    List<ProtocolFile> expectedFiles = fileRepository.findByProtocolAndDeletedTrue(protocol);
    List<ProtocolFile> files = items(dialog.files);
    assertEquals(expectedFiles.size(), files.size());
    for (int i = 0; i < expectedFiles.size(); i++) {
      assertEquals(expectedFiles.get(i), files.get(i));
    }
  }

  @Test
  public void setProtocol_Null() {
    assertThrows(NullPointerException.class, () -> {
      presenter.setProtocol(null);
    });
  }

  @Test
  public void setProtocol_NoFiles() {
    when(service.deletedFiles(any())).thenReturn(new ArrayList<>());
    presenter.setProtocol(new Protocol());
    assertTrue(items(dialog.files).isEmpty());
  }
}
