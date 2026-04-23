package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.web.SigninView.ID;

import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link SigninView}.
 */
public class SigninViewPage {

  private final WebElement view;

  public static Function<WebDriver, SigninViewPage> find() {
    return d -> new SigninViewPage(d.findElement(By.id(ID)));
  }

  public SigninViewPage(WebElement signinView) {
    assert ID.equals(signinView.getAttribute("id"));
    this.view = signinView;
  }

  public WebElement username() {
    return view.findElement(By.id("vaadinLoginUsername"));
  }

  public WebElement password() {
    return view.findElement(By.id("vaadinLoginPassword"));
  }

  public WebElement signin() {
    return view.findElement(By.cssSelector("vaadin-button[slot='submit']"));
  }

  public WebElement errorMessageDescription() {
    WebElement loginForm = view.getShadowRoot()
        .findElement(By.cssSelector("vaadin-login-form-wrapper"));
    return loginForm.getShadowRoot()
        .findElement(By.cssSelector("div[part='error-message-description']"));
  }

  public WebElement forgotPassword() {
    return view.findElement(By.cssSelector("vaadin-button[slot='forgot-password']"));
  }
}
