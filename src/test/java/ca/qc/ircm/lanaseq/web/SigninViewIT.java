package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.web.SigninView.DISABLED;
import static ca.qc.ircm.lanaseq.web.SigninView.FAIL;
import static ca.qc.ircm.lanaseq.web.SigninView.LOCKED;
import static ca.qc.ircm.lanaseq.web.SigninView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordView;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.login.AbstractLogin.LoginEvent;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

/**
 * Integration tests for {@link SigninView}.
 */
@ServiceTestAnnotations
@AutoConfigureMockMvc
@WithAnonymousUser
public class SigninViewIT extends SpringUIUnitTest {

  @Autowired
  private MockMvcTester mvc;

  @Test
  @WithAnonymousUser
  public void sign_send_to_spring() {
    String username = "jonh.smith@ircm.qc.ca";
    String password = "pass1";
    SigninView view = navigate(SigninView.class);
    assertEquals(VIEW_NAME, view.getAction());
    ComponentEventListener<LoginEvent> listener = mock();
    ArgumentCaptor<LoginEvent> captor = ArgumentCaptor.forClass(LoginEvent.class);
    view.addLoginListener(listener);
    test(view).login(username, password);
    verify(listener).onComponentEvent(captor.capture());
    LoginEvent event = captor.getValue();
    assertEquals(username, event.getUsername());
    assertEquals(password, event.getPassword());
  }

  @Test
  @WithAnonymousUser
  public void sign_spring() {
    RequestBuilder requestBuilder = formLogin("/" + VIEW_NAME).user("jonh.smith@ircm.qc.ca")
        .password("pass1");
    MvcTestResultAssert resultAssert = mvc.perform(requestBuilder).assertThat();
    resultAssert.doesNotHaveFailed();
    resultAssert.hasRedirectedUrl("/");
    resultAssert.cookies().containsCookie("remember-me");
    resultAssert.cookies()
        .hasCookieSatisfying("remember-me", cookie -> assertNotEquals("pass1", cookie.getValue()));
    resultAssert.cookies().hasPath("remember-me", "/");
  }

  @Test
  public void sign_Fail_invalid_username() {
    RequestBuilder requestBuilder = formLogin("/" + VIEW_NAME).user("not.exists@ircm.qc.ca")
        .password("notright");
    MvcTestResultAssert resultAssert = mvc.perform(requestBuilder).assertThat();
    resultAssert.doesNotHaveFailed();
    resultAssert.hasRedirectedUrl("/" + VIEW_NAME + "?" + FAIL);
    resultAssert.cookies()
        .hasCookieSatisfying("remember-me", cookie -> assertNull(cookie.getValue()));
  }

  @Test
  public void sign_Fail_invalid_password() {
    RequestBuilder requestBuilder = formLogin("/" + VIEW_NAME).user("olivia.brown@ircm.qc.ca")
        .password("notright");
    MvcTestResultAssert resultAssert = mvc.perform(requestBuilder).assertThat();
    resultAssert.doesNotHaveFailed();
    resultAssert.hasRedirectedUrl("/" + VIEW_NAME + "?" + FAIL);
    resultAssert.cookies()
        .hasCookieSatisfying("remember-me", cookie -> assertNull(cookie.getValue()));
  }

  @Test
  public void sign_Disabled() {
    RequestBuilder requestBuilder = formLogin("/" + VIEW_NAME).user("ava.martin@ircm.qc.ca")
        .password("password");
    MvcTestResultAssert resultAssert = mvc.perform(requestBuilder).assertThat();
    resultAssert.doesNotHaveFailed();
    resultAssert.hasRedirectedUrl("/" + VIEW_NAME + "?" + DISABLED);
    resultAssert.cookies()
        .hasCookieSatisfying("remember-me", cookie -> assertNull(cookie.getValue()));
  }

  @Test
  public void sign_Locked() {
    Supplier<RequestBuilder> requestBuilder = () -> formLogin("/" + VIEW_NAME).user(
        "olivia.brown@ircm.qc.ca").password("notright");
    for (int i = 0; i < 5; i++) {
      mvc.perform(requestBuilder.get());
      try {
        Thread.sleep(1000); // Wait for page to load.
      } catch (InterruptedException e) {
        throw new IllegalStateException("Sleep was interrupted", e);
      }
    }
    MvcTestResultAssert resultAssert = mvc.perform(requestBuilder.get()).assertThat();
    resultAssert.doesNotHaveFailed();
    resultAssert.hasRedirectedUrl("/" + VIEW_NAME + "?" + LOCKED);
    resultAssert.cookies()
        .hasCookieSatisfying("remember-me", cookie -> assertNull(cookie.getValue()));
  }

  @Test
  public void forgotPassword() {
    SigninView view = navigate(SigninView.class);
    test(view).forgotPassword();
    assertTrue($(ForgotPasswordView.class).exists());
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void already_User() {
    navigate(VIEW_NAME, DatasetsView.class);
  }
}
