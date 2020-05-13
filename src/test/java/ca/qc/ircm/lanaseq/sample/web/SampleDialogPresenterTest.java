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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import java.util.Locale;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class SampleDialogPresenterTest extends AbstractViewTestCase {
  private SampleDialogPresenter presenter;
  @Mock
  private SampleDialog dialog;
  @Autowired
  private SampleRepository repository;
  private Locale locale = Locale.ENGLISH;
  private AppResources webResources = new AppResources(Constants.class, locale);
  private String sampleId = "Test Sample";
  private String replicate = "Test Replicate";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    presenter = new SampleDialogPresenter();
    dialog.header = new H3();
    dialog.sampleId = new TextField();
    dialog.replicate = new TextField();
    dialog.save = new Button();
    dialog.cancel = new Button();
    dialog.delete = new Button();
    presenter.init(dialog);
  }

  private void fillForm() {
    dialog.sampleId.setValue(sampleId);
    dialog.replicate.setValue(replicate);
  }

  @Test
  public void getSample() {
    Sample sample = new Sample();
    presenter.setSample(sample);
    assertEquals(sample, presenter.getSample());
  }

  @Test
  public void setSample_NewSample() {
    Sample sample = new Sample();

    presenter.localeChange(locale);
    presenter.setSample(sample);

    assertEquals("", dialog.sampleId.getValue());
    assertEquals("", dialog.replicate.getValue());
  }

  @Test
  public void setSample_Sample() {
    Sample sample = repository.findById(1L).get();

    presenter.localeChange(locale);
    presenter.setSample(sample);

    assertEquals("FR1", dialog.sampleId.getValue());
    assertEquals("R1", dialog.replicate.getValue());
  }

  @Test
  public void setSample_BeforeLocaleChange() {
    Sample sample = repository.findById(1L).get();

    presenter.setSample(sample);
    presenter.localeChange(locale);

    assertEquals("FR1", dialog.sampleId.getValue());
    assertEquals("R1", dialog.replicate.getValue());
  }

  @Test
  public void setSample_Null() {
    presenter.localeChange(locale);
    presenter.setSample(null);

    assertEquals("", dialog.sampleId.getValue());
    assertEquals("", dialog.replicate.getValue());
  }

  @Test
  public void save_NameEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.sampleId.setValue("");

    presenter.save();

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.sampleId);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_ReplicateEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.replicate.setValue("");

    presenter.save();

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.replicate);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_NewSample() {
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    Sample sample = presenter.getSample();
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(replicate, sample.getReplicate());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void save_UpdateSample() {
    Sample sample = repository.findById(2L).get();
    presenter.setSample(sample);
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    sample = presenter.getSample();
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(replicate, sample.getReplicate());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void cancel_SampleProperties() {
    Sample sample = repository.findById(2L).get();
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();

    presenter.cancel();

    assertEquals("FR2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    verify(dialog).close();
    verify(dialog, never()).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void cancel_SamplePropertiesAfterValidationFail() {
    Sample sample = repository.findById(2L).get();
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();
    dialog.replicate.setValue("");
    presenter.save();

    presenter.cancel();

    assertEquals("FR2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    verify(dialog).close();
    verify(dialog, never()).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void cancel_Close() {
    presenter.init(dialog);
    presenter.localeChange(locale);

    presenter.cancel();

    verify(dialog).close();
    verify(dialog, never()).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void delete() {
    presenter.init(dialog);
    presenter.localeChange(locale);

    presenter.delete();

    verify(dialog).close();
    verify(dialog, never()).fireSavedEvent();
    verify(dialog).fireDeletedEvent();
  }
}
