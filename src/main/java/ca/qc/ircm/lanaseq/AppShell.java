package ca.qc.ircm.lanaseq;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;

/**
 * Configures PUSH notifications for Vaadin.
 */
@Push(PushMode.MANUAL)
@SuppressWarnings("unused")
public class AppShell implements AppShellConfigurator {

}
