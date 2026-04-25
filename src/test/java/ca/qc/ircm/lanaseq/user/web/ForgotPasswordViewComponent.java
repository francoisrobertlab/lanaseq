package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.ID;

import ca.qc.ircm.lanaseq.test.config.SeleniumComponent;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link ForgotPasswordView}.
 */
public class ForgotPasswordViewComponent extends SeleniumComponent {

  public static Function<WebDriver, ForgotPasswordViewComponent> find() {
    return d -> new ForgotPasswordViewComponent(d.findElement(By.id(ID)));
  }

  public ForgotPasswordViewComponent(WebElement forgotPasswordView) {
    super(forgotPasswordView);
    assert ID.equals(forgotPasswordView.getAttribute("id"));
  }
}
