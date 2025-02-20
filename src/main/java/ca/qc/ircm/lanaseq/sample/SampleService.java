package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.AppConfiguration.DELETED_FILENAME;
import static ca.qc.ircm.lanaseq.sample.QSample.sample;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toLocalDateTime;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.DataWithFiles;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.files.Renamer;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

/**
 * Service for {@link Sample}.
 */
@Service
@Transactional
public class SampleService {

  private static final Logger logger = LoggerFactory.getLogger(SampleService.class);
  private final SampleRepository repository;
  private final DatasetRepository datasetRepository;
  private final AppConfiguration configuration;
  private final AuthenticatedUser authenticatedUser;
  private final JPAQueryFactory queryFactory;

  @Autowired
  protected SampleService(SampleRepository repository, DatasetRepository datasetRepository,
      AppConfiguration configuration, AuthenticatedUser authenticatedUser,
      JPAQueryFactory queryFactory) {
    this.repository = repository;
    this.datasetRepository = datasetRepository;
    this.configuration = configuration;
    this.authenticatedUser = authenticatedUser;
    this.queryFactory = queryFactory;
  }

  /**
   * Returns sample with id.
   *
   * @param id sample's id
   * @return sample with id
   */
  @PostAuthorize("!returnObject.isPresent() || hasPermission(returnObject.get(), 'read')")
  public Optional<Sample> get(long id) {
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
   * Returns all samples.
   *
   * @return all samples
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Sample> all() {
    return repository.findAll();
  }

  /**
   * Returns all samples passing filter.
   *
   * @param filter   filter
   * @param pageable sorts and limits number of results
   * @return all samples passing filter
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public Stream<Sample> all(SampleFilter filter, Pageable pageable) {
    Objects.requireNonNull(filter, "filter parameter cannot be null");
    return repository.findAll(filter.predicate(), pageable).stream();
  }

  /**
   * Returns number of samples passing filter.
   *
   * @param filter filter
   * @return number of samples passing filter
   */
  public long count(SampleFilter filter) {
    Objects.requireNonNull(filter, "filter parameter cannot be null");
    return repository.count(filter.predicate());
  }

  /**
   * Returns all sample's files.
   *
   * @param sample sample
   * @return all sample's files
   */
  @PreAuthorize("hasPermission(#sample, 'read')")
  public List<Path> files(Sample sample) {
    Objects.requireNonNull(sample, "sample parameter cannot be null");
    if (sample.getId() == 0) {
      return new ArrayList<>();
    }
    List<Path> files = new ArrayList<>();
    Path folder = configuration.getHome().folder(sample);
    try (Stream<Path> homeFiles = Files.list(folder)) {
      homeFiles.filter(file -> !DELETED_FILENAME.equals(file.getFileName().toString()))
          .filter(file -> !file.toFile().isHidden()).forEach(files::add);
    } catch (IOException e) {
      // Ignore since folder probably does not exist.
    }
    for (AppConfiguration.NetworkDrive<DataWithFiles> drive : configuration.getArchives()) {
      folder = drive.folder(sample);
      try (Stream<Path> archiveFiles = Files.list(folder)) {
        archiveFiles.filter(file -> !DELETED_FILENAME.equals(file.getFileName().toString()))
            .filter(file -> !file.toFile().isHidden()).forEach(files::add);
      } catch (IOException e) {
        // Ignore since folder probably does not exist.
      }
    }
    if (!sample.getFilenames().isEmpty()) {
      List<PathMatcher> matchers = sample.getFilenames().stream()
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
   * Returns all sample's folder labels.
   * <p>
   * Only returns label for folders that exist.
   * </p>
   *
   * @param sample sample
   * @param unix   true if path elements should be separated by slashes instead of backslashes
   * @return all sample's folder labels
   */
  @PreAuthorize("hasPermission(#sample, 'read')")
  public List<String> folderLabels(Sample sample, boolean unix) {
    Objects.requireNonNull(sample, "sample parameter cannot be null");
    if (sample.getId() == 0) {
      return new ArrayList<>();
    }
    List<String> labels = new ArrayList<>();
    Path folder = configuration.getHome().folder(sample);
    if (Files.exists(folder)) {
      labels.add(configuration.getHome().label(sample, unix));
    }
    for (AppConfiguration.NetworkDrive<DataWithFiles> drive : configuration.getArchives()) {
      folder = drive.folder(sample);
      if (Files.exists(folder)) {
        labels.add(drive.label(sample, unix));
      }
    }
    return labels;
  }

  /**
   * Returns all sample's upload files.
   *
   * @param sample sample
   * @return all sample's upload files
   */
  @PreAuthorize("hasPermission(#sample, 'read')")
  public List<Path> uploadFiles(Sample sample) {
    Objects.requireNonNull(sample, "sample parameter cannot be null");
    if (sample.getId() == 0) {
      return new ArrayList<>();
    }
    Path upload = configuration.getUpload().getFolder();
    Path sampleUpload = configuration.getUpload().folder(sample);
    try {
      List<Path> files = new ArrayList<>();
      if (Files.exists(upload)) {
        try (Stream<Path> uploadFiles = Files.list(upload)) {
          uploadFiles.filter(file -> file.toFile().isFile()).filter(file -> {
            String filename = file.getFileName().toString();
            return filename.contains(sample.getName());
          }).filter(file -> !file.toFile().isHidden()).forEach(files::add);
        }
      }
      if (Files.exists(sampleUpload)) {
        try (Stream<Path> uploadFiles = Files.list(sampleUpload)) {
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
   * Returns most recent keywords.
   *
   * @param limit maximum number of keywords to return
   * @return most recent keywords
   */
  public List<String> topKeywords(int limit) {
    Set<String> keywords = new LinkedHashSet<>();
    int page = 0;
    while (keywords.size() < limit) {
      Page<Sample> samples = repository.findAllByOrderByIdDesc(PageRequest.of(page++, 50));
      if (samples.isEmpty()) {
        break; // No more samples.
      }
      keywords.addAll(samples.flatMap(s -> s.getKeywords().stream()).toList());
    }
    return keywords.stream().limit(limit).collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Returns most recent assays.
   *
   * @param limit maximum number of assays to return
   * @return most recent assays
   */
  public List<String> topAssays(int limit) {
    return queryFactory.select(sample.assay).distinct().from(sample).orderBy(sample.id.desc())
        .limit(limit).fetch();
  }

  /**
   * Returns most recent types.
   *
   * @param limit maximum number of types to return
   * @return most recent types
   */
  public List<String> topTypes(int limit) {
    return queryFactory.select(sample.type).distinct().from(sample).where(sample.type.isNotNull())
        .orderBy(sample.id.desc()).limit(limit).fetch();
  }

  /**
   * Returns true if sample can be deleted, false otherwise.
   *
   * @param sample sample
   * @return true if sample can be deleted, false otherwise
   */
  @PreAuthorize("hasPermission(#sample, 'read')")
  public boolean isDeletable(Sample sample) {
    Objects.requireNonNull(sample, "sample parameter cannot be null");
    return sample.isEditable() && !datasetRepository.existsBySamples(sample);
  }

  /**
   * Returns true if samples can be merged, false otherwise.
   *
   * @param samples samples
   * @return true if samples can be merged, false otherwise
   */
  @PreAuthorize("hasRole('USER')")
  public boolean isMergable(Collection<Sample> samples) {
    Objects.requireNonNull(samples, "samples parameter cannot be null");
    if (samples.isEmpty()) {
      return false;
    }
    Sample first = samples.stream().findAny().orElse(new Sample());
    boolean mergable = true;
    for (Sample sample : samples) {
      mergable &= first.getProtocol().getId() == sample.getProtocol().getId();
      mergable &= first.getAssay().equals(sample.getAssay());
      mergable &= first.getType() != null ? first.getType().equals(sample.getType())
          : sample.getType() == null;
      mergable &= first.getTarget() != null ? first.getTarget().equals(sample.getTarget())
          : sample.getTarget() == null;
      mergable &= first.getStrain().equals(sample.getStrain());
      mergable &= first.getStrainDescription() != null ? first.getStrainDescription()
          .equals(sample.getStrainDescription()) : sample.getStrainDescription() == null;
      mergable &= first.getTreatment() != null ? first.getTreatment().equals(sample.getTreatment())
          : sample.getTreatment() == null;
    }
    return mergable;
  }

  /**
   * Saves sample into database.
   *
   * @param sample sample
   */
  @PreAuthorize("hasPermission(#sample, 'write')")
  public void save(Sample sample) {
    Objects.requireNonNull(sample, "sample parameter cannot be null");
    Objects.requireNonNull(sample.getName(), "sample's name cannot be null");
    if (sample.getId() != 0 && !sample.isEditable()) {
      throw new IllegalArgumentException("sample " + sample + " cannot be edited");
    }
    LocalDateTime now = LocalDateTime.now();
    User user = authenticatedUser.getUser().orElseThrow();
    if (sample.getId() == 0) {
      sample.setOwner(user);
      sample.setCreationDate(now);
      sample.setEditable(true);
    }
    Sample old = old(sample).orElse(null);
    if (old == null) {
      repository.save(sample);
    } else {
      final String oldName = old.getName();
      final Path oldFolder = configuration.getHome().folder(old);
      List<Path> oldArchives = configuration.getArchives().stream().map(drive -> drive.folder(old))
          .toList();
      repository.save(sample);
      Path folder = configuration.getHome().folder(sample);
      Renamer.moveFolder(oldFolder, folder);
      Renamer.renameFiles(oldName, sample.getName(), folder);
      for (int i = 0; i < configuration.getArchives().size(); i++) {
        Path oldArchive = oldArchives.get(i);
        Path archive = configuration.getArchives().get(i).folder(sample);
        Renamer.moveFolder(oldArchive, archive);
        Renamer.renameFiles(oldName, sample.getName(), archive);
      }
    }
  }

  @Transactional(TxType.REQUIRES_NEW)
  protected Optional<Sample> old(Sample sample) {
    if (sample.getId() != 0) {
      return repository.findById(sample.getId());
    } else {
      return Optional.empty();
    }
  }

  /**
   * Save files to sample folder.
   *
   * @param sample sample
   * @param files  files to save
   */
  @PreAuthorize("hasPermission(#sample, 'write')")
  public void saveFiles(Sample sample, Collection<Path> files) {
    Path folder = configuration.getHome().folder(sample);
    try {
      Files.createDirectories(folder);
    } catch (IOException e) {
      throw new IllegalStateException("could not create folder " + folder, e);
    }
    for (Path file : files) {
      Path target = folder.resolve(file.getFileName());
      try {
        logger.debug("moving file {} to {} for sample {}", file, target, sample);
        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
        Files.delete(file);
      } catch (IOException e) {
        throw new IllegalArgumentException("could not move file " + file + " to " + target, e);
      }
    }
  }

  /**
   * Deletes sample from database.
   *
   * @param sample sample
   */
  @PreAuthorize("hasPermission(#sample, 'write')")
  public void delete(Sample sample) {
    if (!isDeletable(sample)) {
      throw new IllegalArgumentException("sample cannot be deleted");
    }
    repository.delete(sample);
    Path folder = configuration.getHome().folder(sample);
    try {
      FileSystemUtils.deleteRecursively(folder);
    } catch (IOException e) {
      logger.error("could not delete folder {}", folder);
    }
  }

  /**
   * Deletes sample file.
   *
   * @param sample sample
   * @param file   file to delete
   */
  public void deleteFile(Sample sample, Path file) {
    Path filename = file.getFileName();
    if (filename == null) {
      throw new IllegalArgumentException("file " + file + " is empty");
    }
    Path folder = configuration.getHome().folder(sample);
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
