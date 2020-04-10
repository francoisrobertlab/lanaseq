package ca.qc.ircm.lanaseq.user.web;

import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.MainView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Change password view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PasswordViewPresenter {
  private Logger logger = LoggerFactory.getLogger(PasswordViewPresenter.class);
  private PasswordView view;
  @Autowired
  private UserService service;
  @Autowired
  private AuthorizationService authorizationService;

  protected PasswordViewPresenter() {
  }

  protected PasswordViewPresenter(UserService service, AuthorizationService authorizationService) {
    this.service = service;
    this.authorizationService = authorizationService;
  }

  void init(PasswordView view) {
    logger.debug("force change password view");
    this.view = view;
    view.passwords.setRequired(true);
  }

  private boolean validate() {
    return view.passwords.validate().isOk();
  }

  void save() {
    if (validate()) {
      User user = authorizationService.currentUser();
      String password = view.passwords.getPassword();
      logger.debug("save new password for user {}", user);
      service.save(password);
      UI.getCurrent().navigate(MainView.class);
    }
  }
}
