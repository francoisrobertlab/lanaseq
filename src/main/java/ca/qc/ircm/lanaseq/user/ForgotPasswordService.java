package ca.qc.ircm.lanaseq.user;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.mail.MailService;
import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
  private static final String MESSAGE_PREFIX = messagePrefix(ForgotPasswordService.class);
  private static final String CONSTANT_PREFIX = messagePrefix(Constants.class);
  private final Logger logger = LoggerFactory.getLogger(ForgotPasswordService.class);
  private final ForgotPasswordRepository repository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TemplateEngine emailTemplateEngine;
  private final MailService emailService;
  private final AppConfiguration appConfiguration;
  private final MessageSource messageSource;

  @Autowired
  protected ForgotPasswordService(ForgotPasswordRepository repository,
      UserRepository userRepository, PasswordEncoder passwordEncoder,
      TemplateEngine emailTemplateEngine, MailService emailService,
      AppConfiguration appConfiguration, MessageSource messageSource) {
    this.repository = repository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailTemplateEngine = emailTemplateEngine;
    this.emailService = emailService;
    this.appConfiguration = appConfiguration;
    this.messageSource = messageSource;
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
  public Optional<ForgotPassword> get(final long id, final String confirmNumber) {
    Objects.requireNonNull(confirmNumber, "confirmNumber parameter cannot be null");

    ForgotPassword forgotPassword = repository.findById(id).orElse(null);
    if (forgotPassword != null && confirmNumber.equals(forgotPassword.getConfirmNumber())
        && !forgotPassword.isUsed()
        && forgotPassword.getRequestMoment().isAfter(LocalDateTime.now().minus(VALID_PERIOD))) {
      return Optional.of(forgotPassword);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Inserts a new forgot password request for user into the database.
   *
   * @param email
   *          user's email
   * @param webContext
   *          web context used to send email to user
   */
  public void insert(String email, ForgotPasswordWebContext webContext) {
    Objects.requireNonNull(email, "email parameter cannot be null");
    Objects.requireNonNull(webContext, "webContext parameter cannot be null");

    ForgotPassword forgotPassword = new ForgotPassword();

    // Set time.
    forgotPassword.setRequestMoment(LocalDateTime.now());

    // Generate random confirm number.
    forgotPassword.setConfirmNumber(RandomStringUtils.secure().nextAlphanumeric(40));

    userRepository.findByEmail(email).ifPresent(user -> {
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
    });
  }

  private void sendMail(String emailAddress, ForgotPassword forgotPassword, Locale locale,
      ForgotPasswordWebContext webContext) throws MessagingException {
    // Prepare URL used to change password.
    final String url =
        appConfiguration.getUrl(webContext.getChangeForgottenPasswordUrl(forgotPassword, locale));

    // Prepare email content.
    MimeMessageHelper email = emailService.htmlEmail();
    String applicationName =
        messageSource.getMessage(CONSTANT_PREFIX + "application.name", null, locale);
    String subject = messageSource.getMessage(MESSAGE_PREFIX + "subject",
        new Object[] { applicationName }, locale);
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
    Objects.requireNonNull(forgotPassword, "forgotPassword parameter cannot be null");
    Objects.requireNonNull(newPassword, "newPassword parameter cannot be null");
    if (LocalDateTime.now().isAfter(forgotPassword.getRequestMoment().plus(VALID_PERIOD))) {
      throw new IllegalArgumentException("ForgotPassword instance has expired.");
    }

    // Get User that changes his password.
    User user = forgotPassword.getUser();

    // Encrypt password.
    String hashedPassword = passwordEncoder.encode(newPassword);
    // Update password.
    user = userRepository.findById(user.getId()).orElseThrow();
    user.setHashedPassword(hashedPassword);
    userRepository.save(user);

    // Tag ForgotPassword has being used.
    forgotPassword.setUsed(true);
    repository.save(forgotPassword);

    logger.info("Forgot password request {} was used", forgotPassword);
  }
}
