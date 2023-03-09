package ca.qc.ircm.lanaseq;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;

@PWA(name = "Location Analysis Net Application for High-throughput sequencing", shortName = "LANAseq")
@Push(PushMode.MANUAL)
public class AppShell implements AppShellConfigurator {
}
