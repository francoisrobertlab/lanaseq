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

package ca.qc.ircm.lanaseq.test.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Fixes SecurityContextHolderStrategy present in SecurityContextHolder.
 */
public class FixSecurityContextHolderStrategyExecutionListener
    implements TestExecutionListener, InjectDependencies {
  @Autowired
  private SecurityContextHolderStrategy injectedStrategy;
  private static final Logger logger =
      LoggerFactory.getLogger(FixSecurityContextHolderStrategyExecutionListener.class);

  @Override
  public void beforeTestClass(TestContext testContext) {
    injectDependencies(testContext.getApplicationContext());
    SecurityContextHolderStrategy holderStrategy = SecurityContextHolder.getContextHolderStrategy();
    if (injectedStrategy != holderStrategy) {
      logger.warn(
          "Missmatch between injected SecurityContextHolderStrategy {} and strategy present in holder {}, "
              + "setting injected strategy in context holder",
          injectedStrategy, holderStrategy);
      SecurityContextHolder.setContextHolderStrategy(injectedStrategy);
    }
  }
}
