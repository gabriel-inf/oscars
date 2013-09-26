package net.es.nsi.cli.cmd;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultHistoryFileNameProvider;
import org.springframework.stereotype.Component;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NsiHistory extends DefaultHistoryFileNameProvider {

    @Override
    public String getHistoryFileName() {
        return "log/history.txt";
    }

    @Override
    public String getProviderName() {
        return "My History filename provider";
    }
}
