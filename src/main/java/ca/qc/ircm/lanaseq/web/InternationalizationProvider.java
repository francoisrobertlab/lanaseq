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

import com.vaadin.flow.i18n.I18NProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link I18NProvider}.
 */
@Component
public class InternationalizationProvider implements I18NProvider {
  private static final long serialVersionUID = -4587139159545162274L;
  private static final Logger logger = LoggerFactory.getLogger(InternationalizationProvider.class);
  private List<Locale> locales = Collections.unmodifiableList(Arrays.asList(Locale.ENGLISH));

  @Override
  public List<Locale> getProvidedLocales() {
    return locales;
  }

  @Override
  public String getTranslation(String key, Locale locale, Object... params) {
    logger.warn("unsupported getTranslation method called with key {}", key);
    return "!{" + key + "}!";
  }
}
