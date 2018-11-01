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

package ca.qc.ircm.lana.test.config;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Injects dependencies in listener before test class.
 */
public class InjectIntoTestExecutionListener extends AbstractTestExecutionListener {
  @Override
  public void beforeTestClass(TestContext testContext) throws Exception {
    injectDependencies(testContext);
  }

  protected void injectDependencies(TestContext testContext) {
    testContext.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(this);
  }
}
