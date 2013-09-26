package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.AuthType;
import net.es.nsi.cli.config.NsiAuth;
import net.es.nsi.cli.config.ProviderProfile;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.db.DB_Util;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.prov.RequesterPortHolder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort;
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
public class ProviderCommands implements CommandMarker {

    @CliAvailabilityIndicator({"prov save", "prov set", "prov copy"})
    public boolean haveProfile() {
        ProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();
        return (!(currentProfile == null));
    }

    @CliCommand(value = "prov help", help = "display help")
    public String prov_help() {
        String help = "";
        help += "Provider Profiles:\n";
        help += "==================\n";
        help += "'prov all' shows all available profiles\n";
        help += "'prov copy' makes a copy of the current profile (*)\n";
        help += "'prov delete' deletes a profile\n";
        help += "'prov load' loads a profile for use\n";
        help += "'prov new' creates a new empty profile\n";
        help += "'prov save' saves the current profile (*)\n";
        help += "'prov set' changes settings in the current profile (*)\n";
        help += "'prov show' shows the profile settings\n";
        help += "   (*) : operation only available if a current profile exists.\n";
        return help;
    }


    @CliCommand(value = "prov all", help = "list all provider profiles")
    public String prov_all() {
        String out = "";
        try {
            out += "Database profiles:\n";
            List<ProviderProfile> profiles = DB_Util.getProviderProfiles();
            for (ProviderProfile profile : profiles) {
                out += profile.toString();
            }
            out += "\n\nConfigured profiles:\n";
            ApplicationContext ax = SpringContext.getInstance().getContext();
            Map<String, ProviderProfile> beans = ax.getBeansOfType(ProviderProfile.class);

            for (ProviderProfile profile : beans.values()) {
                out += profile.toString();
            }
            return out;
        } catch (CliInternalException ex) {
            ex.printStackTrace();
            return out;
        }
    }

    @CliCommand(value = "prov new", help = "create a new provider profile")
    public String prov_new(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a provider profile name") final String name) {
        ProviderProfile providerProfile = new ProviderProfile();
        providerProfile.setName(name);

        NsiCliState.getInstance().setProvProfile(providerProfile);
        return "provider profile created: [" + providerProfile.getName() + "]";
    }

    @CliCommand(value = "prov load", help = "load a provider profile")
    public String prov_load(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a provider profile name") final ProviderProfile inProfile) {
        NsiCliState.getInstance().setProvProfile(inProfile);
        String out;
        out = "profile loaded: [" + inProfile.getName() + "]\n";
        out += inProfile.toString();
        return out;
    }

    @CliCommand(value = "prov delete", help = "delete a named provider profile")
    public String prov_delete(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a provider profile name") final ProviderProfile inProfile) {
        String name = inProfile.getName();
        DB_Util.delete(inProfile);
        ProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();
        if (currentProfile != null) {
            if (inProfile.getName().equals(currentProfile.getName())) {
                NsiCliState.getInstance().setProvProfile(null);
            }
        }
        return "provider profile deleted: [" + name + "]";
    }


    @CliCommand(value = "prov show", help = "show current or named provider profile")
    public String prov_show(
            @CliOption(key = { "", "n" }, mandatory = false, help = "a provider profile name") final ProviderProfile inProfile) {
        ProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();

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


    @CliCommand(value = "prov save", help = "save the current providerProfile")
    public String prov_save() {
        ProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();
        if (currentProfile != null) {
            DB_Util.save(currentProfile);
            String out = currentProfile.toString();
            return out;
        }
        return "could not save provider profile";
    }


    @CliCommand(value = "prov copy", help = "make a copy of the current provider profile with a new name and set it as current")
    public String prov_copy(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a provider profile name") final String name) {
        ProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();

        if (currentProfile == null) {
            return "no current profile";
        }
        try {
            ProviderProfile previous = DB_Util.getProviderProfile(name);
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
            @CliOption(key = { "n" }, mandatory = false, help = "profile name") final String name,
            @CliOption(key = { "st" }, mandatory = false, help = "service type") final String serviceType,
            @CliOption(key = { "url" }, mandatory = false, help = "url") final String url,
            @CliOption(key = { "bus" }, mandatory = false, help = "busConfig") final File busConfig,
            @CliOption(key = { "auth" }, mandatory = false, help = "auth type") final AuthType auth,
            @CliOption(key = { "u" }, mandatory = false, help = "http-basic username") final String username,
            @CliOption(key = { "p" }, mandatory = false, help = "http-basic password") final String password,
            @CliOption(key = { "o" }, mandatory = false, help = "oauth token") final String oauth
    )
    {
        ProviderProfile currentProfile = NsiCliState.getInstance().getProvProfile();

        if (name != null)           currentProfile.setName(name);
        if (serviceType != null)    currentProfile.setServiceType(serviceType);
        if (url != null)            currentProfile.getProviderServer().setUrl(url);
        if (busConfig != null)      currentProfile.getProviderServer().setBusConfig(busConfig.getAbsolutePath());
        if (auth != null)           currentProfile.getProviderServer().getAuth().setAuthType(auth);
        if (username != null)       currentProfile.getProviderServer().getAuth().setUsername(username);
        if (password != null)       currentProfile.getProviderServer().getAuth().setPassword(password);
        if (oauth != null)          currentProfile.getProviderServer().getAuth().setOauth(oauth);


        return currentProfile.toString();
    }





}
