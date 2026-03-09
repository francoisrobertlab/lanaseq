package ca.qc.ircm.lanaseq;

import static ca.qc.ircm.lanaseq.UsedBy.VAADIN;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * Configures PUSH notifications for Vaadin.
 */
@Push(PushMode.MANUAL)
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
@UsedBy(VAADIN)
public class AppShell implements AppShellConfigurator {

}
