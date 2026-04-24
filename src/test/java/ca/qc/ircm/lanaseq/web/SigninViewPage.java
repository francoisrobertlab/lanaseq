package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.web.SigninView.ID;

import ca.qc.ircm.lanaseq.test.config.SeleniumComponent;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link SigninView}.
 */
public class SigninViewPage extends SeleniumComponent {

  public static Function<WebDriver, SigninViewPage> find() {
    return d -> new SigninViewPage(d.findElement(By.id(ID)));
  }

  public SigninViewPage(WebElement signinView) {
    super(signinView);
    assert ID.equals(signinView.getAttribute("id"));
  }

  public WebElement username() {
    return element.findElement(By.id("vaadinLoginUsername"));
  }

  public WebElement password() {
    return element.findElement(By.id("vaadinLoginPassword"));
  }

  public WebElement signin() {
    return element.findElement(By.cssSelector("vaadin-button[slot='submit']"));
  }

  public WebElement errorMessageDescription() {
    WebElement loginForm = element.getShadowRoot()
        .findElement(By.cssSelector("vaadin-login-form-wrapper"));
    return loginForm.getShadowRoot()
        .findElement(By.cssSelector("div[part='error-message-description']"));
  }

  public WebElement forgotPassword() {
    return element.findElement(By.cssSelector("vaadin-button[slot='forgot-password']"));
  }
}
