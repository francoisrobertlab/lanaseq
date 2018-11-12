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

import static ca.qc.ircm.lana.test.config.AnnotationFinder.findAnnotation;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.security.SecurityConfiguration;
import ca.qc.ircm.lana.user.web.UsersView;
import com.vaadin.testbench.TestBenchTestCase;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Optional;
import javax.inject.Inject;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.CipherService;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;

/**
 * Sets Shiro's subject.
 */
public class ShiroTestExecutionListener extends InjectIntoTestExecutionListener {
  public static final String REMEMBER_ME_COOKIE_NAME = "rememberMe";
  private static final String ANONYMOUS_VIEW = UsersView.VIEW_NAME;
  public static final String DOMAIN = "localhost";
  private static final Logger logger = LoggerFactory.getLogger(ShiroTestExecutionListener.class);
  private ThreadState threadState;
  @Inject
  private SecurityConfiguration securityConfiguration;

  @Override
  public void beforeTestMethod(TestContext testContext) throws Exception {
    long userId = 1L;
    boolean anonymous = false;
    WithSubject withSubject =
        findAnnotation(testContext.getTestClass(), testContext.getTestMethod(), WithSubject.class);
    if (withSubject != null) {
      userId = withSubject.userId();
      anonymous = withSubject.anonymous();
    }
    Optional<Long> optionalUser = anonymous ? Optional.empty() : Optional.of(userId);
    logger.debug("Set {} as user", optionalUser.isPresent() ? optionalUser.get() : "anonymous");
    setSubjectInThread(optionalUser);
    if (isTestBenchTest(testContext)) {
      setTestBenchUser(testContext, optionalUser);
    }
  }

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    if (threadState != null) {
      threadState.restore();
    }
  }

  private void setSubjectInThread(Optional<Long> userId) {
    Subject subject = mock(Subject.class);
    if (userId.isPresent()) {
      when(subject.getPrincipal()).thenReturn(userId.get());
    }
    ThreadContext.bind(new DefaultSecurityManager());
    threadState = new SubjectThreadState(subject);
    threadState.bind();
  }

  private boolean isTestBenchTest(TestContext testContext) {
    return TestBenchTestCase.class.isAssignableFrom(testContext.getTestClass());
  }

  private void setTestBenchUser(TestContext testContext, Optional<Long> userId) {
    if (userId.isPresent()) {
      AbstractTestBenchTestCase testInstance =
          (AbstractTestBenchTestCase) testContext.getTestInstance();
      WebDriver driver = testInstance.getDriver();
      testInstance.openView(ANONYMOUS_VIEW);
      String domain = DOMAIN;
      logger.debug("Set cookie for user {} in domain {}", userId.get(), domain);
      Cookie cookie = new Cookie("rememberMe", rememberCookie(userId.get()), domain, "/", null);
      driver.manage().addCookie(cookie);
    }
  }

  private String rememberCookie(Long userId) {
    PrincipalCollection principals =
        new SimplePrincipalCollection(userId, securityConfiguration.getRealmName());
    ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
    try (ObjectOutputStream output = new ObjectOutputStream(byteArrayOutput)) {
      output.writeObject(principals);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    CipherService cipherService = new AesCipherService();
    byte[] encrypted = cipherService
        .encrypt(byteArrayOutput.toByteArray(), securityConfiguration.getCipherKeyBytes())
        .getBytes();
    return Base64.encodeToString(encrypted);
  }
}
