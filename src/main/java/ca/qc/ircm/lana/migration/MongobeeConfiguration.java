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

package ca.qc.ircm.lana.migration;

import ca.qc.ircm.lana.security.AdminUserConfiguration;
import ca.qc.ircm.lana.user.User;
import com.github.mongobee.Mongobee;
import com.mongodb.MongoClient;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Mongobee configuration.
 */
@Configuration
public class MongobeeConfiguration {
  private static final String SCAN_PACKAGE = MongobeeConfiguration.class.getPackage().getName();
  private static final String[] TEST_PROFILES = new String[] { "test", "integration-test" };
  private static final Logger logger = LoggerFactory.getLogger(MongobeeConfiguration.class);
  @Inject
  private MongoClient mongoClient;
  @Inject
  private MongoTemplate mongoTemplate;
  @Inject
  private Environment environment;
  @Inject
  private AdminUserConfiguration adminUserConfiguration;
  @Value("${spring.data.mongodb.database}")
  private String database;
  private boolean enabled;

  @PostConstruct
  void init() {
    enabled = !runningUnitTests();
    if (enabled && !adminExists()) {
      if (adminUserConfiguration.getName() == null
          || adminUserConfiguration.getHashedPassword() == null) {
        logger.warn("Cannot initialize admin user, admin user properties must be defined");
        enabled = false;
      }
    }
  }

  /**
   * Returns Mongobee configuration bean.
   *
   * @return Mongobee configuration bean
   */
  @Bean
  public Mongobee mongobee() {
    Mongobee runner = new Mongobee(mongoClient);
    runner.setDbName(database);
    runner.setChangeLogsScanPackage(SCAN_PACKAGE);
    runner.setMongoTemplate(mongoTemplate);
    runner.setSpringEnvironment(environment);
    runner.setEnabled(enabled);
    return runner;
  }

  private boolean adminExists() {
    return mongoTemplate.findById(AdminUserConfiguration.ADMIN_ID, User.class) != null;
  }

  private boolean runningUnitTests() {
    Set<String> testProfiles = Stream.of(TEST_PROFILES).collect(Collectors.toSet());
    return Stream.of(environment.getActiveProfiles())
        .filter(profile -> testProfiles.contains(profile)).findAny().isPresent();
  }
}
