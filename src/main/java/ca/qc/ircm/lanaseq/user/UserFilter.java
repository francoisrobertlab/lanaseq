package ca.qc.ircm.lanaseq.user;

import static ca.qc.ircm.lanaseq.text.Strings.comparable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Filters users.
 */
public class UserFilter implements Predicate<User> {
  public String emailContains;
  public String nameContains;
  public Boolean active;

  @Override
  public boolean test(User user) {
    boolean test = true;
    if (emailContains != null) {
      test &= comparable(replaceNull(user.getEmail())).contains(comparable(emailContains));
    }
    if (nameContains != null) {
      test &= comparable(replaceNull(user.getName())).contains(comparable(nameContains));
    }
    if (active != null) {
      test &= active == user.isActive();
    }
    return test;
  }

  private String replaceNull(String value) {
    return Objects.toString(value, "");
  }
}
