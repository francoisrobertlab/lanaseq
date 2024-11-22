package ca.qc.ircm.lanaseq.user;

/**
 * Object owned by a {@link User}.
 */
public interface Owned {
  /**
   * Returns owner.
   *
   * @return owner
   */
  public User getOwner();
}
