package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.*;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.db.DB_Util;
import net.es.nsi.cli.config.CliSpringContext;
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

    @CliCommand(value = "admin defaults help", help = "display help")
    public String defaults_help() {
        String help = "";
        help += "Setting defaults:\n";
        help += "==================\n";
        help += "use 'admin defaults resv' to set the reservation profile default\n";
        help += "use 'admin defaults prov' to set the provider profile default\n";
        help += "use 'admin defaults req' to set the requester profile default\n";
        help += "use 'admin defaults show' to show the defaults\n";
        help += "use 'admin defaults save' to save defaults\n";
        help += "use 'admin defaults load' to load defaults\n";
        return help;
    }

    @CliCommand(value = "admin defaults show", help = "show all defaults")
    public String defaults_show() {
        String out = "";
        ApplicationContext ax = CliSpringContext.getInstance().getContext();
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




            CliProviderProfile pp = DB_Util.getProviderProfile(defs.getProvProfileName());
            if (pp == null) {
                Map<String, CliProviderProfile> beans = ax.getBeansOfType(CliProviderProfile.class);
                for (CliProviderProfile prof : beans.values()) {
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



            CliRequesterProfile rqp  = DB_Util.getRequesterProfile(defs.getRequesterProfileName());

            if (rqp == null) {
                Map<String, CliRequesterProfile> beans = ax.getBeansOfType(CliRequesterProfile.class);
                for (CliRequesterProfile prof : beans.values()) {
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



    @CliCommand(value = "admin defaults resv", help = "set the default reservation profile name")
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
    @CliCommand(value = "admin defaults prov", help = "set the default provider profile name")
    public String defaults_prov(
            @CliOption(key = { "name" }, mandatory = true, help = "a provider profile name") final CliProviderProfile inProfile) {
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


    @CliCommand(value = "admin defaults req", help = "set the default requester profile name")
    public String defaults_req(
            @CliOption(key = { "name" }, mandatory = true, help = "a requester  profile name") final CliRequesterProfile inProfile) {
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


    @CliCommand(value = "admin defaults save", help = "save the defaults")
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


    @CliCommand(value = "admin defaults load", help = "load defaults")
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
