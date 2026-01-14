package ca.qc.ircm.lanaseq;

import static ca.qc.ircm.lanaseq.UsedBy.VAADIN;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;

/**
 * Configures PUSH notifications for Vaadin.
 */
@Push(PushMode.MANUAL)
@Theme("ircm")
@UsedBy(VAADIN)
public class AppShell implements AppShellConfigurator {

}
