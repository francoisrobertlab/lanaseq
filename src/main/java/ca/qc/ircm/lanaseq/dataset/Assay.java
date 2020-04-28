package ca.qc.ircm.lanaseq.dataset;

import ca.qc.ircm.lanaseq.AppResources;
import java.util.Locale;

/**
 * Assay type.
 */
public enum Assay {
  NULL, MNASE_SEQ, CHIP_SEQ, CHIP_EXO, NET_SEQ, RNA_SEQ;

  /**
   * Returns assay's label to show in user interface.
   *
   * @param locale
   *          locale
   * @return assay's label to show in user interface
   */
  public String getLabel(Locale locale) {
    final AppResources resources = new AppResources(Assay.class, locale);
    return resources.message(this.name());
  }
}
