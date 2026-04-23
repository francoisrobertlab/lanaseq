package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.ID;

import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link ForgotPasswordView}.
 */
public class ForgotPasswordViewPage {

  private final WebElement view;

  public static Function<WebDriver, ForgotPasswordViewPage> find() {
    return d -> new ForgotPasswordViewPage(d.findElement(By.id(ID)));
  }

  public ForgotPasswordViewPage(WebElement forgotPasswordView) {
    assert ID.equals(forgotPasswordView.getAttribute("id"));
    this.view = forgotPasswordView;
  }
}
