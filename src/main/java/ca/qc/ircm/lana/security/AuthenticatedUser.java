package ca.qc.ircm.lana.security;

import ca.qc.ircm.lana.Data;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * An authenticated user.
 */
public class AuthenticatedUser extends User implements Data {
  private static final long serialVersionUID = -5167464958438112402L;
  private final Long id;

  /**
   * Construct the <code>UserWithId</code> with the details required by authentication.
   *
   * @param user
   *          user
   * @param accountNonExpired
   *          set to <code>true</code> if the account has not expired
   * @param credentialsNonExpired
   *          set to <code>true</code> if the credentials have not expired
   * @param accountNonLocked
   *          set to <code>true</code> if the account is not locked
   * @param authorities
   *          the authorities that should be granted to the caller if they presented the correct
   *          username and password and the user is enabled. Not null.
   *
   * @throws IllegalArgumentException
   *           if a <code>null</code> value was passed either as a parameter or as an element in the
   *           <code>GrantedAuthority</code> collection
   */
  public AuthenticatedUser(ca.qc.ircm.lana.user.User user, boolean accountNonExpired,
      boolean credentialsNonExpired, boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities) {
    super(user.getEmail(), user.getHashedPassword(), user.isActive(), accountNonExpired,
        credentialsNonExpired, accountNonLocked, authorities);
    id = user.getId();
  }

  /**
   * Construct the <code>UserWithId</code> with the details required by authentication.
   *
   * @param user
   *          user
   * @param authorities
   *          the authorities that should be granted to the caller if they presented the correct
   *          username and password and the user is enabled. Not null.
   *
   * @throws IllegalArgumentException
   *           if a <code>null</code> value was passed either as a parameter or as an element in the
   *           <code>GrantedAuthority</code> collection
   */
  public AuthenticatedUser(ca.qc.ircm.lana.user.User user,
      Collection<? extends GrantedAuthority> authorities) {
    super(user.getEmail(), user.getHashedPassword(), authorities);
    id = user.getId();
  }

  @Override
  public Long getId() {
    return id;
  }
}
