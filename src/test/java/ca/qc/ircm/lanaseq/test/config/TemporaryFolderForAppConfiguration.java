package ca.qc.ircm.lanaseq.test.config;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppConfiguration.NetworkDrive;
import ca.qc.ircm.lanaseq.DataWithFiles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.mockito.Mockito;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Change folder in {@link AppConfiguration} to be located inside a temporary folder.
 */
public class TemporaryFolderForAppConfiguration implements TestExecutionListener {

  private Path savedHome;
  private List<NetworkDrive<DataWithFiles>> savedArchives;
  private Path savedArchive;
  private Path savedArchive2;
  private Path savedAnalysis;
  private Path savedUpload;

  @Override
  public void beforeTestMethod(TestContext testContext) throws Exception {
    if (changeFolders(testContext)) {
      AppConfiguration configuration = testContext.getApplicationContext()
          .getBean(AppConfiguration.class);
      savedHome = configuration.getHome().getFolder();
      savedArchives = new ArrayList<>(configuration.getArchives());
      savedArchive = configuration.getArchives().get(0).getFolder();
      savedArchive2 = configuration.getArchives().get(1).getFolder();
      savedAnalysis = configuration.getAnalysis().getFolder();
      savedUpload = configuration.getUpload().getFolder();
      Path home = Files.createTempDirectory("lanaseq-test-");
      Path archive = home.resolve("archive");
      Path archive2 = home.resolve("archive2");
      Path analysis = home.resolve("analysis");
      Path upload = home.resolve("upload");
      setHome(configuration, home);
      setArchive(configuration, archive, archive2);
      setAnalysis(configuration, analysis);
      setUpload(configuration, upload);
    }
  }

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    if (changeFolders(testContext)) {
      AppConfiguration configuration = testContext.getApplicationContext()
          .getBean(AppConfiguration.class);
      setHome(configuration, savedHome);
      setArchive(configuration, savedArchive, savedArchive2);
      setAnalysis(configuration, savedAnalysis);
      setUpload(configuration, savedUpload);
    }
  }

  private boolean changeFolders(TestContext testContext) {
    AppConfiguration configuration = testContext.getApplicationContext()
        .getBean(AppConfiguration.class);
    Optional<KeepFoldersForAppConfiguration> keepFolders = AnnotationFinder.findAnnotation(
        testContext.getTestClass(), testContext.getTestMethod(),
        KeepFoldersForAppConfiguration.class);
    return !Mockito.mockingDetails(configuration).isMock() && keepFolders.isEmpty();
  }

  protected void setHome(AppConfiguration configuration, Path home)
      throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method setFolder = NetworkDrive.class.getDeclaredMethod("setFolder", Path.class);
    setFolder.setAccessible(true);
    NetworkDrive<DataWithFiles> homeDrive = configuration.getHome();
    setFolder.invoke(homeDrive, home);
  }

  protected void setArchive(AppConfiguration configuration, Path archive, Path archive2)
      throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    configuration.getArchives().clear();
    configuration.getArchives().addAll(savedArchives);
    Method setFolder = NetworkDrive.class.getDeclaredMethod("setFolder", Path.class);
    setFolder.setAccessible(true);
    setFolder.invoke(configuration.getArchives().get(0), archive);
    setFolder.invoke(configuration.getArchives().get(1), archive2);
  }

  protected void setAnalysis(AppConfiguration configuration, Path analysis)
      throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method setFolder = NetworkDrive.class.getDeclaredMethod("setFolder", Path.class);
    setFolder.setAccessible(true);
    NetworkDrive<Collection<? extends DataWithFiles>> analysisDrive = configuration.getAnalysis();
    setFolder.invoke(analysisDrive, analysis);
  }

  protected void setUpload(AppConfiguration configuration, Path upload)
      throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method setFolder = NetworkDrive.class.getDeclaredMethod("setFolder", Path.class);
    setFolder.setAccessible(true);
    NetworkDrive<DataWithFiles> uploadDrive = configuration.getUpload();
    setFolder.invoke(uploadDrive, upload);
  }
}
