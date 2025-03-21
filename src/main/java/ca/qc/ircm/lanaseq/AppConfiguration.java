package ca.qc.ircm.lanaseq;

import static ca.qc.ircm.lanaseq.UsedBy.SPRING;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.User;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application's configuration.
 */
@ConfigurationProperties(prefix = AppConfiguration.PREFIX)
public class AppConfiguration implements InitializingBean {

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
  private static final DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");
  private final AuthenticatedUser authenticatedUser;
  /**
   * Log file.
   */
  @Value("${logging.path:${user.dir}}/${logging.file.name:" + APPLICATION_NAME + "log}")
  private String logfile;
  /**
   * Application home folder.
   */
  private NetworkDrive<DataWithFiles> home = new NetworkDrive<>();
  /**
   * Archive folders, if any.
   */
  private List<NetworkDrive<DataWithFiles>> archives = new ArrayList<>();
  /**
   * Where sample files are stored.
   */
  private Path sampleFolder;
  /**
   * Where dataset files are stored.
   */
  private Path datasetFolder;
  /**
   * Location of files inside home folder.
   */
  private final Function<DataWithFiles, Path> subfolder = df -> {
    Path base = df instanceof Dataset ? datasetFolder : sampleFolder;
    return base.resolve(year.format(df.getDate())).resolve(df.getName());
  };
  /**
   * Analysis network drive.
   */
  private NetworkDrive<Collection<? extends DataWithFiles>> analysis = new NetworkDrive<>();
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
  private NetworkDrive<DataWithFiles> upload = new NetworkDrive<>();
  /**
   * Time that must elapse before an upload folder get deleted.
   */
  private Duration uploadDeleteAge;
  /**
   * Server's actual URL, used in emails.
   */
  private String serverUrl;
  /**
   * Context path, used with {@link #serverUrl}.
   */
  private String contextPath;
  /**
   * Time during which a public file link is valid.
   */
  private Period publicFilePeriod;

  @Autowired
  @UsedBy(SPRING)
  protected AppConfiguration(AuthenticatedUser authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
    home.subfolder = subfolder;
    analysis.subfolder = this::analysisSubfolder;
    upload.subfolder = df -> Paths.get(df.getName());
  }

  @Override
  public void afterPropertiesSet() {
    archives.forEach(archive -> archive.subfolder = subfolder);
  }

  public Path getLogFile() {
    return Paths.get(logfile);
  }

  private Path analysisSubfolder(Collection<? extends DataWithFiles> datas) {
    Objects.requireNonNull(datas, "datas parameter cannot be null");
    if (datas.isEmpty()) {
      throw new IllegalArgumentException("datas parameter cannot be empty");
    }
    DataWithFiles data = datas.iterator().next();
    Objects.requireNonNull(data);
    if (datas.size() == 1) {
      return Paths.get(data.getName());
    } else {
      Optional<Sample> sample = Optional.ofNullable(
          data instanceof Dataset ? ((Dataset) data).getSamples().stream().findFirst().orElse(null)
              : (Sample) data);
      StringBuilder builder = new StringBuilder();
      Optional<User> user = authenticatedUser.getUser();
      user.map(User::getEmail).map(email -> Pattern.compile("(\\w+)").matcher(email))
          .filter(Matcher::find).ifPresent(match -> builder.append("_").append(match.group(1)));
      sample.map(Sample::getAssay)
          .ifPresent(assay -> builder.append("_").append(assay.replaceAll("\\W", "")));
      builder.append("_");
      builder.append(DateTimeFormatter.ofPattern("yyyyMMdd").format(data.getDate()));
      if (!builder.isEmpty()) {
        builder.deleteCharAt(0);
      }
      return Paths.get(builder.toString());
    }
  }

  /**
   * Returns urlEnd with prefix that allows to access application from anywhere.
   *
   * <p>For example, to obtain the full URL <code><a href=
   * "http://myserver.com/lanaseq/myurl?param1=abc">
   * http://myserver.com/lanaseq/myurl?param1=abc</a></code> , the urlEnd parameter should be
   * <code>/lanaseq/myurl?param1=abc</code>
   * </p>
   *
   * @param urlEnd end portion of URL
   * @return urlEnd with prefix that allows to access application from anywhere
   */
  public String getUrl(String urlEnd) {
    return serverUrl + contextPath + "/" + urlEnd;
  }

  public NetworkDrive<DataWithFiles> getHome() {
    return home;
  }

  @UsedBy(SPRING)
  void setHome(NetworkDrive<DataWithFiles> home) {
    this.home = home;
  }

  public List<NetworkDrive<DataWithFiles>> getArchives() {
    return archives;
  }

  @UsedBy(SPRING)
  void setArchives(List<NetworkDrive<DataWithFiles>> archives) {
    this.archives = archives;
  }

  Path getSampleFolder() {
    return sampleFolder;
  }

  @UsedBy(SPRING)
  void setSampleFolder(Path sampleFolder) {
    this.sampleFolder = sampleFolder;
  }

  Path getDatasetFolder() {
    return datasetFolder;
  }

  @UsedBy(SPRING)
  void setDatasetFolder(Path datasetFolder) {
    this.datasetFolder = datasetFolder;
  }

  public NetworkDrive<DataWithFiles> getUpload() {
    return upload;
  }

  @UsedBy(SPRING)
  public void setUpload(NetworkDrive<DataWithFiles> upload) {
    this.upload = upload;
  }

  @UsedBy(SPRING)
  void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  @Value("${server.servlet.context-path:}")
  @UsedBy(SPRING)
  void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  public Duration getUploadDeleteAge() {
    return uploadDeleteAge;
  }

  @UsedBy(SPRING)
  void setUploadDeleteAge(Duration uploadDeleteAge) {
    this.uploadDeleteAge = uploadDeleteAge;
  }

  public NetworkDrive<Collection<? extends DataWithFiles>> getAnalysis() {
    return analysis;
  }

  @UsedBy(SPRING)
  public void setAnalysis(NetworkDrive<Collection<? extends DataWithFiles>> analysis) {
    this.analysis = analysis;
  }

  public boolean isAnalysisSymlinks() {
    return analysisSymlinks;
  }

  @UsedBy(SPRING)
  void setAnalysisSymlinks(boolean analysisSymlinks) {
    this.analysisSymlinks = analysisSymlinks;
  }

  public Duration getAnalysisDeleteAge() {
    return analysisDeleteAge;
  }

  @UsedBy(SPRING)
  void setAnalysisDeleteAge(Duration analysisDeleteAge) {
    this.analysisDeleteAge = analysisDeleteAge;
  }

  public Period getPublicFilePeriod() {
    return publicFilePeriod;
  }

  @UsedBy(SPRING)
  void setPublicFilePeriod(Period publicFilePeriod) {
    this.publicFilePeriod = publicFilePeriod;
  }

  /**
   * Folder that can be on a network drive.
   */
  public static class NetworkDrive<D> {

    private Path folder;
    private String windowsLabel;
    private String unixLabel;
    private Function<D, Path> subfolder;

    /**
     * Returns data's files folder.
     *
     * @param dataWithFiles data with files
     * @return data's files folder
     */
    public Path folder(D dataWithFiles) {
      return folder.resolve(subfolder.apply(dataWithFiles));
    }

    /**
     * Returns label to be shown to user, so he can find the data's files folder on the network.
     *
     * @param dataWithFiles data with files
     * @param unix          true if path elements should be separated by slashes instead of
     *                      backslashes
     * @return label to be shown to user, so he can find the data's files folder on the network
     */
    public String label(D dataWithFiles, boolean unix) {
      Path subfolder = this.subfolder.apply(dataWithFiles);
      if (unix) {
        return FilenameUtils.separatorsToUnix(unixLabel + "/" + subfolder.toString());
      } else {
        return FilenameUtils.separatorsToWindows(windowsLabel + "/" + subfolder.toString());
      }
    }

    public Path getFolder() {
      return folder;
    }

    @UsedBy(SPRING)
    void setFolder(Path folder) {
      this.folder = folder;
    }

    String getWindowsLabel() {
      return windowsLabel;
    }

    @UsedBy(SPRING)
    void setWindowsLabel(String windowsLabel) {
      this.windowsLabel = windowsLabel;
    }

    String getUnixLabel() {
      return unixLabel;
    }

    @UsedBy(SPRING)
    void setUnixLabel(String unixLabel) {
      this.unixLabel = unixLabel;
    }
  }
}
