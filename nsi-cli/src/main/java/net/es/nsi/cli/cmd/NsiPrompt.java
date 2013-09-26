package net.es.nsi.cli.cmd;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NsiPrompt extends DefaultPromptProvider {

    @Override
    public String getPrompt() {
        return "nsi>";
    }


    @Override
    public String getProviderName() {
        return "My prompt provider";
    }

}