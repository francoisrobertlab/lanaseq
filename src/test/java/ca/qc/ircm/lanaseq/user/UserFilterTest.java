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

package ca.qc.ircm.lanaseq.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class UserFilterTest {
  private UserFilter filter = new UserFilter();

  @Test
  public void test_EmailContains() {
    filter.emailContains = "test";

    assertTrue(filter.test(email("My.test@abc.com")));
    assertTrue(filter.test(email("Test.my@abc.com")));
    assertTrue(filter.test(email("My.test.my@abc.com")));
    assertTrue(filter.test(email("My.TEST.my@abc.com")));
    assertTrue(filter.test(email("abc@test.com")));
    assertTrue(filter.test(email("abc@a.test")));
    assertTrue(filter.test(email("test@test.com")));
    assertFalse(filter.test(email(null)));
    assertFalse(filter.test(email("")));
    assertFalse(filter.test(email("christian@abc.com")));
  }

  @Test
  public void test_EmailContainsNull() {
    filter.emailContains = null;

    assertTrue(filter.test(email("My.test@abc.com")));
    assertTrue(filter.test(email("Test.my@abc.com")));
    assertTrue(filter.test(email("My.test.my@abc.com")));
    assertTrue(filter.test(email("My.TEST.my@abc.com")));
    assertTrue(filter.test(email("abc@test.com")));
    assertTrue(filter.test(email("abc@a.test")));
    assertTrue(filter.test(email("test@test.com")));
    assertTrue(filter.test(email(null)));
    assertTrue(filter.test(email("")));
    assertTrue(filter.test(email("christian@abc.com")));
  }

  @Test
  public void test_NameContains() {
    filter.nameContains = "test";

    assertTrue(filter.test(name("My test")));
    assertTrue(filter.test(name("Test my")));
    assertTrue(filter.test(name("My test my")));
    assertTrue(filter.test(name("My TEST my")));
    assertFalse(filter.test(name(null)));
    assertFalse(filter.test(name("")));
    assertFalse(filter.test(name("christian")));
  }

  @Test
  public void test_NameContainsNull() {
    filter.nameContains = null;

    assertTrue(filter.test(name("My test")));
    assertTrue(filter.test(name("Test my")));
    assertTrue(filter.test(name("My test my")));
    assertTrue(filter.test(name("My TEST my")));
    assertTrue(filter.test(name(null)));
    assertTrue(filter.test(name("")));
    assertTrue(filter.test(name("christian")));
  }

  @Test
  public void test_ActiveFalse() {
    filter.active = false;

    assertTrue(filter.test(active(false)));
    assertFalse(filter.test(active(true)));
  }

  @Test
  public void test_ActiveTrue() {
    filter.active = true;

    assertFalse(filter.test(active(false)));
    assertTrue(filter.test(active(true)));
  }

  @Test
  public void test_ActiveNull() {
    filter.active = null;

    assertTrue(filter.test(active(false)));
    assertTrue(filter.test(active(true)));
  }

  @Test
  public void test_EmailAndNameContains() {
    filter.emailContains = "test";
    filter.nameContains = "test";

    assertTrue(filter.test(emailName("My.test@abc.com", "My test")));
    assertTrue(filter.test(emailName("Test.my@abc.com", "Test my")));
    assertTrue(filter.test(emailName("My.test.my@abc.com", "My test my")));
    assertTrue(filter.test(emailName("My.TEST.my@abc.com", "My TEST my")));
    assertTrue(filter.test(emailName("abc@test.com", "My test")));
    assertTrue(filter.test(emailName("abc@a.test", "My test")));
    assertTrue(filter.test(emailName("test@test.com", "My test")));
    assertFalse(filter.test(emailName("My.test@abc.com", "christian")));
    assertFalse(filter.test(emailName("Test.my@abc.com", "christian")));
    assertFalse(filter.test(emailName("My.test.my@abc.com", "christian")));
    assertFalse(filter.test(emailName("My.TEST.my@abc.com", "christian")));
    assertFalse(filter.test(emailName("abc@test.com", "christian")));
    assertFalse(filter.test(emailName("abc@a.test", "christian")));
    assertFalse(filter.test(emailName("test@test.com", "christian")));
    assertFalse(filter.test(emailName("christian@abc.com", "My test")));
    assertFalse(filter.test(emailName("christian@abc.com", "Test my")));
    assertFalse(filter.test(emailName("christian@abc.com", "My test my")));
    assertFalse(filter.test(emailName("christian@abc.com", "My TEST my")));
    assertFalse(filter.test(emailName(null, null)));
    assertFalse(filter.test(emailName("", "")));
    assertFalse(filter.test(emailName(null, "My test")));
    assertFalse(filter.test(emailName("", "My test")));
    assertFalse(filter.test(emailName("my.test@abc.com", null)));
    assertFalse(filter.test(emailName("my.test@abc.com", "")));
  }

  private User email(String email) {
    User user = new User();
    user.setEmail(email);
    return user;
  }

  private User name(String name) {
    User user = new User();
    user.setName(name);
    return user;
  }

  private User active(boolean active) {
    User user = new User();
    user.setActive(active);
    return user;
  }

  private User emailName(String email, String name) {
    User user = new User();
    user.setEmail(email);
    user.setName(name);
    return user;
  }
}
