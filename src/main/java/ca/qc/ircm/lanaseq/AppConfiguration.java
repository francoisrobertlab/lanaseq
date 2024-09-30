package ca.qc.ircm.lanaseq;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.User;
import jakarta.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application's configuration.
 */
@ConfigurationProperties(prefix = AppConfiguration.PREFIX)
public class AppConfiguration {
  /**
   * Default name of application.
   */
  public static final String APPLICATION_NAME = "lanaseq";
  /**
   * Prefix for all application properties inside configuration file.
   */
  public static final String PREFIX = "app";
  /**
   * Filename containing deleted file metadata.
   */
  public static final String DELETED_FILENAME = ".deleted";
  /**
   * Year formatter.
   */
  private static DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");
  /**
   * Log file.
   */
  @Value("${logging.path:${user.dir}}/${logging.file.name:" + APPLICATION_NAME + "log}")
  private String logfile;
  /**
   * Application home folder.
   */
  private NetworkDrive<DataWithFiles> home;
  /**
   * Archive folders, if any.
   */
  private List<NetworkDrive<DataWithFiles>> archives;
  /**
   * Where sample files are stored.
   */
  private Path sampleFolder;
  /**
   * Where dataset files are stored.
   */
  private Path datasetFolder;
  /**
   * Analysis network drive.
   */
  private NetworkDrive<Collection<? extends DataWithFiles>> analysis;
  /**
   * Use symbolic links for analysis instead of copying files.
   */
  private boolean analysisSymlinks;
  /**
   * Time that must elapse before an analysis folder get deleted.
   */
  private Duration analysisDeleteAge;
  /**
   * Upload network drive.
   */
  private NetworkDrive<DataWithFiles> upload;
  /**
   * Time that must elapse before an upload folder get deleted.
   */
  private Duration uploadDeleteAge;
  /**
   * Server's actual URL, used in emails.
   */
  private String serverUrl;
  private final AuthenticatedUser authenticatedUser;
  /**
   * Location of files inside home folder.
   */
  private Function<DataWithFiles, Path> subfolder = df -> {
    Path base = df instanceof Dataset ? datasetFolder : sampleFolder;
    return base.resolve(year.format(df.getDate())).resolve(df.getName());
  };
  /**
   * Location of upload files.
   */
  private Function<DataWithFiles, Path> uploadSubfolder = df -> Paths.get(df.getName());
  /**
   * Location of analysis files.
   */
  private Function<Collection<? extends DataWithFiles>, Path> analysisSubfolder = collection -> {
    if (collection.isEmpty()) {
      throw new IllegalArgumentException("collection cannot be empty");
    } else if (collection.stream().findAny().get() instanceof Dataset) {
      return datasetAnalysisSubfolder((Collection<Dataset>) collection);
    } else {
      return sampleAnalysisSubfolder((Collection<Sample>) collection);
    }
  };

  @Autowired
  protected AppConfiguration(AuthenticatedUser authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
  }

  /**
   * Initializes instances of NetworkDrive.
   */
  @PostConstruct
  void initNetworkDrives() {
    if (archives == null) {
      archives = new ArrayList<>();
    }
    home.subfolder = subfolder;
    archives.forEach(archive -> archive.subfolder = subfolder);
    analysis.subfolder = analysisSubfolder;
    upload.subfolder = uploadSubfolder;
  }

  public Path getLogFile() {
    return Paths.get(logfile);
  }

  private Path resolveHome(Path subfolder) {
    return home.folder.resolve(subfolder);
  }

  private Path datasetAnalysisSubfolder(Collection<Dataset> datasets) {
    if (datasets == null || datasets.isEmpty()) {
      throw new IllegalArgumentException("datasets cannot be null or empty");
    }
    if (datasets.size() == 1) {
      return Paths.get(datasets.stream().findFirst().map(Dataset::getName).get());
    } else {
      Optional<User> user = authenticatedUser.getUser();
      Dataset dataset = datasets.iterator().next();
      Optional<Sample> sample =
          datasets.stream().flatMap(ds -> ds.getSamples().stream()).findFirst();
      StringBuilder builder = new StringBuilder();
      user.map(User::getEmail).map(email -> Pattern.compile("(\\w+)").matcher(email))
          .filter(match -> match.find()).ifPresent(match -> builder.append("_" + match.group(1)));
      sample.map(sa -> sa.getAssay())
          .ifPresent(assay -> builder.append("_" + assay.replaceAll("[^\\w]", "")));
      builder.append("_");
      builder.append(DateTimeFormatter.ofPattern("yyyyMMdd").format(dataset.getDate()));
      if (builder.length() > 0) {
        builder.deleteCharAt(0);
      }
      return Paths.get(builder.toString());
    }
  }

  private Path sampleAnalysisSubfolder(Collection<Sample> samples) {
    if (samples == null || samples.isEmpty()) {
      throw new IllegalArgumentException("samples cannot be null or empty");
    }
    if (samples.size() == 1) {
      return Paths.get(samples.stream().findFirst().map(Sample::getName).get());
    } else {
      Optional<User> user = authenticatedUser.getUser();
      Sample sample = samples.iterator().next();
      StringBuilder builder = new StringBuilder();
      user.map(User::getEmail).map(email -> Pattern.compile("(\\w+)").matcher(email))
          .filter(match -> match.find()).ifPresent(match -> builder.append("_" + match.group(1)));
      builder.append(
          sample.getAssay() != null ? "_" + sample.getAssay().replaceAll("[^\\w]", "") : "");
      builder.append("_");
      builder.append(DateTimeFormatter.ofPattern("yyyyMMdd").format(sample.getDate()));
      if (builder.length() > 0) {
        builder.deleteCharAt(0);
      }
      return Paths.get(builder.toString());
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

  public NetworkDrive<DataWithFiles> getHome() {
    return home;
  }

  void setHome(NetworkDrive<DataWithFiles> home) {
    this.home = home;
  }

  public List<NetworkDrive<DataWithFiles>> getArchives() {
    return archives;
  }

  void setArchives(List<NetworkDrive<DataWithFiles>> archives) {
    this.archives = archives;
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

  public NetworkDrive<DataWithFiles> getUpload() {
    return upload;
  }

  public void setUpload(NetworkDrive<DataWithFiles> upload) {
    this.upload = upload;
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

  public NetworkDrive<Collection<? extends DataWithFiles>> getAnalysis() {
    return analysis;
  }

  public void setAnalysis(NetworkDrive<Collection<? extends DataWithFiles>> analysis) {
    this.analysis = analysis;
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

  /**
   * Folder that can be on a network drive.
   */
  public static class NetworkDrive<DF> {
    private Path folder;
    private String windowsLabel;
    private String unixLabel;
    private Function<DF, Path> subfolder;

    /**
     * Returns data's files folder.
     * 
     * @param df
     *          data with files
     * @return data's files folder
     */
    public Path folder(DF df) {
      return folder.resolve(subfolder.apply(df));
    }

    /**
     * Returns label to be shown to user, so he can find the data's files folder on the network.
     *
     * @param df
     *          data with files
     * @param unix
     *          true if path elements should be separated by slashes instead of backslashes
     * @return label to be shown to user, so he can find the data's files folder on the network
     */
    public String label(DF df, boolean unix) {
      Path subfolder = this.subfolder.apply(df);
      if (unix) {
        return FilenameUtils.separatorsToUnix(unixLabel + "/" + subfolder.toString());
      } else {
        return FilenameUtils.separatorsToWindows(windowsLabel + "/" + subfolder.toString());
      }
    }

    public Path getFolder() {
      return folder;
    }

    void setFolder(Path folder) {
      this.folder = folder;
    }

    String getWindowsLabel() {
      return windowsLabel;
    }

    void setWindowsLabel(String windowsLabel) {
      this.windowsLabel = windowsLabel;
    }

    String getUnixLabel() {
      return unixLabel;
    }

    void setUnixLabel(String unixLabel) {
      this.unixLabel = unixLabel;
    }
  }
}
