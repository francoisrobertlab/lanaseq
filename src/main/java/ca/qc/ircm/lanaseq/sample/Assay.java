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

package ca.qc.ircm.lanaseq.sample;

import ca.qc.ircm.lanaseq.AppResources;
import java.util.Locale;

/**
 * Assay type.
 */
public enum Assay {
  CHEC_SEQ, CHIP_SEQ, CHIP_EXO, MNASE_SEQ, NET_SEQ, RNA_SEQ;

  /**
   * Returns assay's label to show in user interface.
   *
   * @param locale
   *          locale
   * @return assay's label to show in user interface
   */
  public static String getNullLabel(Locale locale) {
    final AppResources resources = new AppResources(Assay.class, locale);
    return resources.message("NULL");
  }

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
