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

package ca.qc.ircm.lanaseq;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.sample.Sample;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Application's configuration.
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = AppConfiguration.PREFIX)
public class AppConfiguration {
  public static final String APPLICATION_NAME = "lana";
  public static final String PREFIX = "app";
  @Value("${logging.path:${user.dir}}/${logging.file:" + APPLICATION_NAME + "log}")
  private String logfile;
  private Path home;
  private Path upload;
  private String serverUrl;
  private DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");

  public Path getLogFile() {
    return Paths.get(logfile);
  }

  public Path folder(Sample sample) {
    return getHome().resolve(year.format(sample.getDate())).resolve(sample.getName());
  }

  public Path upload(Sample sample) {
    return getUpload().resolve(sample.getName());
  }

  public Path folder(Dataset dataset) {
    return getHome().resolve(year.format(dataset.getDate())).resolve(dataset.getName());
  }

  public Path upload(Dataset dataset) {
    return getUpload().resolve(dataset.getName());
  }

  /**
   * Returns urlEnd with prefix that allows to access application from anywhere.
   * <p>
   * For example, to obtain the full URL <code>http://myserver.com/proview/myurl?param1=abc</code> ,
   * the urlEnd parameter should be <code>/lana/myurl?param1=abc</code>
   * </p>
   *
   * @param urlEnd
   *          end portion of URL
   * @return urlEnd with prefix that allows to access application from anywhere
   */
  public String getUrl(String urlEnd) {
    return serverUrl + urlEnd;
  }

  Path getHome() {
    return home;
  }

  void setHome(Path home) {
    this.home = home;
  }

  Path getUpload() {
    return upload;
  }

  void setUpload(Path upload) {
    this.upload = upload;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }
}
