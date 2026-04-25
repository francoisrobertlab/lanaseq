package ca.qc.ircm.lanaseq.test.config;

import org.openqa.selenium.WebElement;

/**
 * Web element wrapping WebElement from Selenium.
 */
public class SeleniumComponent {

  protected final WebElement element;

  public SeleniumComponent(WebElement element) {
    this.element = element;
  }
}
