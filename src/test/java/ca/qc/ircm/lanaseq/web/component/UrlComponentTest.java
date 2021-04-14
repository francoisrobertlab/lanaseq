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

package ca.qc.ircm.lanaseq.web.component;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import org.junit.jupiter.api.Test;

@NonTransactionalTestAnnotations
public class UrlComponentTest extends AbstractViewTestCase {
  private UrlComponentForTest urlComponent = new UrlComponentForTest();

  @Test
  public void getUrl() {
    String url = urlComponent.getUrl(DatasetsView.VIEW_NAME);
    assertEquals("/" + DatasetsView.VIEW_NAME, url);
  }

  private class UrlComponentForTest implements UrlComponent {
  }
}
