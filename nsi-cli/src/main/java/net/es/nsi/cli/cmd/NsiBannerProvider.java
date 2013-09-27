package net.es.nsi.cli.cmd;

import org.apache.commons.io.FileUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.shell.plugin.support.DefaultBannerProvider;

import java.io.File;
import java.io.IOException;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NsiBannerProvider extends DefaultBannerProvider {

    public String getBanner() {
        File f = new File("config/banner.txt");
        String out = null;
        try {
            out = FileUtils.readFileToString(f);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return out;
    }

    public String getVersion() {
        return "1.0";
    }

    public String getWelcomeMessage() {
        return "";
    }

    @Override
    public String getProviderName() {
        return "ESnet NSI CLI";
    }
}