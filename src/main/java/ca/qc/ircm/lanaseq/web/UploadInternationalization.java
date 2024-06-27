package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.FRENCH;

import com.vaadin.flow.component.upload.UploadI18N;
import java.util.Arrays;
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
    return new UploadI18N()
        .setAddFiles(new UploadI18N.AddFiles().setOne("Add file...").setMany("Add files..."))
        .setDropFiles(
            new UploadI18N.DropFiles().setOne("Drop file here").setMany("Drop files here"))
        .setError(new UploadI18N.Error().setFileIsTooBig("The file is too big")
            .setIncorrectFileType("Wrong file type")
            .setTooManyFiles("Too many files were uploaded"))
        .setUploading(new UploadI18N.Uploading()
            .setError(
                new UploadI18N.Uploading.Error().setForbidden("You are not allowed to upload files")
                    .setServerUnavailable("The server is unavailable")
                    .setUnexpectedServerError("An unexpected error occurred on server"))
            .setRemainingTime(new UploadI18N.Uploading.RemainingTime().setPrefix("Remains: ")
                .setUnknown("Unknown remaining time"))
            .setStatus(new UploadI18N.Uploading.Status().setConnecting("Connecting...")
                .setHeld("Waiting...").setProcessing("Uploading...").setStalled("Stalled...")))
        .setUnits(new UploadI18N.Units()
            .setSize(Arrays.asList("B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")));
  }

  /**
   * Returns {@link UploadI18N} for French.
   *
   * @return {@link UploadI18N} for French
   */
  public static UploadI18N frenchUploadI18N() {
    return new UploadI18N()
        .setAddFiles(new UploadI18N.AddFiles().setOne("Ajouter un fichier...")
            .setMany("Ajouter fichiers..."))
        .setDropFiles(new UploadI18N.DropFiles().setOne("Déplacer un fichier ici")
            .setMany("Déplacer des fichiers ici"))
        .setError(new UploadI18N.Error().setFileIsTooBig("Le fichier est trop volumineux")
            .setIncorrectFileType("Le fichier n'est pas du bon type").setTooManyFiles(
                "Trop de fichiers téléchargés"))
        .setUploading(new UploadI18N.Uploading()
            .setError(new UploadI18N.Uploading.Error()
                .setForbidden("Vous n'avez pas la permission de télécharger des fichiers")
                .setServerUnavailable("Le serveur n'est pas disponible")
                .setUnexpectedServerError("Erreur inattendu lors du téléchargement"))
            .setRemainingTime(new UploadI18N.Uploading.RemainingTime().setPrefix("Il reste ")
                .setUnknown("Temps restant inconnu"))
            .setStatus(new UploadI18N.Uploading.Status().setConnecting("Connexion...")
                .setHeld("En attente...").setProcessing("En cours...").setStalled("Bloqué...")))
        .setUnits(new UploadI18N.Units()
            .setSize(Arrays.asList("B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")));
  }
}
