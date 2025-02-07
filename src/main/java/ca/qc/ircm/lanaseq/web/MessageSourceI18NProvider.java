package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.UsedBy.SPRING;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.UsedBy;
import com.vaadin.flow.i18n.I18NProvider;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

/**
 * {@link I18NProvider} implementation that uses {@link MessageSource}.
 */
@Component
public class MessageSourceI18NProvider implements I18NProvider {

  private static final Logger logger = LoggerFactory.getLogger(MessageSourceI18NProvider.class);
  /**
   * {@link MessageSource} from Spring.
   */
  private final MessageSource messageSource;

  @Autowired
  @UsedBy(SPRING)
  protected MessageSourceI18NProvider(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public List<Locale> getProvidedLocales() {
    return Constants.getLocales();
  }

  @Override
  public String getTranslation(String key, Locale locale, Object... params) {
    try {
      return messageSource.getMessage(key, params, locale);
    } catch (NoSuchMessageException e) {
      logger.warn("Could not find message for key {}", key);
      return "!{" + key + "}!";
    }
  }
}
