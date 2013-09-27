package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.ResvProfile;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.db.DB_Util;
import net.es.oscars.nsibridge.config.SpringContext;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Component
public class ResvCommands implements CommandMarker {

    @CliAvailabilityIndicator({"resv save", "resv set", "resv copy"})
    public boolean haveProfile() {
        ResvProfile currentProfile = NsiCliState.getInstance().getResvProfile();

        return (!(currentProfile == null));
    }

    @CliCommand(value = "resv help", help = "display help")
    public String resv_help() {
        String help = "";
        help += "Reservation Profiles:\n";
        help += "=====================\n";
        help += "'resv all' shows all available profiles\n";
        help += "'resv copy' makes a copy of the current profile (*)\n";
        help += "'resv delete' deletes a profile\n";
        help += "'resv load' loads a profile for use\n";
        help += "'resv new' creates a new empty profile\n";
        help += "'resv save' saves the current profile (*)\n";
        help += "'resv set' changes settings in the current profile (*)\n";
        help += "'resv show' shows the profile settings\n";
        help += "   (*) : operation only available if a current profile exists.\n";
        return help;
    }



    @CliCommand(value = "resv new", help = "create a new reservation profile")
    public String resv_new(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a reservation profile name") final String name) {
        ResvProfile resvProfile = new ResvProfile();
        resvProfile.setName(name);
        NsiCliState.getInstance().setResvProfile(resvProfile);
        return "reservation profile created: [" + resvProfile.getName() + "]";
    }

    @CliCommand(value = "resv all", help = "list all reservation profiles")
    public String resv_all() {
        String out = "";
        try {
            out += "Database profiles:\n";
            List<ResvProfile> profiles = DB_Util.getResvProfiles();
            for (ResvProfile profile : profiles) {
                out += profile.toString();
            }
            out += "\n\nConfigured profiles:\n";
            ApplicationContext ax = SpringContext.getInstance().getContext();
            Map<String, ResvProfile> beans = ax.getBeansOfType(ResvProfile.class);

            for (ResvProfile profile : beans.values()) {
                out += profile.toString();
            }
            return out;
        } catch (CliInternalException ex) {
            ex.printStackTrace();
            return out;
        }
    }

    @CliCommand(value = "resv load", help = "load a named reservation profile")
    public String resv_load(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a reservation profile name") final ResvProfile inProfile) {
        NsiCliState.getInstance().setResvProfile(inProfile);
        String out;

        out = "profile loaded: [" + inProfile.getName() + "]\n";
        out += inProfile.toString();
        return out;
    }



    @CliCommand(value = "resv delete", help = "delete a named reservation profile")
    public String resv_delete(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a reservation profile name") final ResvProfile inProfile) {
        DB_Util.delete(inProfile);
        ResvProfile currentProfile = NsiCliState.getInstance().getResvProfile();
        if (currentProfile != null) {
            if (inProfile.getName().equals(currentProfile.getName())) {
                NsiCliState.getInstance().setResvProfile(null);
            }
        }
        return "reservation profile deleted: [" + inProfile.getName() + "]";
    }


    @CliCommand(value = "resv show", help = "show current or named reservation profile")
    public String resv_show(
            @CliOption(key = { "", "n" }, mandatory = false, help = "a reservation profile name") final ResvProfile inProfile) {
        ResvProfile currentProfile = NsiCliState.getInstance().getResvProfile();
        if (inProfile != null) {
            String out = inProfile.toString();
            return out;
        } else if (currentProfile != null) {
            String out = currentProfile.toString();
            return out;
        } else {
            return "no current or named reservation profile ";
        }
    }

    @CliCommand(value = "resv save", help = "save the current reservation profile")
    public String resv_save() {
        ResvProfile currentProfile = NsiCliState.getInstance().getResvProfile();
        if (currentProfile != null) {
            DB_Util.save(currentProfile);
            String out = currentProfile.toString();
            return out;
        }
        return "could not save current reservation profile";
    }



    @CliCommand(value = "resv copy", help = "make a copy of the current reservation profile with a new name and set it as current")
    public String resv_copy(
            @CliOption(key = { "", "n" }, mandatory = true, help = "a reservation profile name") final String name) {
        ResvProfile currentProfile = NsiCliState.getInstance().getResvProfile();
        if (currentProfile == null) {
            return "no current profile";
        }
        try {
            ResvProfile previous = DB_Util.getResvProfile(name);
            if (previous != null) {
                return "cannot copy into existing profile for name "+name;
            }
            currentProfile = DB_Util.copyResvProfile(currentProfile, name);
            NsiCliState.getInstance().setResvProfile(currentProfile);
        } catch (CliInternalException ex) {
            ex.printStackTrace();
            return "internal error";
        }

        return "copied new reservation profile with name "+name;
    }


    @CliCommand(value = "resv set", help = "set current reservation profile parameters")
    public String resv_set(
            @CliOption(key = { "n" },  mandatory = false, help = "profile name") final String name,
            @CliOption(key = { "b" },  mandatory = false, help = "bandwidth (mbps)") final Integer bw,
            @CliOption(key = { "v" },  mandatory = false, help = "version") final Integer version,

            @CliOption(key = { "st" }, mandatory = false, help = "start time (natty format)") final Date startTime,
            @CliOption(key = { "et" }, mandatory = false, help = "end time (natty format)") final Date endTime,

            @CliOption(key = { "g" },  mandatory = false, help = "gri") final String gri,
            @CliOption(key = { "d" },  mandatory = false, help = "description") final String description,

            @CliOption(key = { "sn" }, mandatory = false, help = "src network") final String srcNet,
            @CliOption(key = { "dn" }, mandatory = false, help = "dst network") final String dstNet,
            @CliOption(key = { "ss" }, mandatory = false, help = "src stp") final String srcStp,
            @CliOption(key = { "ds" }, mandatory = false, help = "dst stp") final String dstStp,
            @CliOption(key = { "sv" }, mandatory = false, help = "src vlan") final Integer srcVlan,
            @CliOption(key = { "dv" }, mandatory = false, help = "dst vlan") final Integer dstVlan
    ) {
        ResvProfile currentProfile = NsiCliState.getInstance().getResvProfile();

        if (name != null)       currentProfile.setName(name);
        if (bw != null)         currentProfile.setBandwidth(bw);
        if (gri != null)        currentProfile.setGri(gri);
        if (description!= null) currentProfile.setDescription(description);

        if (version != null)    currentProfile.setVersion(version);


        if (srcVlan != null)    currentProfile.setSrcVlan(srcVlan);
        if (srcNet != null)     currentProfile.setSrcNet(srcNet);
        if (srcStp != null)     currentProfile.setSrcStp(srcStp);

        if (dstVlan != null)    currentProfile.setDstVlan(dstVlan);
        if (dstNet != null)     currentProfile.setDstNet(dstNet);
        if (dstStp != null)     currentProfile.setDstStp(dstStp);

        if (startTime != null)  currentProfile.setStartTime(startTime);
        if (endTime != null)    currentProfile.setEndTime(endTime);

        if (NsiCliState.getInstance().isVerbose()) {
            return currentProfile.toString();
        } else {
            return "";
        }
    }


}
