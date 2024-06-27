package ca.qc.ircm.lanaseq.user;

import static ca.qc.ircm.lanaseq.FindbugsExplanations.ENTITY_EI_EXPOSE_REP;
import static jakarta.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
import ca.qc.ircm.processing.GeneratePropertyNames;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * For forgotten password information.
 */
@Entity
@GeneratePropertyNames
@SuppressFBWarnings(
    value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" },
    justification = ENTITY_EI_EXPOSE_REP)
public class ForgotPassword implements Data, Serializable {
  private static final long serialVersionUID = -2805056622482303376L;
  /**
   * Database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private Long id;
  /**
   * Moment where User requested a forgot password.
   */
  @Column
  private LocalDateTime requestMoment;
  /**
   * Confirm number for the forgot password request.
   */
  @Column
  private String confirmNumber;
  /**
   * Forgot password request was used.
   */
  @Column
  private boolean used;
  /**
   * User that created this forgot password request.
   */
  @ManyToOne
  @JoinColumn
  private User user;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((confirmNumber == null) ? 0 : confirmNumber.hashCode());
    result = prime * result + ((requestMoment == null) ? 0 : requestMoment.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ForgotPassword other = (ForgotPassword) obj;
    if (confirmNumber == null) {
      if (other.confirmNumber != null) {
        return false;
      }
    } else if (!confirmNumber.equals(other.confirmNumber)) {
      return false;
    }
    if (requestMoment == null) {
      if (other.requestMoment != null) {
        return false;
      }
    } else if (!requestMoment.equals(other.requestMoment)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ForgotPassword [id=" + id + "]";
  }

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDateTime getRequestMoment() {
    return requestMoment;
  }

  public void setRequestMoment(LocalDateTime requestMoment) {
    this.requestMoment = requestMoment;
  }

  public String getConfirmNumber() {
    return confirmNumber;
  }

  public void setConfirmNumber(String confirmNumber) {
    this.confirmNumber = confirmNumber;
  }

  public boolean isUsed() {
    return used;
  }

  public void setUsed(boolean used) {
    this.used = used;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
