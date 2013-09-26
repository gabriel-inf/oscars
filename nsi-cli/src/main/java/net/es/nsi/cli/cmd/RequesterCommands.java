package net.es.nsi.cli.cmd;

import net.es.nsi.cli.client.CLI_ListenerHolder;
import net.es.nsi.cli.client.CliNsiHandler;
import net.es.nsi.cli.config.RequesterProfile;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.db.DB_Util;
import net.es.oscars.nsibridge.client.cli.CLIListener;
import net.es.oscars.nsibridge.config.SpringContext;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;


@Component
public class RequesterCommands implements CommandMarker {
    private static final Logger log = Logger.getLogger(RequesterCommands.class);
    @CliAvailabilityIndicator({"listener stop"})
    public boolean listenerStarted() {
        return NsiCliState.getInstance().isListenerStarted();
    }

    @CliAvailabilityIndicator({"listener start"})
    public boolean listenerStartable() {
        RequesterProfile currentProfile = NsiCliState.getInstance().getRequesterProfile();
        if (currentProfile == null) {
            log.debug("no profile");
            return false;
        }
        if (NsiCliState.getInstance().isListenerStarted()) {
            log.debug("already started");
            return false;
        }

        if (! NsiCliState.getInstance().isListenerStartable()) {
            log.debug("not startable");
            return false;
        }
        return true;
    }

    @CliCommand(value = "listener help", help = "display help")
    public String listener_help() {
        String help = "";
        help += "Callback Listener:\n";
        help += "==================\n";
        help += "'listener start' starts the callback listener\n";
        help += "     (available only if a requester profile is loaded (see 'req help')\n" +
                "      and the listener is stopped)\n";
        help += "'listener stop'  stops the listener\n";
        help += "     (available only if the listener is started)\n";
        return help;
    }


    @CliCommand(value = "req help", help = "display help")
    public String requester_help() {
        String help = "";
        help += "Requester Profiles:\n";
        help += "===================\n";
        help += "'req all' shows all available profiles\n";
        help += "'req copy' makes a copy of the current profile (*)\n";
        help += "'req delete' deletes a profile\n";
        help += "'req load' loads a profile for use\n";
        help += "'req new' creates a new empty profile\n";
        help += "'req save' saves the current profile (*)\n";
        help += "'req set' changes settings in the current profile (*)\n";
        help += "'req show' shows the profile settings\n";
        help += "   (*) : operation only available if a current profile exists.\n";

        return help;
    }


    @CliAvailabilityIndicator({"req save", "req set", "req copy"})
    public boolean haveProfile() {
        RequesterProfile currentProfile = NsiCliState.getInstance().getRequesterProfile();
        return (!(currentProfile == null));
    }

    @CliCommand(value = "req all", help = "list all requester profiles")
    public String requester_all() {
        String out = "";
        try {
            out += "Database profiles:\n";
            List<RequesterProfile> profiles = DB_Util.getRequesterProfiles();
            for (RequesterProfile profile : profiles) {
                out += profile.toString();
            }
            out += "\nConfigured profiles:\n";
            ApplicationContext ax = SpringContext.getInstance().getContext();
            Map<String, RequesterProfile> beans = ax.getBeansOfType(RequesterProfile.class);

            for (RequesterProfile profile : beans.values()) {
                out += profile.toString();
            }
            return out;
        } catch (CliInternalException ex) {
            ex.printStackTrace();
            return out;
        }
    }


    @CliCommand(value = "req new", help = "create a new requester profile")
    public String requester_new(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a requester profile name") final String name) {
        RequesterProfile profile = new RequesterProfile();
        profile.setName(name);

        NsiCliState.getInstance().setRequesterProfile(profile);
        return "requester profile created: [" + profile.getName() + "]";
    }

    @CliCommand(value = "req load", help = "load a requester profile")
    public String requester_load(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a requester profile name") final RequesterProfile inProfile) {
        NsiCliState.getInstance().setRequesterProfile(inProfile);
        String out;
        out = "profile loaded: [" + inProfile.getName() + "]\n";
        out += inProfile.toString();
        NsiCliState.getInstance().setListenerStartable(true);
        return out;
    }

    @CliCommand(value = "req delete", help = "delete a named requester profile")
    public String requester_delete(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a requester profile name") final RequesterProfile inProfile) {
        String name = inProfile.getName();
        DB_Util.delete(inProfile);
        RequesterProfile currentProfile = NsiCliState.getInstance().getRequesterProfile();
        if (currentProfile != null) {
            if (inProfile.getName().equals(currentProfile.getName())) {
                NsiCliState.getInstance().setProvProfile(null);
            }
        }
        return "requester profile deleted: [" + name + "]";
    }


    @CliCommand(value = "req show", help = "show current or named requester profile")
    public String requester_show(
            @CliOption(key = { "", "n" }, mandatory = false, help = "a requester profile name") final RequesterProfile inProfile) {
        RequesterProfile currentProfile = NsiCliState.getInstance().getRequesterProfile();

        if (inProfile != null) {
            String out = inProfile.toString();
            return out;
        } else if (currentProfile != null) {
            String out = currentProfile.toString();
            return out;
        } else {
            return "no current or named provider profile ";
        }
    }


    @CliCommand(value = "req save", help = "save the current requester profile")
    public String requester_save() {
        RequesterProfile currentProfile = NsiCliState.getInstance().getRequesterProfile();
        if (currentProfile != null) {
            DB_Util.save(currentProfile);
            String out = currentProfile.toString();
            return out;
        }
        return "could not save requester profile";
    }


    @CliCommand(value = "req copy", help = "make a copy of the current requester profile with a new name and set it as current")
    public String requester_copy(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a requester profile name") final String name) {
        RequesterProfile currentProfile = NsiCliState.getInstance().getRequesterProfile();

        if (currentProfile == null) {
            return "no current profile";
        }
        try {
            RequesterProfile previous = DB_Util.getRequesterProfile(name);
            if (previous != null) {
                return "cannot copy into existing profile for name "+name;
            }
            currentProfile = DB_Util.copyRequesterProfile(currentProfile, name);
            NsiCliState.getInstance().setRequesterProfile(currentProfile);
        } catch (CliInternalException ex) {
            ex.printStackTrace();
            return "internal error";
        }

        return "copied new requester profile with name "+name;
    }



    @CliCommand(value = "req set", help = "set current requester profile parameters")
    public String requester_set(
            @CliOption(key = { "n" }, mandatory = false, help = "profile name") final String name,
            @CliOption(key = { "url" }, mandatory = false, help = "URL") final String url,
            @CliOption(key = { "r" }, mandatory = false, help = "requester id") final String requesterId,
            @CliOption(key = { "bus" }, mandatory = false, help = "bus config filename") final File busConfig
    )
    {
        RequesterProfile currentProfile = NsiCliState.getInstance().getRequesterProfile();

        if (name != null)           currentProfile.setName(name);
        if (requesterId != null)    currentProfile.setRequesterId(requesterId);
        if (busConfig != null)      currentProfile.setBusConfig(busConfig.getAbsolutePath());
        if (url != null)            currentProfile.setUrl(url);


        return currentProfile.toString();
    }

    @CliCommand(value = "listener start", help = "start listener")
    public String listener_start() {
        RequesterProfile currentProfile = NsiCliState.getInstance().getRequesterProfile();
        String listenerUrl = currentProfile.getUrl();
        URL url;
        try {
            url = new URL(listenerUrl);
        } catch (MalformedURLException e) {
            return "malformed URL: "+listenerUrl;
        }

        try {
            CLIListener listener = CLI_ListenerHolder.getInstance().getListeners().get(currentProfile.getName());
            if (listener == null) {
                listener = new CLIListener(currentProfile.getUrl(), currentProfile.getBusConfig(), new CliNsiHandler());
                CLI_ListenerHolder.getInstance().getListeners().put(currentProfile.getName(), listener);
            }
            listener.start();
            NsiCliState.getInstance().setListenerStarted(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "could not start listener";
        }

        return "started listener at: "+listenerUrl;
    }

    @CliCommand(value = "listener stop", help = "stop listener")
    public String listener_stop() {
        RequesterProfile currentProfile = NsiCliState.getInstance().getRequesterProfile();
        CLIListener listener = CLI_ListenerHolder.getInstance().getListeners().get(currentProfile.getName());
        if (listener == null) {
            return "listener not started";
        }
        listener.stop();
        NsiCliState.getInstance().setListenerStarted(false);
        return "stopped listener";
    }



}
