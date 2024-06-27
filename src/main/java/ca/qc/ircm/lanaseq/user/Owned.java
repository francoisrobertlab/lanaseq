package ca.qc.ircm.lanaseq.user;

import ca.qc.ircm.lanaseq.Data;

/**
 * Object owned by a {@link User}.
 */
public interface Owned extends Data {
  /**
   * Returns owner.
   *
   * @return owner
   */
  public User getOwner();
}
