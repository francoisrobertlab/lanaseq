package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.AppConfiguration.DELETED_FILENAME;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toLocalDateTime;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.DataWithFiles;
import ca.qc.ircm.lanaseq.files.Renamer;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

/**
 * Services for {@link Dataset}.
 */
@Service
@Transactional
public class DatasetService {

  private static final Logger logger = LoggerFactory.getLogger(DatasetService.class);
  private DatasetRepository repository;
  private DatasetPublicFileRepository datasetPublicFileRepository;
  private AppConfiguration configuration;
  private AuthenticatedUser authenticatedUser;
  private JPAQueryFactory queryFactory;

  protected DatasetService() {
  }

  @Autowired
  protected DatasetService(DatasetRepository repository,
      DatasetPublicFileRepository datasetPublicFileRepository, AppConfiguration configuration,
      AuthenticatedUser authenticatedUser, JPAQueryFactory queryFactory) {
    this.repository = repository;
    this.datasetPublicFileRepository = datasetPublicFileRepository;
    this.configuration = configuration;
    this.authenticatedUser = authenticatedUser;
    this.queryFactory = queryFactory;
  }

  /**
   * Returns dataset having specified id.
   *
   * @param id dataset's id
   * @return dataset having specified id
   */
  @PostAuthorize("!returnObject.isPresent() || hasPermission(returnObject.get(), 'read')")
  public Optional<Dataset> get(long id) {
    return repository.findById(id);
  }

  /**
   * Returns true if a dataset with specified name exists, false otherwise.
   *
   * @param name name
   * @return true if a dataset with specified name exists, false otherwise
   */
  public boolean exists(String name) {
    Objects.requireNonNull(name, "name parameter cannot be null");
    return repository.existsByName(name);
  }

  /**
   * Returns all datasets.
   *
   * @return all datasets
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Dataset> all() {
    return repository.findAll();
  }

  /**
   * Returns all datasets passing filter.
   *
   * @param filter   filter
   * @param pageable sorts and limits number of results
   * @return all datasets passing filter
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public Stream<Dataset> all(DatasetFilter filter, Pageable pageable) {
    Objects.requireNonNull(filter, "filter parameter cannot be null");
    return repository.findAll(filter.predicate(), pageable).stream();
  }

  /**
   * Returns number of datasets passing filter.
   *
   * @param filter filter
   * @return number of datasets passing filter
   */
  public long count(DatasetFilter filter) {
    Objects.requireNonNull(filter, "filter parameter cannot be null");
    return repository.count(filter.predicate());
  }

  /**
   * Returns all dataset's files.
   *
   * @param dataset dataset
   * @return all dataset's files
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public List<Path> files(Dataset dataset) {
    Objects.requireNonNull(dataset, "dataset parameter cannot be null");
    if (dataset.getId() == 0) {
      return new ArrayList<>();
    }
    List<Path> files = new ArrayList<>();
    Path folder = configuration.getHome().folder(dataset);
    try (Stream<Path> homeFiles = Files.list(folder)) {
      homeFiles.filter(file -> !DELETED_FILENAME.equals(file.getFileName().toString()))
          .filter(file -> !file.toFile().isHidden()).forEach(files::add);
    } catch (IOException e) {
      // Ignore since folder probably does not exist.
    }
    for (AppConfiguration.NetworkDrive<DataWithFiles> drive : configuration.getArchives()) {
      folder = drive.folder(dataset);
      try (Stream<Path> archiveFiles = Files.list(folder)) {
        archiveFiles.filter(file -> !DELETED_FILENAME.equals(file.getFileName().toString()))
            .filter(file -> !file.toFile().isHidden()).forEach(files::add);
      } catch (IOException e) {
        // Ignore since folder probably does not exist.
      }
    }
    if (!dataset.getFilenames().isEmpty()) {
      List<PathMatcher> matchers = dataset.getFilenames().stream()
          .map(filename -> FileSystems.getDefault().getPathMatcher("glob:**/*" + filename + "*"))
          .toList();
      try (Stream<Path> filenamesFiles = Files.walk(configuration.getHome().getFolder(),
          FileVisitOption.FOLLOW_LINKS)) {
        filenamesFiles.filter(Files::isRegularFile)
            .filter(file -> matchers.stream().anyMatch(matcher -> matcher.matches(file)))
            .filter(file -> !DELETED_FILENAME.equals(file.getFileName().toString()))
            .filter(file -> !file.toFile().isHidden()).forEach(files::add);
      } catch (IOException e) {
        // Ignore since folder probably does not exist.
      }
      for (AppConfiguration.NetworkDrive<DataWithFiles> drive : configuration.getArchives()) {
        try (Stream<Path> filenamesFiles = Files.walk(drive.getFolder(),
            FileVisitOption.FOLLOW_LINKS)) {
          filenamesFiles.filter(Files::isRegularFile)
              .filter(file -> matchers.stream().anyMatch(matcher -> matcher.matches(file)))
              .filter(file -> !DELETED_FILENAME.equals(file.getFileName().toString()))
              .filter(file -> !file.toFile().isHidden()).forEach(files::add);
        } catch (IOException e) {
          // Ignore since folder probably does not exist.
        }
      }
    }
    return files;
  }

  /**
   * Returns a path that is relative to a configured network drive.
   *
   * @param dataset dataset
   * @param path    path
   * @return path that is relative to a configured network drive
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public Path relativize(Dataset dataset, Path path) {
    return Stream.concat(Stream.of(configuration.getHome()), configuration.getArchives().stream())
        .map(drive -> drive.folder(dataset)).filter(path::startsWith)
        .map(folder -> folder.relativize(path)).findFirst()
        // No parent found in home or archives folders.
        .orElse(path);
  }

  /**
   * Returns all dataset's folder labels.
   * <p>
   * Only returns label for folders that exist.
   * </p>
   *
   * @param dataset dataset
   * @param unix    true if path elements should be separated by slashes instead of backslashes
   * @return all dataset's folder labels
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public List<String> folderLabels(Dataset dataset, boolean unix) {
    Objects.requireNonNull(dataset, "dataset parameter cannot be null");
    if (dataset.getId() == 0) {
      return new ArrayList<>();
    }
    List<String> labels = new ArrayList<>();
    Path folder = configuration.getHome().folder(dataset);
    if (Files.exists(folder)) {
      labels.add(configuration.getHome().label(dataset, unix));
    }
    for (AppConfiguration.NetworkDrive<DataWithFiles> drive : configuration.getArchives()) {
      folder = drive.folder(dataset);
      if (Files.exists(folder)) {
        labels.add(drive.label(dataset, unix));
      }
    }
    return labels;
  }

  /**
   * Returns all dataset's upload files.
   *
   * @param dataset dataset
   * @return all dataset's upload files
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public List<Path> uploadFiles(Dataset dataset) {
    Objects.requireNonNull(dataset, "dataset parameter cannot be null");
    if (dataset.getId() == 0) {
      return new ArrayList<>();
    }
    Path upload = configuration.getUpload().getFolder();
    Path datasetUpload = configuration.getUpload().folder(dataset);
    try {
      List<Path> files = new ArrayList<>();
      if (Files.exists(upload)) {
        try (Stream<Path> uploadFiles = Files.list(upload)) {
          uploadFiles.filter(file -> file.toFile().isFile()).filter(file -> {
            String filename = file.getFileName().toString();
            return filename.contains(dataset.getName());
          }).filter(file -> !file.toFile().isHidden()).forEach(files::add);
        }
      }
      if (Files.exists(datasetUpload)) {
        try (Stream<Path> uploadFiles = Files.list(datasetUpload)) {
          uploadFiles.filter(file -> file.toFile().isFile())
              .filter(file -> !file.toFile().isHidden()).forEach(files::add);
        }
      }
      return files;
    } catch (IOException e) {
      return new ArrayList<>();
    }
  }

  /**
   * Returns Dataset's file if it is accessible to the public.
   *
   * @param name     dataset's name
   * @param filename filename of dataset's file
   * @return Dataset's file if it is accessible to the public
   */
  @AnonymousAllowed
  public Optional<Path> publicFile(String name, String filename) {
    Objects.requireNonNull(name, "name parameter cannot be null");
    Objects.requireNonNull(filename, "filename parameter cannot be null");
    Optional<Dataset> optionalDataset = repository.findByName(name);
    if (optionalDataset.isEmpty() || !isFilePublic(optionalDataset.orElseThrow(),
        Paths.get(filename))) {
      return Optional.empty();
    }
    Dataset dataset = optionalDataset.orElseThrow();
    return Stream.concat(Stream.of(configuration.getHome()), configuration.getArchives().stream())
        .map(drive -> drive.folder(dataset).resolve(filename)).filter(Files::isRegularFile)
        .findFirst();
  }

  /**
   * Returns true if dataset's file is accessible to the public, false otherwise.
   *
   * @param dataset dataset
   * @param path    file
   * @return true if dataset's file is accessible to the public, false otherwise
   */
  @AnonymousAllowed
  public boolean isFilePublic(Dataset dataset, Path path) {
    path = relativize(dataset, path);
    Optional<DatasetPublicFile> optionalDatasetPublicFile = datasetPublicFileRepository.findByDatasetAndPath(
        dataset, path.toString());
    return optionalDatasetPublicFile.isPresent() && Range.leftUnbounded(
            Bound.inclusive(optionalDatasetPublicFile.orElseThrow().getExpiryDate()))
        .contains(LocalDate.now(), Comparator.naturalOrder());
  }

  /**
   * Returns all files accessible to the public.
   *
   * @return all files accessible to the public
   */
  @PreAuthorize("hasRole('USER')")
  public List<DatasetPublicFile> publicFiles() {
    return datasetPublicFileRepository.findByExpiryDateGreaterThanEqual(LocalDate.now());
  }

  /**
   * Returns most recent keywords.
   *
   * @param limit maximum number of keywords to return
   * @return most recent keywords
   */
  public List<String> topKeywords(int limit) {
    Set<String> keywords = new LinkedHashSet<>();
    int page = 0;
    while (keywords.size() < limit) {
      Page<Dataset> datasets = repository.findAllByOrderByIdDesc(PageRequest.of(page++, 50));
      if (datasets.isEmpty()) {
        break; // No more datasets.
      }
      keywords.addAll(datasets.flatMap(d -> d.getKeywords().stream()).toList());
    }
    return keywords.stream().limit(limit).collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Returns true if dataset can be deleted, false otherwise.
   *
   * @param dataset dataset
   * @return true if dataset can be deleted, false otherwise
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public boolean isDeletable(Dataset dataset) {
    Objects.requireNonNull(dataset, "dataset parameter cannot be null");
    return dataset.isEditable();
  }

  /**
   * Saves dataset into database.
   *
   * @param dataset dataset
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public void save(Dataset dataset) {
    Objects.requireNonNull(dataset, "dataset parameter cannot be null");
    Objects.requireNonNull(dataset.getName(), "dataset's name cannot be null");
    Objects.requireNonNull(dataset.getSamples(), "dataset's samples cannot be null");
    if (dataset.getId() != 0 && !dataset.isEditable()) {
      throw new IllegalArgumentException("dataset " + dataset + " cannot be edited");
    }
    if (dataset.getSamples().stream().anyMatch(sample -> sample.getId() == 0)) {
      throw new IllegalArgumentException("all dataset's samples must already be in database");
    }
    LocalDateTime now = LocalDateTime.now();
    User user = authenticatedUser.getUser().orElseThrow();
    if (dataset.getId() == 0) {
      dataset.setOwner(user);
      dataset.setCreationDate(now);
      dataset.setEditable(true);
    }
    Dataset old = old(dataset).orElse(null);
    if (old == null) {
      repository.save(dataset);
    } else {
      final String oldName = old.getName();
      Path oldFolder = configuration.getHome().folder(old);
      List<Path> oldArchives = configuration.getArchives().stream().map(drive -> drive.folder(old))
          .toList();
      repository.save(dataset);
      Path folder = configuration.getHome().folder(dataset);
      Renamer.moveFolder(oldFolder, folder);
      Renamer.renameFiles(oldName, dataset.getName(), folder);
      for (int i = 0; i < configuration.getArchives().size(); i++) {
        Path oldArchive = oldArchives.get(i);
        Path archive = configuration.getArchives().get(i).folder(dataset);
        Renamer.moveFolder(oldArchive, archive);
        Renamer.renameFiles(oldName, dataset.getName(), archive);
      }
    }
  }

  @Transactional(TxType.REQUIRES_NEW)
  protected Optional<Dataset> old(Dataset dataset) {
    if (dataset.getId() != 0) {
      return repository.findById(dataset.getId());
    } else {
      return Optional.empty();
    }
  }

  /**
   * Save files to dataset folder.
   *
   * @param dataset     dataset
   * @param files       files to save
   * @param filename    returns filename to use for file
   * @param progression progression of file saving
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  @Async
  public CompletableFuture<Void> saveFiles(Dataset dataset, Collection<Path> files,
      Function<Path, String> filename, BiConsumer<String, Double> progression) {
    Path folder = configuration.getHome().folder(dataset);
    try {
      Files.createDirectories(folder);
    } catch (IOException e) {
      throw new IllegalStateException("could not create folder " + folder, e);
    }
    int i = -1;
    for (Path file : files) {
      i++;
      String name = filename.apply(file);
      Path target = folder.resolve(name);
      try {
        logger.debug("moving file {} to {} for dataset {}", file, target, dataset);
        progression.accept(name, (double) i / files.size());
        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
        Files.delete(file);
      } catch (IOException e) {
        throw new IllegalArgumentException("could not move file " + file + " to " + target, e);
      }
    }
    progression.accept("", 1.0);
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Allows file to be accessible by anyone.
   *
   * @param dataset    file's dataset
   * @param file       file
   * @param expiryDate date when file stops being public
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public void allowPublicFileAccess(Dataset dataset, Path file, LocalDate expiryDate) {
    Path relative = relativize(dataset, file);
    DatasetPublicFile datasetPublicFile = datasetPublicFileRepository.findByDatasetAndPath(dataset,
        relative.toString()).orElseGet(() -> {
      DatasetPublicFile newDatasetPublicFile = new DatasetPublicFile();
      newDatasetPublicFile.setDataset(dataset);
      newDatasetPublicFile.setPath(relative.toString());
      return newDatasetPublicFile;
    });
    datasetPublicFile.setExpiryDate(expiryDate);
    datasetPublicFileRepository.save(datasetPublicFile);
  }

  /**
   * Revoke public access to file.
   *
   * @param dataset file's dataset
   * @param file    file
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public void revokePublicFileAccess(Dataset dataset, Path file) {
    Path relative = relativize(dataset, file);
    datasetPublicFileRepository.findByDatasetAndPath(dataset, relative.toString())
        .ifPresent(datasetPublicFileRepository::delete);
  }


  /**
   * Deletes dataset from database.
   *
   * @param dataset dataset
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public void delete(Dataset dataset) {
    if (!isDeletable(dataset)) {
      throw new IllegalArgumentException("dataset cannot be deleted");
    }
    repository.delete(dataset);
    Path folder = configuration.getHome().folder(dataset);
    try {
      FileSystemUtils.deleteRecursively(folder);
    } catch (IOException e) {
      logger.error("could not delete folder {}", folder);
    }
  }

  /**
   * Deletes dataset file.
   *
   * @param dataset dataset
   * @param file    file to delete
   */
  public void deleteFile(Dataset dataset, Path file) {
    Path filename = file.getFileName();
    if (filename == null) {
      throw new IllegalArgumentException("file " + file + " is empty");
    }
    Path folder = configuration.getHome().folder(dataset);
    if (!folder.equals(file.getParent())) {
      throw new IllegalArgumentException("file " + file + " not in folder " + folder);
    }
    Path deleted = folder.resolve(DELETED_FILENAME);
    try (Writer writer = Files.newBufferedWriter(deleted, StandardOpenOption.APPEND,
        StandardOpenOption.CREATE)) {
      writer.write(filename.toString());
      writer.write("\t");
      DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
      writer.write(formatter.format(toLocalDateTime(Files.getLastModifiedTime(file).toInstant())));
      writer.write("\t");
      writer.write(formatter.format(LocalDateTime.now()));
      writer.write("\n");
      Files.delete(file);
    } catch (IOException e) {
      throw new IllegalArgumentException("could not delete file " + file + " from folder " + folder,
          e);
    }
  }
}
