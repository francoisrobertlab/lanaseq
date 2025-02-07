package ca.qc.ircm.lanaseq.user;

import static ca.qc.ircm.lanaseq.UsedBy.SPRING;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;

import ca.qc.ircm.lanaseq.UsedBy;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Services for {@link User}.
 */
@Service
@Transactional
public class UserService {

  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticatedUser authenticatedUser;

  @Autowired
  @UsedBy(SPRING)
  protected UserService(UserRepository repository, PasswordEncoder passwordEncoder,
      AuthenticatedUser authenticatedUser) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
    this.authenticatedUser = authenticatedUser;
  }

  /**
   * Returns user having specified id.
   *
   * @param id user's id
   * @return user having specified id
   */
  @PostAuthorize("!returnObject.isPresent() || hasPermission(returnObject.get(), 'read')")
  public Optional<User> get(long id) {
    return repository.findById(id);
  }

  /**
   * Returns user having specified email.
   *
   * @param email user's email
   * @return user having specified email
   */
  @PostAuthorize("!returnObject.isPresent() || hasPermission(returnObject.get(), 'read')")
  public Optional<User> getByEmail(String email) {
    Objects.requireNonNull(email, "email parameter cannot be null");
    return repository.findByEmail(email);
  }

  /**
   * Returns true if a user exists with this email.
   *
   * @param email email
   * @return true if a user exists with this email
   */
  public boolean exists(String email) {
    Objects.requireNonNull(email, "email parameter cannot be null");
    return repository.findByEmail(email).isPresent();
  }

  /**
   * Returns all users.
   *
   * @return all users
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<User> all() {
    return repository.findAll();
  }

  /**
   * Saves user into database.
   * <p>
   * If user is a normal user, his laboratory must exists.
   * </p>
   * <p>
   * If user is a manager, his laboratory will be created.
   * </p>
   *
   * @param user     user
   * @param password user's unhashed password; required for new users; can be null to keep previous
   *                 password
   */
  @PreAuthorize("hasPermission(#user, 'write')")
  public void save(User user, @Nullable String password) {
    Objects.requireNonNull(user, "user parameter cannot be null");
    if (user.getId() == 1 && (!user.isAdmin() || !user.isActive())) {
      throw new AccessDeniedException("user 1 must be an admin and active");
    }

    final boolean reloadAuthorities = user.isExpiredPassword() && password != null;
    if (user.getId() == 0) {
      user.setActive(true);
      user.setCreationDate(LocalDateTime.now());
    }
    if (password != null) {
      String hashedPassword = passwordEncoder.encode(password);
      user.setHashedPassword(hashedPassword);
      user.setExpiredPassword(false);
    }
    repository.save(user);
    if (reloadAuthorities) {
      authenticatedUser.reloadAuthorities();
    }
  }

  /**
   * Saves new password for current user.
   *
   * @param password user's unhashed password
   */
  @PreAuthorize("hasAuthority('" + USER + "')")
  public void save(String password) {
    Objects.requireNonNull(password, "password parameter cannot be null");

    authenticatedUser.getUser().ifPresent(user -> {
      final boolean reloadAuthorities = user.isExpiredPassword();
      String hashedPassword = passwordEncoder.encode(password);
      user.setHashedPassword(hashedPassword);
      user.setExpiredPassword(false);
      repository.save(user);
      if (reloadAuthorities) {
        authenticatedUser.reloadAuthorities();
      }
    });
  }
}
