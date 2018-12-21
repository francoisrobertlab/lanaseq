package ca.qc.ircm.lana.experiment;

import static ca.qc.ircm.lana.test.utils.SearchUtils.find;
import static ca.qc.ircm.lana.time.TimeConverter.toInstant;
import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;
import static ca.qc.ircm.lana.user.UserRole.USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ExperimentServiceTest {
  private ExperimentService service;
  @Inject
  private ExperimentRepository repository;
  @Inject
  private UserRepository userRepository;
  @Mock
  private AuthorizationService authorizationService;

  @Before
  public void beforeTest() {
    service = new ExperimentService(repository, authorizationService);
  }

  @Test
  public void get() {
    Experiment experiment = service.get(1L);

    assertEquals((Long) 1L, experiment.getId());
    assertEquals("POLR2A DNA location", experiment.getName());
    assertEquals((Long) 2L, experiment.getOwner().getId());
    assertEquals(toInstant(LocalDateTime.of(2018, 10, 20, 13, 28, 12)), experiment.getDate());
    verify(authorizationService).checkRead(experiment);
  }

  @Test
  public void get_Null() {
    Experiment experiment = service.get(null);
    assertNull(experiment);
  }

  @Test
  public void all() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.currentUser()).thenReturn(user);

    List<Experiment> experiments = service.all();

    assertEquals(2, experiments.size());
    assertTrue(find(experiments, 2L).isPresent());
    assertTrue(find(experiments, 3L).isPresent());
    verify(authorizationService).checkRole(USER);
  }

  @Test
  public void all_Manager() {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.currentUser()).thenReturn(user);
    when(authorizationService.hasRole(MANAGER)).thenReturn(true);

    List<Experiment> experiments = service.all();

    assertEquals(3, experiments.size());
    assertTrue(find(experiments, 1L).isPresent());
    assertTrue(find(experiments, 2L).isPresent());
    assertTrue(find(experiments, 3L).isPresent());
    verify(authorizationService).checkRole(USER);
  }

  @Test
  public void all_Admin() {
    User user = userRepository.findById(1L).orElse(null);
    when(authorizationService.currentUser()).thenReturn(user);
    when(authorizationService.hasRole(ADMIN)).thenReturn(true);

    List<Experiment> experiments = service.all();

    assertEquals(5, experiments.size());
    assertTrue(find(experiments, 1L).isPresent());
    assertTrue(find(experiments, 2L).isPresent());
    assertTrue(find(experiments, 3L).isPresent());
    assertTrue(find(experiments, 4L).isPresent());
    assertTrue(find(experiments, 5L).isPresent());
    verify(authorizationService).checkRole(USER);
  }

  @Test
  public void save() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.currentUser()).thenReturn(user);
    Experiment experiment = new Experiment();
    experiment.setName("New experiment");

    service.save(experiment);

    assertNotNull(experiment.getId());
    Experiment database = repository.findById(experiment.getId()).orElse(null);
    assertEquals(experiment.getName(), database.getName());
    assertEquals(user.getId(), database.getOwner().getId());
    assertTrue(Instant.now().minusSeconds(10).isBefore(experiment.getDate()));
    assertTrue(Instant.now().plusSeconds(10).isAfter(experiment.getDate()));
    verify(authorizationService).checkRole(USER);
  }
}
