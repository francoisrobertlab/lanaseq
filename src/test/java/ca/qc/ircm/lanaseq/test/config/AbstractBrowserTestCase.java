package ca.qc.ircm.lanaseq.test.config;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.web.ViewLayout.DATASETS;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.jobs.Job;
import ca.qc.ircm.lanaseq.jobs.JobService;
import ca.qc.ircm.lanaseq.security.web.AccessDeniedView;
import ca.qc.ircm.lanaseq.user.web.PasswordView;
import ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView;
import ca.qc.ircm.lanaseq.web.SigninView;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import com.vaadin.flow.component.sidenav.testbench.SideNavElement;
import com.vaadin.flow.component.sidenav.testbench.SideNavItemElement;
import com.vaadin.testbench.BrowserTestBase;
import com.vaadin.testbench.IPAddress;
import com.vaadin.testbench.browser.BrowserTestInfo;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;

/**
 * Additional functions for BrowserTestBase.
 */
public abstract class AbstractBrowserTestCase extends BrowserTestBase {

  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final String LAYOUT_PREFIX = messagePrefix(ViewLayout.class);
  private static final String SIGNIN_PREFIX = messagePrefix(SigninView.class);
  private static final String USE_FORGOT_PASSWORD_PREFIX = messagePrefix(
      UseForgotPasswordView.class);
  private static final String PASSWORD_PREFIX = messagePrefix(PasswordView.class);
  private static final String ACCESS_DENIED_PREFIX = messagePrefix(AccessDeniedView.class);
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(AbstractBrowserTestCase.class);
  @Value("${local.server.port}")
  protected int port;
  @Value("${server.servlet.context-path:}")
  protected String contextPath;
  private boolean runOnHub = false;
  @Autowired
  private AppConfiguration configuration;
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private JobService jobService;

  @BeforeEach
  @SuppressWarnings("JUnitMalformedDeclaration") // Works because of Vaadin's JUnit5 extension.
  public void setRunOnHub(BrowserTestInfo browserTestInfo) {
    runOnHub = browserTestInfo.hubHostname() != null;
  }

  @BeforeEach
  public void setServerUrl()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method setServerUrl = AppConfiguration.class.getDeclaredMethod("setServerUrl", String.class);
    setServerUrl.setAccessible(true);
    setServerUrl.invoke(configuration, baseUrl());
  }

  /**
   * Remove all jobs in JobService.
   */
  @AfterEach
  public void clearJobs() throws NoSuchFieldException, IllegalAccessException {
    Field field = JobService.class.getDeclaredField("jobs");
    field.setAccessible(true);
    @SuppressWarnings("unchecked") List<Job> jobs = (List<Job>) field.get(jobService);
    jobs.clear();
  }

  protected String baseUrl() {
    String host = runOnHub ? IPAddress.findSiteLocalAddress() : "localhost";
    return "http://" + host + ":" + port;
  }

  protected String homeUrl() {
    return baseUrl() + contextPath + "/";
  }

  protected String viewUrl(String view) {
    return baseUrl() + contextPath + "/" + view;
  }

  protected String viewUrl(String view, String parameters) {
    return baseUrl() + contextPath + "/" + view + "/" + parameters;
  }

  protected void openView(String view) {
    openView(view, null);
  }

  protected void openView(String view, @Nullable String parameters) {
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
    Function<Locale, String> applicationName = locale -> messageSource.getMessage(
        CONSTANTS_PREFIX + APPLICATION_NAME, null, locale);
    SideNavItemElement home = optional(
        () -> $(SideNavElement.class).first().$(SideNavItemElement.class).first()).orElse(null);
    Optional<Locale> optlocale = locales.stream().filter(
        locale -> messageSource.getMessage(LAYOUT_PREFIX + DATASETS, null, locale)
            .equals(home != null ? home.getLabel() : "")).findAny();
    if (optlocale.isEmpty()) {
      optlocale = locales.stream().filter(locale -> messageSource.getMessage(SIGNIN_PREFIX + TITLE,
              new Object[]{applicationName.apply(locale)}, locale).equals(getDriver().getTitle()))
          .findAny();
    }
    if (optlocale.isEmpty()) {
      optlocale = locales.stream().filter(
              locale -> messageSource.getMessage(USE_FORGOT_PASSWORD_PREFIX + TITLE,
                  new Object[]{applicationName.apply(locale)}, locale).equals(getDriver().getTitle()))
          .findAny();
    }
    if (optlocale.isEmpty()) {
      optlocale = locales.stream().filter(
              locale -> messageSource.getMessage(PASSWORD_PREFIX + TITLE,
                  new Object[]{applicationName.apply(locale)}, locale).equals(getDriver().getTitle()))
          .findAny();
    }
    if (optlocale.isEmpty()) {
      optlocale = locales.stream().filter(
              locale -> messageSource.getMessage(ACCESS_DENIED_PREFIX + TITLE,
                  new Object[]{applicationName.apply(locale)}, locale).equals(getDriver().getTitle()))
          .findAny();
    }
    return optlocale.orElse(Constants.DEFAULT_LOCALE);
  }

  protected <T> Optional<T> optional(Supplier<T> supplier) {
    try {
      return Optional.of(supplier.get());
    } catch (Throwable e) {
      return Optional.empty();
    }
  }
}
