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

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.web.ViewLayout.DATASETS;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.web.AccessDeniedView;
import ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView;
import ca.qc.ircm.lanaseq.web.SigninView;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import com.vaadin.flow.component.tabs.testbench.TabElement;
import com.vaadin.flow.component.tabs.testbench.TabsElement;
import com.vaadin.testbench.TestBenchTestCase;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Additional functions for TestBenchTestCase.
 */
public abstract class AbstractTestBenchTestCase extends TestBenchTestCase {
  private static final Logger logger = LoggerFactory.getLogger(AbstractTestBenchTestCase.class);
  @Value("http://localhost:${local.server.port}")
  protected String baseUrl;

  protected String homeUrl() {
    return baseUrl + "/";
  }

  protected String viewUrl(String view) {
    return baseUrl + "/" + view;
  }

  protected String viewUrl(String view, String parameters) {
    return baseUrl + "/" + view + "/" + parameters;
  }

  protected void openView(String view) {
    openView(view, null);
  }

  protected void openView(String view, String parameters) {
    String url = viewUrl(view);
    if (parameters != null && !parameters.isEmpty()) {
      url += "/" + parameters;
    }
    if (url.equals(getDriver().getCurrentUrl())) {
      getDriver().navigate().refresh();
    } else {
      getDriver().get(url);
    }
  }

  protected Locale currentLocale() {
    List<Locale> locales = Constants.getLocales();
    TabElement home =
        optional(() -> $(TabsElement.class).first().$(TabElement.class).first()).orElse(null);
    Optional<Locale> optlocale =
        locales.stream().filter(locale -> new AppResources(ViewLayout.class, locale).message(DATASETS)
            .equals(home != null ? home.getText() : "")).findAny();
    if (!optlocale.isPresent()) {
      optlocale = locales.stream()
          .filter(locale -> new AppResources(SigninView.class, locale)
              .message(TITLE, new AppResources(Constants.class, locale).message(APPLICATION_NAME))
              .equals(getDriver().getTitle()))
          .findAny();
    }
    if (!optlocale.isPresent()) {
      optlocale = locales.stream()
          .filter(locale -> new AppResources(UseForgotPasswordView.class, locale)
              .message(TITLE, new AppResources(Constants.class, locale).message(APPLICATION_NAME))
              .equals(getDriver().getTitle()))
          .findAny();
    }
    if (!optlocale.isPresent()) {
      optlocale = locales.stream()
          .filter(locale -> new AppResources(AccessDeniedView.class, locale)
              .message(TITLE, new AppResources(Constants.class, locale).message(APPLICATION_NAME))
              .equals(getDriver().getTitle()))
          .findAny();
    }
    return optlocale.orElse(null);
  }

  protected AppResources resources(Class<?> baseClass) {
    return new AppResources(baseClass, currentLocale());
  }

  protected <T> Optional<T> optional(Supplier<T> supplier) {
    try {
      return Optional.of(supplier.get());
    } catch (Throwable e) {
      return Optional.empty();
    }
  }

  protected void uploadFile(WebElement uploader, Path file) {
    logger.debug("Uploading file {} to uploader {} with class {}", file,
        uploader.getAttribute("id"), uploader.getAttribute("class"));
    logger.error("Not updated for Vaadin 10+");
    throw new UnsupportedOperationException("Not updated for Vaadin 10+");
  }
}
