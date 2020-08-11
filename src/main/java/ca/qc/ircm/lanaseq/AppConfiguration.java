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
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import org.apache.commons.io.FilenameUtils;
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
  public static final String APPLICATION_NAME = "lanaseq";
  public static final String PREFIX = "app";
  public static final String DELETED_FILENAME = ".deleted";
  @Value("${logging.path:${user.dir}}/${logging.file:" + APPLICATION_NAME + "log}")
  private String logfile;
  /**
   * Application home folder.
   */
  private Path home;
  /**
   * Where sample files are stored.
   */
  private Path sampleHome;
  /**
   * Where dataset files are stored.
   */
  private Path datasetHome;
  /**
   * Analysis folder.
   */
  private Path analysis;
  /**
   * Use symbolic links for analysis instead of copying files.
   */
  private boolean analysisSymlinks;
  /**
   * Upload folder.
   */
  private Path upload;
  /**
   * Time that must elapse before a folder get deleted.
   */
  private Duration uploadDeleteAge;
  /**
   * Application home folder as shown to users.
   */
  private Folder userHome;
  /**
   * Application upload folder as shown to users.
   */
  private Folder userUpload;
  /**
   * Server's actual URL, used in emails.
   */
  private String serverUrl;
  /**
   * Year formatter.
   */
  private DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");

  public Path getLogFile() {
    return Paths.get(logfile);
  }

  public Path folder(Sample sample) {
    return sampleHome.resolve(year.format(sample.getCreationDate())).resolve(sample.getName());
  }

  public Path folder(Dataset dataset) {
    return datasetHome.resolve(year.format(dataset.getCreationDate())).resolve(dataset.getName());
  }

  public String folderLabel(Sample sample, boolean unix) {
    Path relative = home.relativize(folder(sample));
    if (unix) {
      return FilenameUtils.separatorsToUnix(userHome.unix + "/" + relative.toString());
    } else {
      return FilenameUtils.separatorsToWindows(userHome.windows + "/" + relative.toString());
    }
  }

  public String folderLabel(Dataset dataset, boolean unix) {
    Path relative = home.relativize(folder(dataset));
    if (unix) {
      return FilenameUtils.separatorsToUnix(userHome.unix + "/" + relative.toString());
    } else {
      return FilenameUtils.separatorsToWindows(userHome.windows + "/" + relative.toString());
    }
  }

  public String folderNetwork(boolean unix) {
    return unix ? userHome.network.unix : userHome.network.windows;
  }

  public Path analysis(Dataset dataset) {
    return getAnalysis().resolve(dataset.getName());
  }

  public String analysisLabel(Dataset dataset, boolean unix) {
    Path relative = home.relativize(analysis(dataset));
    if (unix) {
      return FilenameUtils.separatorsToUnix(userHome.unix + "/" + relative.toString());
    } else {
      return FilenameUtils.separatorsToWindows(userHome.windows + "/" + relative.toString());
    }
  }

  public Path upload(Sample sample) {
    return getUpload().resolve(sample.getName());
  }

  public Path upload(Dataset dataset) {
    return getUpload().resolve(dataset.getName());
  }

  public String uploadLabel(Sample sample, boolean unix) {
    if (unix) {
      return FilenameUtils.separatorsToUnix(userUpload.unix + "/" + sample.getName());
    } else {
      return FilenameUtils.separatorsToWindows(userUpload.windows + "/" + sample.getName());
    }
  }

  public String uploadLabel(Dataset dataset, boolean unix) {
    if (unix) {
      return FilenameUtils.separatorsToUnix(userUpload.unix + "/" + dataset.getName());
    } else {
      return FilenameUtils.separatorsToWindows(userUpload.windows + "/" + dataset.getName());
    }
  }

  public String uploadNetwork(boolean unix) {
    return unix ? userUpload.network.unix : userUpload.network.windows;
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

  Path getSampleHome() {
    return sampleHome;
  }

  void setSampleHome(Path sampleHome) {
    this.sampleHome = sampleHome;
  }

  Path getDatasetHome() {
    return datasetHome;
  }

  void setDatasetHome(Path datasetHome) {
    this.datasetHome = datasetHome;
  }

  public Path getUpload() {
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

  Folder getUserHome() {
    return userHome;
  }

  void setUserHome(Folder userHome) {
    this.userHome = userHome;
  }

  Folder getUserUpload() {
    return userUpload;
  }

  void setUserUpload(Folder userUpload) {
    this.userUpload = userUpload;
  }

  public Duration getUploadDeleteAge() {
    return uploadDeleteAge;
  }

  void setUploadDeleteAge(Duration uploadDeleteAge) {
    this.uploadDeleteAge = uploadDeleteAge;
  }

  Path getAnalysis() {
    return analysis;
  }

  void setAnalysis(Path analysis) {
    this.analysis = analysis;
  }

  public boolean isAnalysisSymlinks() {
    return analysisSymlinks;
  }

  void setAnalysisSymlinks(boolean analysisSymlinks) {
    this.analysisSymlinks = analysisSymlinks;
  }

  @SuppressWarnings("unused")
  private static class Folder {
    private String windows;
    private String unix;
    private Network network;

    String getWindows() {
      return windows;
    }

    void setWindows(String windows) {
      this.windows = windows;
    }

    String getUnix() {
      return unix;
    }

    void setUnix(String unix) {
      this.unix = unix;
    }

    Network getNetwork() {
      return network;
    }

    void setNetwork(Network network) {
      this.network = network;
    }
  }

  @SuppressWarnings("unused")
  private static class Network {
    private String windows;
    private String unix;

    String getWindows() {
      return windows;
    }

    void setWindows(String windows) {
      this.windows = windows;
    }

    String getUnix() {
      return unix;
    }

    void setUnix(String unix) {
      this.unix = unix;
    }
  }
}
