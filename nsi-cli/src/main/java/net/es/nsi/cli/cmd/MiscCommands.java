package net.es.nsi.cli.cmd;

import org.apache.log4j.Logger;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;


@Component
public class MiscCommands implements CommandMarker {
    private static final Logger log = Logger.getLogger(MiscCommands.class);

    @CliAvailabilityIndicator({"verbose off"})
    public boolean isVerbose() {
        return (NsiCliState.getInstance().isVerbose());
    }

    @CliAvailabilityIndicator({"verbose on"})
    public boolean isNotVerbose() {
        return (!NsiCliState.getInstance().isVerbose());
    }

    @CliCommand(value = "verbose off", help = "less verbose when setting parameters")
    public String verbose_off() {
        NsiCliState.getInstance().setVerbose(false);
        return "";
    }
    @CliCommand(value = "verbose on", help = "more verbose when setting parameters")
    public String verbose_on() {
        NsiCliState.getInstance().setVerbose(true);
        return "";
    }
    @CliCommand(value = "sleep", help = "sleep")
    public String sleep(
            @CliOption(key = { "sec" }, mandatory = true, help = "sec to sleep (default: 5 sec)") final Integer maxWait) {
        String out = "";

        Long sleepTime = maxWait * 1000L;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        out += "slept for: "+maxWait+" seconds";
        return out;

    }
}
