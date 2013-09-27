package net.es.nsi.cli.cmd;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NsiBannerProvider extends DefaultBannerProvider {

    public String getBanner() {
        StringBuffer buf = new StringBuffer();
        buf.append("=======================================" + OsUtils.LINE_SEPARATOR);
        buf.append("*                                     *"+ OsUtils.LINE_SEPARATOR);
        buf.append("*            ESnet NSI CLI            *" +OsUtils.LINE_SEPARATOR);
        buf.append("*                                     *"+ OsUtils.LINE_SEPARATOR);
        buf.append("=======================================" + OsUtils.LINE_SEPARATOR);
        buf.append("Version:" + this.getVersion());
        return buf.toString();
    }

    public String getVersion() {
        return "1.0";
    }

    public String getWelcomeMessage() {
        return "Welcome to the ESnet NSI CLI";
    }

    @Override
    public String getProviderName() {
        return "ESnet NSI CLI";
    }
}