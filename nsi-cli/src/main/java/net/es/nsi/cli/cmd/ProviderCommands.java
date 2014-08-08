package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.CliProviderProfile;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.db.DB_Util;
import net.es.nsi.client.types.AuthType;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;


@Component
public class ProviderCommands implements CommandMarker {

    @CliAvailabilityIndicator({"admin prov save", "admin prov set", "admin prov copy"})
    public boolean haveProfile() {
        CliProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();
        return (!(currentProfile == null));
    }

    @CliCommand(value = "admin prov help", help = "display help")
    public String admin_prov_help() {
        String help = "";
        help += "Provider Profiles:\n";
        help += "==================\n";
        help += "'admin prov all' shows all available profiles\n";
        help += "'admin prov copy' makes a copy of the current profile (*)\n";
        help += "'admin prov delete' deletes a profile\n";
        help += "'admin prov new' creates a new empty profile\n";
        help += "'admin prov save' saves the current profile (*)\n";
        help += "   (*) : operation only available if a current profile exists.\n";
        help += "See also: 'prov help'\n";
        return help;
    }

    @CliCommand(value = "prov help", help = "display help")
    public String prov_help() {
        String help = "";
        help += "Provider Profile Administration:\n";
        help += "==================\n";
        help += "'prov load' loads a profile for use\n";
        help += "'prov show' shows the profile settings\n";
        help += "'prov set' changes settings in the current profile (*)\n";
        help += "prov set --name <profile name>\n";
        help += "         --auth <auth type>\n";
        help += "         --bus <HTTP bus config file name>\n";
        help += "         --nsa <nsa>\n";
        help += "         --url <nsa id>\n";
        help += "         --prot <protocol version>\n";
        help += "         --st <service type>\n";
        help += "         --o <oauth token>\n";
        help += "         --u <http-basic username>\n";
        help += "         --p <http-basic password>\n";
        help += "   (*) : operation only available if a current profile exists.\n";
        help += "See also: 'admin prov help'\n";
        return help;
    }

    @CliCommand(value = "admin prov all", help = "list all provider profiles")
    public String prov_all() {
        String out = "";
        try {
            out += "Database profiles:\n";
            List<CliProviderProfile> profiles = DB_Util.getProviderProfiles();
            for (CliProviderProfile profile : profiles) {
                out += profile.toString();
            }
            return out;
        } catch (CliInternalException ex) {
            ex.printStackTrace();
            return out;
        }
    }

    @CliCommand(value = "admin prov new", help = "create a new provider profile")
    public String prov_new(
            @CliOption(key = { "name" }, mandatory = true, help = "a provider profile name") final String name) {
        CliProviderProfile providerProfile = new CliProviderProfile();
        providerProfile.setName(name);

        NsiCliState.getInstance().setProvProfile(providerProfile);
        return "provider profile created: [" + providerProfile.getName() + "]";
    }

    @CliCommand(value = "prov load", help = "load a provider profile")
    public String prov_load(
            @CliOption(key = { "name" }, mandatory = true, help = "a provider profile name") final CliProviderProfile inProfile) {
        NsiCliState.getInstance().setProvProfile(inProfile);
        String out;
        out = "profile loaded: [" + inProfile.getName() + "]\n";
        if (NsiCliState.getInstance().isVerbose()) {
            out += inProfile.toString();
        }
        return out;
    }

    @CliCommand(value = "admin prov delete", help = "delete a named provider profile")
    public String prov_delete(
            @CliOption(key = { "name" }, mandatory = true, help = "a provider profile name") final CliProviderProfile inProfile) {
        String name = inProfile.getName();
        DB_Util.delete(inProfile);
        CliProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();
        if (currentProfile != null) {
            if (inProfile.getName().equals(currentProfile.getName())) {
                NsiCliState.getInstance().setProvProfile(null);
            }
        }
        return "provider profile deleted: [" + name + "]";
    }


    @CliCommand(value = "prov show", help = "show current or named provider profile")
    public String prov_show(
            @CliOption(key = { "name" }, mandatory = false, help = "a provider profile name") final CliProviderProfile inProfile) {
        CliProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();

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


    @CliCommand(value = "admin prov save", help = "save the current providerProfile")
    public String prov_save() {
        CliProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();
        if (currentProfile != null) {
            DB_Util.save(currentProfile);
            String out = "";
            if (NsiCliState.getInstance().isVerbose()) {
                out += currentProfile.toString();
            }
            return out;
        }
        return "could not save provider profile";
    }


    @CliCommand(value = "admin prov copy", help = "make a copy of the current provider profile with a new name and set it as current")
    public String prov_copy(
            @CliOption(key = { "name" }, mandatory = true, help = "a provider profile name") final String name) {
        CliProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();

        if (currentProfile == null) {
            return "no current profile";
        }
        try {
            CliProviderProfile previous = DB_Util.getProviderProfile(name);
            if (previous != null) {
                return "cannot copy into existing profile for name "+name;
            }
            currentProfile = DB_Util.copyProviderProfile(currentProfile, name);
            NsiCliState.getInstance().setProvProfile(currentProfile);
        } catch (CliInternalException ex) {
            ex.printStackTrace();
            return "internal error";
        }

        return "copied new provider profile with name "+name;
    }



    @CliCommand(value = "prov set", help = "set current provider profile parameters")
    public String prov_set(
            @CliOption(key = { "name" }, mandatory = false, help = "profile name") final String name,
            @CliOption(key = { "st" }, mandatory = false, help = "service type") final String serviceType,
            @CliOption(key = { "url" }, mandatory = false, help = "url") final String url,
            @CliOption(key = { "prot" }, mandatory = false, help = "protocol version") final String prot,
            @CliOption(key = { "nsa" }, mandatory = false, help = "nsa") final String nsa,
            @CliOption(key = { "bus" }, mandatory = false, help = "busConfig") final File busConfig,
            @CliOption(key = { "auth" }, mandatory = false, help = "auth type") final AuthType auth,
            @CliOption(key = { "u" }, mandatory = false, help = "http-basic username") final String username,
            @CliOption(key = { "p" }, mandatory = false, help = "http-basic password") final String password,
            @CliOption(key = { "o" }, mandatory = false, help = "oauth token") final String oauth
    )
    {
        CliProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();

        if (name != null)           currentProfile.setName(name);
        if (serviceType != null)    currentProfile.setServiceType(serviceType);
        if (prot != null)           currentProfile.setProtocolVersion(prot);
        if (nsa != null)            currentProfile.setProviderNSA(nsa);
        if (url != null)            currentProfile.getProviderServer().setUrl(url);
        if (busConfig != null)      currentProfile.getProviderServer().setBusConfig(busConfig.getAbsolutePath());
        if (auth != null)           currentProfile.getProviderServer().getAuth().setAuthType(auth);
        if (username != null)       currentProfile.getProviderServer().getAuth().setUsername(username);
        if (password != null)       currentProfile.getProviderServer().getAuth().setPassword(password);
        if (oauth != null)          currentProfile.getProviderServer().getAuth().setOauth(oauth);

        if (NsiCliState.getInstance().isVerbose()) {
            return currentProfile.toString();
        } else {
            return "";
        }
    }





}
