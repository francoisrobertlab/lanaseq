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

package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.FRENCH;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import com.vaadin.flow.component.upload.UploadI18N;
import java.util.Locale;

/**
 * Upload internationalization.
 */
public class UploadInternationalization {
  /**
   * Returns {@link UploadI18N} for specified locale. <br>
   * Falls back to English if no instance exists for specified locale.
   *
   * @param locale
   *          locale
   * @return {@link UploadI18N} for specified locale, never null
   */
  public static UploadI18N uploadI18N(Locale locale) {
    if (FRENCH.getLanguage().equals(locale.getLanguage())) {
      return frenchUploadI18N();
    }
    return englishUploadI18N();
  }

  /**
   * Returns {@link UploadI18N} for English.
   *
   * @return {@link UploadI18N} for English
   */
  public static UploadI18N englishUploadI18N() {
    final AppResources resources = new AppResources(Constants.class, ENGLISH);
    return new UploadI18N()
        .setAddFiles(new UploadI18N.AddFiles().setOne("Add file...").setMany("Add files..."))
        .setCancel(resources.message(CANCEL))
        .setDropFiles(
            new UploadI18N.DropFiles().setOne("Drop file here").setMany("Drop files here"))
        .setError(new UploadI18N.Error().setFileIsTooBig("The file is too big")
            .setIncorrectFileType("Wrong file type")
            .setTooManyFiles("Too many files were uploaded"))
        .setUploading(new UploadI18N.Uploading()
            .setError(
                new UploadI18N.Uploading.Error().setForbidden("You are not allowed to upload files")
                    .setServerUnavailable("The server is unavailable")
                    .setUnexpectedServerError("An unexpected error occured on server"))
            .setRemainingTime(new UploadI18N.Uploading.RemainingTime().setPrefix("Remains: ")
                .setUnknown("Unknown remaining time"))
            .setStatus(new UploadI18N.Uploading.Status().setConnecting("Connecting...")
                .setHeld("Waiting...").setProcessing("Uploading...").setStalled("Stalled...")));
  }

  /**
   * Returns {@link UploadI18N} for French.
   *
   * @return {@link UploadI18N} for French
   */
  public static UploadI18N frenchUploadI18N() {
    final AppResources resources = new AppResources(Constants.class, FRENCH);
    return new UploadI18N()
        .setAddFiles(new UploadI18N.AddFiles()
            .setOne("Ajouter un fichier...").setMany("Ajouter fichiers..."))
        .setCancel(resources.message(CANCEL))
        .setDropFiles(new UploadI18N.DropFiles().setOne("Déplacer un fichier ici")
            .setMany("Déplacer des fichiers ici"))
        .setError(new UploadI18N.Error().setFileIsTooBig("Le fichier est trop volumineux")
            .setIncorrectFileType("Le fichier n'est pas du bon type").setTooManyFiles(
                "Trop de fichiers téléchargés"))
        .setUploading(new UploadI18N.Uploading()
            .setError(new UploadI18N.Uploading.Error()
                .setForbidden("Vous n'avez pas la permission de télécharger des fichiers")
                .setServerUnavailable("Le serveur n'est pas disponible")
                .setUnexpectedServerError("Erreur inatendu lors du téléchargement"))
            .setRemainingTime(new UploadI18N.Uploading.RemainingTime().setPrefix("Il reste ")
                .setUnknown("Temps restant inconnu"))
            .setStatus(new UploadI18N.Uploading.Status().setConnecting("Connexion...")
                .setHeld("En attente...").setProcessing("En cours...").setStalled("Bloqué...")));
  }
}
