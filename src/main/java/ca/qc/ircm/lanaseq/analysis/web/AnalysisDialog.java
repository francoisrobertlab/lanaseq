package ca.qc.ircm.lanaseq.analysis.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.SpringComponent;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Analysis dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AnalysisDialog extends Dialog implements LocaleChangeObserver {
  private static final long serialVersionUID = 3521519771905055445L;
  public static final String ID = "analysis-dialog";
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String CREATE_FOLDER = "createFolder";
  public static final String ERRORS = "errors";
  public static final String CREATE_FOLDER_EXCEPTION = property(CREATE_FOLDER, "exception");
  protected H3 header = new H3();
  protected Div message = new Div();
  protected Button createFolder = new Button();
  protected ConfirmDialog confirm = new ConfirmDialog();
  protected VerticalLayout confirmLayout = new VerticalLayout();
  protected ConfirmDialog errors = new ConfirmDialog();
  protected VerticalLayout errorsLayout = new VerticalLayout();
  @Autowired
  private transient AnalysisDialogPresenter presenter;

  public AnalysisDialog() {
  }

  AnalysisDialog(AnalysisDialogPresenter presenter) {
    this.presenter = presenter;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    setId(ID);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.setMaxWidth("60em");
    layout.setMinWidth("22em");
    layout.add(header, message, createFolder, confirm, errors);
    header.setId(id(HEADER));
    message.setId(id(MESSAGE));
    createFolder.setId(id(CREATE_FOLDER));
    createFolder.addClickListener(e -> presenter.createFolder());
    confirm.setId(id(CONFIRM));
    confirm.setText(confirmLayout);
    errors.setId(id(ERRORS));
    errors.setText(errorsLayout);
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    AppResources resources = new AppResources(AnalysisDialog.class, getLocale());
    header.setText(resources.message(HEADER));
    message.setText(resources.message(MESSAGE));
    createFolder.setText(resources.message(CREATE_FOLDER));
    confirm.setHeader(resources.message(CONFIRM));
    confirm.setConfirmText(resources.message(property(CONFIRM, CONFIRM)));
    errors.setHeader(resources.message(ERRORS));
    errors.setConfirmText(resources.message(property(ERRORS, CONFIRM)));
    updateHeader();
    presenter.localChange(getLocale());
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(AnalysisDialog.class, getLocale());
    Dataset dataset = presenter.getDataset();
    if (dataset != null && dataset.getName() != null) {
      header.setText(resources.message(HEADER, dataset.getName()));
    } else {
      header.setText(resources.message(HEADER, ""));
    }
  }

  public void setDataset(Dataset dataset) {
    presenter.setDataset(dataset);
    updateHeader();
  }
}
