package ca.qc.ircm.lanaseq.web;

import ca.qc.ircm.lanaseq.Constants;
import com.vaadin.flow.i18n.I18NProvider;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * {@link I18NProvider} implementation that uses {@link MessageSource}.
 */
@Component
public class MessageSourceI18NProvider implements I18NProvider {
  /**
   * {@link MessageSource} from Spring.
   */
  private final MessageSource messageSource;

  @Autowired
  protected MessageSourceI18NProvider(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public List<Locale> getProvidedLocales() {
    return Stream.of(Constants.ENGLISH, Constants.FRENCH).collect(Collectors.toList());
  }

  @Override
  public String getTranslation(String key, Locale locale, Object... params) {
    return messageSource.getMessage(key, params, locale);
  }

  @Override
  public String getTranslation(Object key, Locale locale, Object... params) {
    return I18NProvider.super.getTranslation(key, locale, params);
  }
}
