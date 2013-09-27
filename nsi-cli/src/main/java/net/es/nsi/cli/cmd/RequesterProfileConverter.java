package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.RequesterProfile;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.db.DB_Util;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;

import java.util.List;

public class RequesterProfileConverter implements Converter<RequesterProfile> {


    @Override
    public boolean supports(final Class<?> requiredType, final String optionContext) {
        return RequesterProfile.class.isAssignableFrom(requiredType);
    }

    @Override
    public RequesterProfile convertFromText(final String value, final Class<?> requiredType, final String optionContext) {
        String name = value;
        if (value == null || value.isEmpty() ) {
            return null;
        }



        try {
            RequesterProfile prof = DB_Util.getRequesterProfile(name);
            return prof;
        } catch (CliInternalException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean getAllPossibleValues(final List<Completion> completions, final Class<?> requiredType, final String existingData, final String optionContext, final MethodTarget target) {
        try {
            List<RequesterProfile> providerProfiles = DB_Util.getRequesterProfiles();
            for (RequesterProfile prof : providerProfiles) {
                Completion completion = new Completion(prof.getName());
                completions.add(completion);
            }
        } catch (CliInternalException ex) {
            ex.printStackTrace();

        }

        return true;

    }
}
