package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.DefaultProfiles;
import net.es.nsi.cli.config.ProviderProfile;
import net.es.nsi.cli.config.RequesterProfile;
import net.es.nsi.cli.config.ResvProfile;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.db.DB_Util;
import net.es.oscars.nsibridge.config.SpringContext;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class DefaultsCommands implements CommandMarker {
    private static final Logger log = Logger.getLogger(DefaultsCommands.class);

    @CliCommand(value = "defaults help", help = "display help")
    public String defaults_help() {
        String help = "";
        help += "Setting defaults:\n";
        help += "==================\n";
        help += "use 'defaults resv' to set the reservation profile default\n";
        help += "use 'defaults prov' to set the provider profile default\n";
        help += "use 'defaults req' to set the requester profile default\n";
        help += "use 'defaults show' to show the defaults\n";
        help += "use 'defaults save' to save defaults\n";
        help += "use 'defaults load' to load defaults\n";
        return help;
    }

    @CliCommand(value = "defaults show", help = "show all defaults")
    public String defaults_show() {
        String out = "";
        ApplicationContext ax = SpringContext.getInstance().getContext();
        try {
            DefaultProfiles defs = NsiCliState.getInstance().getDefs();
            if (defs == null) {
                return "no defaults set";
            }

            ResvProfile rp = DB_Util.getResvProfile(defs.getResvProfileName());
            if (rp == null) {
                Map<String, ResvProfile> beans = ax.getBeansOfType(ResvProfile.class);
                for (ResvProfile prof : beans.values()) {
                    if (prof.getName().equals(defs.getResvProfileName())) {
                        rp = prof;
                        break;
                    }
                }
            }
            out += "\nDefault Reservation Profile:\n";
            if (rp == null) {
                out += "None set\n";
            } else {
                out += rp.toString();
            }




            ProviderProfile pp = DB_Util.getProviderProfile(defs.getProvProfileName());
            if (pp == null) {
                Map<String, ProviderProfile> beans = ax.getBeansOfType(ProviderProfile.class);
                for (ProviderProfile prof : beans.values()) {
                    if (prof.getName().equals(defs.getProvProfileName())) {
                        pp = prof;
                        break;
                    }
                }
            }
            out += "\nDefault Provider Profile: \n";
            if (pp == null) {
                out += "None set\n";
            } else {
                out += pp.toString();
            }



            RequesterProfile rqp  = DB_Util.getRequesterProfile(defs.getRequesterProfileName());

            if (rqp == null) {
                Map<String, RequesterProfile> beans = ax.getBeansOfType(RequesterProfile.class);
                for (RequesterProfile prof : beans.values()) {
                    if (prof.getName().equals(defs.getRequesterProfileName())) {
                        rqp = prof;
                        break;
                    }
                }
            }

            out += "\nDefault Requester Profile: \n";
            if (rqp == null) {
                out += "None set\n";
            } else {
                out += rqp.toString();
            }


            return out;
        } catch (CliInternalException ex) {
            log.error(ex.getMessage(), ex);
            ex.printStackTrace();
            return out;
        }
    }



    @CliCommand(value = "defaults resv", help = "set the default reservation profile name")
    public String defaults_resv(
            @CliOption(key = { "name" }, mandatory = true, help = "a reservation profile name") final ResvProfile inProfile) {
        String out = "";

        DefaultProfiles defs = NsiCliState.getInstance().getDefs();

        if (defs == null) {
            defs = new DefaultProfiles();
            NsiCliState.getInstance().setDefs(defs);
        }

        defs.setResvProfileName(inProfile.getName());
        out += "set default reservation profile: "+inProfile.getName();

        return out;

    }
    @CliCommand(value = "defaults prov", help = "set the default provider profile name")
    public String defaults_prov(
            @CliOption(key = { "name" }, mandatory = true, help = "a provider profile name") final ProviderProfile inProfile) {
        String out = "";
        DefaultProfiles defs = NsiCliState.getInstance().getDefs();
        if (defs == null) {
            defs = new DefaultProfiles();
            NsiCliState.getInstance().setDefs(defs);
        }
        defs.setProvProfileName(inProfile.getName());
        out += "set default provider profile: "+inProfile.getName();

        return out;
    }


    @CliCommand(value = "defaults req", help = "set the default requester profile name")
    public String defaults_req(
            @CliOption(key = { "name" }, mandatory = true, help = "a requester  profile name") final RequesterProfile inProfile) {
        String out = "";
        DefaultProfiles defs = NsiCliState.getInstance().getDefs();

        if (defs == null) {
            defs = new DefaultProfiles();
            NsiCliState.getInstance().setDefs(defs);
        }
        defs.setRequesterProfileName(inProfile.getName());
        out += "set default provider profile: "+inProfile.getName();

        return out;
    }


    @CliCommand(value = "defaults save", help = "save the defaults")
    public String defaults_save() {
        DefaultProfiles defs = NsiCliState.getInstance().getDefs();
        if (defs != null) {
            try {
                DB_Util.save(defs);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return ex.getMessage();
            }
            String out = defs.toString();
            return out;
        }
        return "could not save provider profile";
    }


    @CliCommand(value = "defaults load", help = "load defaults")
    public String defaults_load() {
        try {
            DefaultProfiles defs = DB_Util.getDefaults();
            if (defs != null) {
                String out = defs.toString();
                return out;
            }
            NsiCliState.getInstance().setDefs(defs);
        } catch (CliInternalException ex) {
            log.error(ex.getMessage(), ex);

            ex.printStackTrace();
        }
        return "could not load defaults";
    }
}
