package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.ResvProfile;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.db.DB_Util;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;

import java.util.List;

public class ResvProfileConverter implements Converter<ResvProfile> {


    @Override
    public boolean supports(final Class<?> requiredType, final String optionContext) {
        return ResvProfile.class.isAssignableFrom(requiredType);
    }

    @Override
    public ResvProfile convertFromText(final String value, final Class<?> requiredType, final String optionContext) {
        String name = value;
        if (value == null || value.isEmpty() ) {
            return null;
        }


        try {
            ResvProfile prof = DB_Util.getResvProfile(name);
            return prof;
        } catch (CliInternalException ex) {
            ex.printStackTrace();
        }


        return null;
    }

    @Override
    public boolean getAllPossibleValues(final List<Completion> completions, final Class<?> requiredType, final String existingData, final String optionContext, final MethodTarget target) {

        try {
            List<ResvProfile> resvProfiles = DB_Util.getResvProfiles();
            for (ResvProfile prof : resvProfiles) {
                Completion completion = new Completion(prof.getName());
                completions.add(completion);
            }
        } catch (CliInternalException ex) {

        }


        return true;

    }
}
