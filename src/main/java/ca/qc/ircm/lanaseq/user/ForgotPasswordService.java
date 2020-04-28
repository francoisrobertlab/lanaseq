/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq.user;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.mail.MailService;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Locale;
import javax.mail.MessagingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for forgot password.
 */
@Service
@Transactional
public class ForgotPasswordService {
  /**
   * Period for which {@link ForgotPassword} instances are valid.
   */
  public static final Period VALID_PERIOD = Period.ofDays(2);
  private final Logger logger = LoggerFactory.getLogger(ForgotPasswordService.class);
  @Autowired
  private ForgotPasswordRepository repository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private TemplateEngine emailTemplateEngine;
  @Autowired
  private MailService emailService;
  @Autowired
  private AppConfiguration appConfiguration;

  protected ForgotPasswordService() {
  }

  /**
   * Returns ForgotPassword having this id.
   *
   * @param id
   *          Database identifier of ForgotPassword.
   * @param confirmNumber
   *          The confirm number of ForgotPassword.
   * @return ForgotPassword having this id.
   */
  public ForgotPassword get(final Long id, final String confirmNumber) {
    if (id == null || confirmNumber == null) {
      return null;
    }

    ForgotPassword forgotPassword = repository.findById(id).orElse(null);
    if (forgotPassword != null && confirmNumber.equals(forgotPassword.getConfirmNumber())
        && !forgotPassword.isUsed()
        && forgotPassword.getRequestMoment().isAfter(LocalDateTime.now().minus(VALID_PERIOD))) {
      return forgotPassword;
    } else {
      return null;
    }
  }

  /**
   * Inserts a new forgot password request for user into the database.
   *
   * @param email
   *          user's email
   * @param webContext
   *          web context used to send email to user
   * @return forgot password request created for user
   */
  public ForgotPassword insert(String email, ForgotPasswordWebContext webContext) {
    ForgotPassword forgotPassword = new ForgotPassword();

    // Set time.
    forgotPassword.setRequestMoment(LocalDateTime.now());

    // Generate random confirm number.
    forgotPassword.setConfirmNumber(RandomStringUtils.randomAlphanumeric(40));

    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
      // Ignore request.
      return null;
    }
    if (user.getId() == User.ROBOT_ID) {
      throw new AccessDeniedException("Cannot change password for robot");
    }
    forgotPassword.setUser(user);
    repository.saveAndFlush(forgotPassword);
    Locale locale = user.getLocale() != null ? user.getLocale() : Constants.DEFAULT_LOCALE;
    try {
      this.sendMail(email, forgotPassword, locale, webContext);
    } catch (Throwable e) {
      logger.error("Could not send email to user " + email + " that forgot his password", e);
    }

    logger.info("Forgot password request {} added to database", forgotPassword);

    return forgotPassword;
  }

  private void sendMail(String emailAddress, ForgotPassword forgotPassword, Locale locale,
      ForgotPasswordWebContext webContext) throws MessagingException {
    // Prepare URL used to change password.
    final String url =
        appConfiguration.getUrl(webContext.getChangeForgottenPasswordUrl(forgotPassword, locale));

    // Prepare email content.
    MimeMessageHelper email = emailService.htmlEmail();
    AppResources constants = new AppResources(Constants.class, locale);
    AppResources resources = new AppResources(ForgotPasswordService.class, locale);
    String subject = resources.message("subject", constants.message("application.name"));
    email.setSubject(subject);
    email.addTo(emailAddress);
    Context context = new Context(locale);
    context.setVariable("url", url);
    String htmlTemplateLocation = "/user/forgotpassword.html";
    String htmlEmail = emailTemplateEngine.process(htmlTemplateLocation, context);
    String textTemplateLocation = "/user/forgotpassword.txt";
    String textEmail = emailTemplateEngine.process(textTemplateLocation, context);
    email.setText(textEmail, htmlEmail);

    emailService.send(email);
  }

  /**
   * Updated password of user of the ForgotPassword request. ForgotPassword instance must still be
   * in it's valid period before this method is called or an {@link IllegalArgumentException} will
   * be raised.
   *
   * @param forgotPassword
   *          The ForgotPassword request.
   * @param newPassword
   *          The new password of User.
   * @throws IllegalArgumentException
   *           if forgotPassword has expired
   */
  public synchronized void updatePassword(ForgotPassword forgotPassword, String newPassword) {
    if (LocalDateTime.now().isAfter(forgotPassword.getRequestMoment().plus(VALID_PERIOD))) {
      throw new IllegalArgumentException("ForgotPassword instance has expired.");
    }

    // Get User that changes his password.
    User user = forgotPassword.getUser();

    // Encrypt password.
    String hashedPassword = passwordEncoder.encode(newPassword);
    // Update password.
    user = userRepository.findById(user.getId()).orElse(null);
    user.setHashedPassword(hashedPassword);
    userRepository.save(user);

    // Tag ForgotPassword has being used.
    forgotPassword.setUsed(true);
    repository.save(forgotPassword);

    logger.info("Forgot password request {} was used", forgotPassword);
  }
}
