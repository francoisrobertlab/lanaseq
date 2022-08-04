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
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.User;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
  @Value("${logging.path:${user.dir}}/${logging.file.name:" + APPLICATION_NAME + "log}")
  private String logfile;
  /**
   * Application home folder.
   */
  private NetworkDrive home;
  /**
   * Where sample files are stored.
   */
  private Path sampleFolder;
  /**
   * Where dataset files are stored.
   */
  private Path datasetFolder;
  /**
   * Analysis folder.
   */
  private Path analysisFolder;
  /**
   * Use symbolic links for analysis instead of copying files.
   */
  private boolean analysisSymlinks;
  /**
   * Time that must elapse before an analysis folder get deleted.
   */
  private Duration analysisDeleteAge;
  /**
   * Upload folder.
   */
  private Path uploadFolder;
  /**
   * Time that must elapse before an upload folder get deleted.
   */
  private Duration uploadDeleteAge;
  /**
   * Server's actual URL, used in emails.
   */
  private String serverUrl;
  /**
   * Year formatter.
   */
  private DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");
  @Autowired
  private AuthorizationService authorizationService;

  public Path getLogFile() {
    return Paths.get(logfile);
  }

  private Path resolveHome(Path subfolder) {
    return home.folder.resolve(subfolder);
  }

  public Path folder(Sample sample) {
    return resolveHome(sampleFolder).resolve(year.format(sample.getCreationDate()))
        .resolve(sample.getName());
  }

  public Path folder(Dataset dataset) {
    return resolveHome(datasetFolder).resolve(year.format(dataset.getCreationDate()))
        .resolve(dataset.getName());
  }

  /**
   * Returns label to be shown to user, so he can find the sample's folder on the network.
   *
   * @param sample
   *          sample
   * @param unix
   *          true if path elements should be separated by slashes instead of backslashes
   * @return label to be shown to user, so he can find the sample's folder on the network
   */
  public String folderLabel(Sample sample, boolean unix) {
    Path relative = home.folder.relativize(folder(sample));
    if (unix) {
      return FilenameUtils.separatorsToUnix(home.unixPath + "/" + relative.toString());
    } else {
      return FilenameUtils.separatorsToWindows(home.windowsPath + "/" + relative.toString());
    }
  }

  /**
   * Returns label to be shown to user, so he can find the dataset's folder on the network.
   *
   * @param dataset
   *          dataset
   * @param unix
   *          true if path elements should be separated by slashes instead of backslashes
   * @return label to be shown to user, so he can find the dataset's folder on the network
   */
  public String folderLabel(Dataset dataset, boolean unix) {
    Path relative = home.folder.relativize(folder(dataset));
    if (unix) {
      return FilenameUtils.separatorsToUnix(home.unixPath + "/" + relative.toString());
    } else {
      return FilenameUtils.separatorsToWindows(home.windowsPath + "/" + relative.toString());
    }
  }

  public Path analysis() {
    return resolveHome(analysisFolder);
  }

  public Path datasetAnalysis(Collection<Dataset> datasets) {
    if (datasets == null || datasets.isEmpty()) {
      throw new IllegalArgumentException("datasets cannot be null or empty");
    }
    if (datasets.size() == 1) {
      return resolveHome(analysisFolder)
          .resolve(datasets.stream().findFirst().map(Dataset::getName).get());
    } else {
      Optional<User> user = authorizationService.getCurrentUser();
      Dataset dataset = datasets.iterator().next();
      Optional<Sample> sample =
          datasets.stream().flatMap(ds -> ds.getSamples().stream()).findFirst();
      StringBuilder builder = new StringBuilder();
      user.map(User::getEmail).map(email -> Pattern.compile("(\\w+)").matcher(email))
          .filter(match -> match.find()).ifPresent(match -> builder.append("_" + match.group(1)));
      sample.map(sa -> sa.getAssay()).ifPresent(assay -> builder.append("_" + assay));
      builder.append("_");
      builder.append(DateTimeFormatter.ofPattern("yyyyMMdd").format(dataset.getDate()));
      if (builder.length() > 0) {
        builder.deleteCharAt(0);
      }
      return resolveHome(analysisFolder).resolve(builder.toString());
    }
  }

  public Path sampleAnalysis(Collection<Sample> samples) {
    if (samples == null || samples.isEmpty()) {
      throw new IllegalArgumentException("samples cannot be null or empty");
    }
    if (samples.size() == 1) {
      return resolveHome(analysisFolder)
          .resolve(samples.stream().findFirst().map(Sample::getName).get());
    } else {
      Optional<User> user = authorizationService.getCurrentUser();
      Sample sample = samples.iterator().next();
      StringBuilder builder = new StringBuilder();
      user.map(User::getEmail).map(email -> Pattern.compile("(\\w+)").matcher(email))
          .filter(match -> match.find()).ifPresent(match -> builder.append("_" + match.group(1)));
      builder.append(sample.getAssay() != null ? "_" + sample.getAssay() : "");
      builder.append("_");
      builder.append(DateTimeFormatter.ofPattern("yyyyMMdd").format(sample.getDate()));
      if (builder.length() > 0) {
        builder.deleteCharAt(0);
      }
      return resolveHome(analysisFolder).resolve(builder.toString());
    }
  }

  /**
   * Returns label to be shown to user, so he can find the dataset's analysis folder on the network.
   *
   * @param datasets
   *          datasets
   * @param unix
   *          true if path elements should be separated by slashes instead of backslashes
   * @return label to be shown to user, so he can find the dataset's analysis folder on the network
   */
  public String datasetAnalysisLabel(Collection<Dataset> datasets, boolean unix) {
    Path relative = home.folder.relativize(datasetAnalysis(datasets));
    if (unix) {
      return FilenameUtils.separatorsToUnix(home.unixPath + "/" + relative.toString());
    } else {
      return FilenameUtils.separatorsToWindows(home.windowsPath + "/" + relative.toString());
    }
  }

  /**
   * Returns label to be shown to user, so he can find the dataset's analysis folder on the network.
   *
   * @param samples
   *          samples
   * @param unix
   *          true if path elements should be separated by slashes instead of backslashes
   * @return label to be shown to user, so he can find the dataset's analysis folder on the network
   */
  public String sampleAnalysisLabel(Collection<Sample> samples, boolean unix) {
    Path relative = home.folder.relativize(sampleAnalysis(samples));
    if (unix) {
      return FilenameUtils.separatorsToUnix(home.unixPath + "/" + relative.toString());
    } else {
      return FilenameUtils.separatorsToWindows(home.windowsPath + "/" + relative.toString());
    }
  }

  public Path upload() {
    return resolveHome(uploadFolder);
  }

  public Path upload(Sample sample) {
    return resolveHome(uploadFolder).resolve(sample.getName());
  }

  public Path upload(Dataset dataset) {
    return resolveHome(uploadFolder).resolve(dataset.getName());
  }

  /**
   * Returns label to be shown to user so he can find the sample's upload folder on the network.
   *
   * @param sample
   *          sample
   * @param unix
   *          true if path elements should be separated by slashes instead of backslashes
   * @return label to be shown to user so he can find the sample's upload folder on the network
   */
  public String uploadLabel(Sample sample, boolean unix) {
    Path relative = home.folder.relativize(upload(sample));
    if (unix) {
      return FilenameUtils.separatorsToUnix(home.unixPath + "/" + relative.toString());
    } else {
      return FilenameUtils.separatorsToWindows(home.windowsPath + "/" + relative.toString());
    }
  }

  /**
   * Returns label to be shown to user so he can find the dataset's upload folder on the network.
   *
   * @param dataset
   *          dataset
   * @param unix
   *          true if path elements should be separated by slashes instead of backslashes
   * @return label to be shown to user so he can find the dataset's upload folder on the network
   */
  public String uploadLabel(Dataset dataset, boolean unix) {
    Path relative = home.folder.relativize(upload(dataset));
    if (unix) {
      return FilenameUtils.separatorsToUnix(home.unixPath + "/" + relative.toString());
    } else {
      return FilenameUtils.separatorsToWindows(home.windowsPath + "/" + relative.toString());
    }
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

  NetworkDrive getHome() {
    return home;
  }

  void setHome(NetworkDrive home) {
    this.home = home;
  }

  Path getSampleFolder() {
    return sampleFolder;
  }

  void setSampleFolder(Path sampleFolder) {
    this.sampleFolder = sampleFolder;
  }

  Path getDatasetFolder() {
    return datasetFolder;
  }

  void setDatasetFolder(Path datasetFolder) {
    this.datasetFolder = datasetFolder;
  }

  Path getUploadFolder() {
    return uploadFolder;
  }

  void setUploadFolder(Path uploadFolder) {
    this.uploadFolder = uploadFolder;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  public Duration getUploadDeleteAge() {
    return uploadDeleteAge;
  }

  void setUploadDeleteAge(Duration uploadDeleteAge) {
    this.uploadDeleteAge = uploadDeleteAge;
  }

  Path getAnalysisFolder() {
    return analysisFolder;
  }

  void setAnalysisFolder(Path analysisFolder) {
    this.analysisFolder = analysisFolder;
  }

  public boolean isAnalysisSymlinks() {
    return analysisSymlinks;
  }

  void setAnalysisSymlinks(boolean analysisSymlinks) {
    this.analysisSymlinks = analysisSymlinks;
  }

  public Duration getAnalysisDeleteAge() {
    return analysisDeleteAge;
  }

  void setAnalysisDeleteAge(Duration analysisDeleteAge) {
    this.analysisDeleteAge = analysisDeleteAge;
  }

  public static class NetworkDrive {
    private Path folder;
    private String windowsPath;
    private String unixPath;

    public Path getFolder() {
      return folder;
    }

    public void setFolder(Path folder) {
      this.folder = folder;
    }

    public String getWindowsPath() {
      return windowsPath;
    }

    public void setWindowsPath(String windowsPath) {
      this.windowsPath = windowsPath;
    }

    public String getUnixPath() {
      return unixPath;
    }

    public void setUnixPath(String unixPath) {
      this.unixPath = unixPath;
    }
  }
}
