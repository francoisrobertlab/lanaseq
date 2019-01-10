package ca.qc.ircm.lana.user;

import static ca.qc.ircm.lana.text.Strings.comparable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Filters users.
 */
public class UserFilter implements Predicate<User> {
  public String emailContains;
  public String nameContains;
  public String laboratoryNameContains;

  @Override
  public boolean test(User user) {
    boolean test = true;
    if (emailContains != null) {
      test &= comparable(replaceNull(user.getEmail())).contains(comparable(emailContains));
    }
    if (nameContains != null) {
      test &= comparable(replaceNull(user.getName())).contains(comparable(nameContains));
    }
    if (laboratoryNameContains != null) {
      test &= comparable(replaceNull(user.getLaboratory().getName()))
          .contains(comparable(laboratoryNameContains));
    }
    return test;
  }

  private String replaceNull(String value) {
    return Objects.toString(value, "");
  }
}
