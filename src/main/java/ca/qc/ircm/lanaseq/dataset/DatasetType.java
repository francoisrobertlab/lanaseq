package ca.qc.ircm.lanaseq.dataset;

import ca.qc.ircm.lanaseq.AppResources;
import java.util.Locale;

/**
 * Dataset type.
 */
public enum DatasetType {
  NULL, IMMUNO_PRECIPITATION, INPUT;

  /**
   * Returns assay's label to show in user interface.
   *
   * @param locale
   *          locale
   * @return assay's label to show in user interface
   */
  public String getLabel(Locale locale) {
    final AppResources resources = new AppResources(DatasetType.class, locale);
    return resources.message(this.name());
  }
}
