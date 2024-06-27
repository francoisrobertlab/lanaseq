package ca.qc.ircm.lanaseq.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;

/**
 * Tests for {@link PermissionEvaluatorDelegator}.
 */
@ServiceTestAnnotations
public class PermissionEvaluatorDelegatorTest {
  private static final String USER_CLASS = User.class.getName();
  private static final String DATASET_CLASS = Dataset.class.getName();
  private static final String PROTOCOL_CLASS = Protocol.class.getName();
  private static final String SAMPLE_CLASS = Sample.class.getName();
  private static final String READ = "read";
  private static final Permission BASE_READ = Permission.READ;
  private static final String WRITE = "write";
  private static final Permission BASE_WRITE = Permission.WRITE;
  @Autowired
  private PermissionEvaluatorDelegator permissionEvaluator;
  @MockBean
  private UserPermissionEvaluator userPermissionEvaluator;
  @MockBean
  private DatasetPermissionEvaluator datasetPermissionEvaluator;
  @MockBean
  private ProtocolPermissionEvaluator protocolPermissionEvaluator;
  @MockBean
  private SamplePermissionEvaluator samplePermissionEvaluator;
  @Mock
  private User user;
  private Collection<User> users = new ArrayList<>();
  @Mock
  private Dataset dataset;
  private Collection<Dataset> datasets = new ArrayList<>();
  @Mock
  private Protocol protocol;
  private Collection<Protocol> protocols = new ArrayList<>();
  @Mock
  private Sample sample;
  private Collection<Sample> samples = new ArrayList<>();

  private Authentication authentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @BeforeEach
  public void beforeTest() {
    users.add(mock(User.class));
    users.add(mock(User.class));
    datasets.add(mock(Dataset.class));
    datasets.add(mock(Dataset.class));
    protocols.add(mock(Protocol.class));
    protocols.add(mock(Protocol.class));
    samples.add(mock(Sample.class));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_User_False() {
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), users, READ));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), users, BASE_READ));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), users, WRITE));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), users, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
    verify(userPermissionEvaluator).hasPermission(authentication(), user, READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, BASE_READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, WRITE);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, BASE_WRITE);
    users.stream().limit(1).forEach(us -> {
      verify(userPermissionEvaluator).hasPermission(authentication(), us, READ);
      verify(userPermissionEvaluator).hasPermission(authentication(), us, BASE_READ);
      verify(userPermissionEvaluator).hasPermission(authentication(), us, WRITE);
      verify(userPermissionEvaluator).hasPermission(authentication(), us, BASE_WRITE);
    });
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS, READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        BASE_READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        WRITE);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_User_True() {
    when(userPermissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(userPermissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(true);
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), users, READ));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), users, BASE_READ));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), users, WRITE));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), users, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
    verify(userPermissionEvaluator).hasPermission(authentication(), user, READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, BASE_READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, WRITE);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, BASE_WRITE);
    users.forEach(us -> {
      verify(userPermissionEvaluator).hasPermission(authentication(), us, READ);
      verify(userPermissionEvaluator).hasPermission(authentication(), us, BASE_READ);
      verify(userPermissionEvaluator).hasPermission(authentication(), us, WRITE);
      verify(userPermissionEvaluator).hasPermission(authentication(), us, BASE_WRITE);
    });
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS, READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        BASE_READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        WRITE);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Dataset_False() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), datasets, READ));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), datasets, BASE_READ));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), datasets, WRITE));
    assertFalse(
        permissionEvaluator.hasCollectionPermission(authentication(), datasets, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, BASE_READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, WRITE);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, BASE_WRITE);
    datasets.stream().limit(1).forEach(ds -> {
      verify(datasetPermissionEvaluator).hasPermission(authentication(), ds, READ);
      verify(datasetPermissionEvaluator).hasPermission(authentication(), ds, BASE_READ);
      verify(datasetPermissionEvaluator).hasPermission(authentication(), ds, WRITE);
      verify(datasetPermissionEvaluator).hasPermission(authentication(), ds, BASE_WRITE);
    });
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, BASE_READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, WRITE);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Dataset_True() throws Throwable {
    when(datasetPermissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(datasetPermissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(true);
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), datasets, READ));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), datasets, BASE_READ));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), datasets, WRITE));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), datasets, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, BASE_READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, WRITE);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, BASE_WRITE);
    datasets.stream().forEach(ds -> {
      verify(datasetPermissionEvaluator).hasPermission(authentication(), ds, READ);
      verify(datasetPermissionEvaluator).hasPermission(authentication(), ds, BASE_READ);
      verify(datasetPermissionEvaluator).hasPermission(authentication(), ds, WRITE);
      verify(datasetPermissionEvaluator).hasPermission(authentication(), ds, BASE_WRITE);
    });
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, BASE_READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, WRITE);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Protocol_False() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), protocols, READ));
    assertFalse(
        permissionEvaluator.hasCollectionPermission(authentication(), protocols, BASE_READ));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), protocols, WRITE));
    assertFalse(
        permissionEvaluator.hasCollectionPermission(authentication(), protocols, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE));
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, BASE_READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, WRITE);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, BASE_WRITE);
    protocols.stream().limit(1).forEach(pr -> {
      verify(protocolPermissionEvaluator).hasPermission(authentication(), pr, READ);
      verify(protocolPermissionEvaluator).hasPermission(authentication(), pr, BASE_READ);
      verify(protocolPermissionEvaluator).hasPermission(authentication(), pr, WRITE);
      verify(protocolPermissionEvaluator).hasPermission(authentication(), pr, BASE_WRITE);
    });
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Protocol_True() throws Throwable {
    when(protocolPermissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(protocolPermissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(true);
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), protocols, READ));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), protocols, BASE_READ));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), protocols, WRITE));
    assertTrue(
        permissionEvaluator.hasCollectionPermission(authentication(), protocols, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_WRITE));
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, BASE_READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, WRITE);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, BASE_WRITE);
    protocols.stream().forEach(pr -> {
      verify(protocolPermissionEvaluator).hasPermission(authentication(), pr, READ);
      verify(protocolPermissionEvaluator).hasPermission(authentication(), pr, BASE_READ);
      verify(protocolPermissionEvaluator).hasPermission(authentication(), pr, WRITE);
      verify(protocolPermissionEvaluator).hasPermission(authentication(), pr, BASE_WRITE);
    });
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Sample_False() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, BASE_WRITE));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), samples, READ));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), samples, BASE_READ));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), samples, WRITE));
    assertFalse(permissionEvaluator.hasCollectionPermission(authentication(), samples, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), SAMPLE_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), SAMPLE_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        BASE_WRITE));
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample, READ);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample, BASE_READ);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample, WRITE);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample, BASE_WRITE);
    samples.stream().limit(1).forEach(sa -> {
      verify(samplePermissionEvaluator).hasPermission(authentication(), sa, READ);
      verify(samplePermissionEvaluator).hasPermission(authentication(), sa, BASE_READ);
      verify(samplePermissionEvaluator).hasPermission(authentication(), sa, WRITE);
      verify(samplePermissionEvaluator).hasPermission(authentication(), sa, BASE_WRITE);
    });
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        READ);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        BASE_READ);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        WRITE);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Sample_True() throws Throwable {
    when(samplePermissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(samplePermissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(true);
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, BASE_WRITE));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), samples, READ));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), samples, BASE_READ));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), samples, WRITE));
    assertTrue(permissionEvaluator.hasCollectionPermission(authentication(), samples, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), SAMPLE_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), SAMPLE_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        BASE_WRITE));
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample, READ);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample, BASE_READ);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample, WRITE);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample, BASE_WRITE);
    samples.stream().forEach(sa -> {
      verify(samplePermissionEvaluator).hasPermission(authentication(), sa, READ);
      verify(samplePermissionEvaluator).hasPermission(authentication(), sa, BASE_READ);
      verify(samplePermissionEvaluator).hasPermission(authentication(), sa, WRITE);
      verify(samplePermissionEvaluator).hasPermission(authentication(), sa, BASE_WRITE);
    });
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        READ);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        BASE_READ);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        WRITE);
    verify(samplePermissionEvaluator).hasPermission(authentication(), sample.getId(), SAMPLE_CLASS,
        BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Other() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), "test", READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "test", BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "test", WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "test", BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, String.class.getName(), READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, String.class.getName(), BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, String.class.getName(), WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), 1L, String.class.getName(),
        BASE_WRITE));
  }
}
