package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.ViewLayout.DATASETS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.EXIT_SWITCH_USER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.ID;
import static ca.qc.ircm.lanaseq.web.ViewLayout.JOBS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NAV;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROFILE;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROTOCOLS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PUBLIC_FILES;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SAMPLES;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SIGNOUT;
import static ca.qc.ircm.lanaseq.web.ViewLayout.USERS;

import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link ViewLayout}.
 */
public class ViewLayoutPage {

  private final WebElement view;

  public static Function<WebDriver, ViewLayoutPage> find() {
    return d -> new ViewLayoutPage(d.findElement(By.id(ID)));
  }

  public ViewLayoutPage(WebElement viewLayout) {
    assert ID.equals(viewLayout.getAttribute("id"));
    this.view = viewLayout;
  }

  public WebElement datasets() {
    return view.findElement(By.id(styleName(DATASETS, NAV)));
  }

  public WebElement samples() {
    return view.findElement(By.id(styleName(SAMPLES, NAV)));
  }

  public WebElement protocols() {
    return view.findElement(By.id(styleName(PROTOCOLS, NAV)));
  }

  public WebElement publicFiles() {
    return view.findElement(By.id(styleName(PUBLIC_FILES, NAV)));
  }

  public WebElement jobs() {
    return view.findElement(By.id(styleName(JOBS, NAV)));
  }

  public WebElement profile() {
    return view.findElement(By.id(styleName(PROFILE, NAV)));
  }

  public WebElement users() {
    return view.findElement(By.id(styleName(USERS, NAV)));
  }

  public WebElement exitSwitchUser() {
    return view.findElement(By.id(styleName(EXIT_SWITCH_USER, NAV)));
  }

  public WebElement signout() {
    return view.findElement(By.id(styleName(SIGNOUT, NAV)));
  }
}
