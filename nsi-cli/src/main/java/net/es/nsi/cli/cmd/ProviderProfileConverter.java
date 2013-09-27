package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.ProviderProfile;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.db.DB_Util;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;

import java.util.List;

public class ProviderProfileConverter implements Converter<ProviderProfile> {


    @Override
    public boolean supports(final Class<?> requiredType, final String optionContext) {
        return ProviderProfile.class.isAssignableFrom(requiredType);
    }

    @Override
    public ProviderProfile convertFromText(final String value, final Class<?> requiredType, final String optionContext) {
        String name = value;
        if (value == null || value.isEmpty() ) {
            return null;
        }

        try {
            ProviderProfile prof = DB_Util.getProviderProfile(name);
            return prof;
        } catch (CliInternalException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean getAllPossibleValues(final List<Completion> completions, final Class<?> requiredType, final String existingData, final String optionContext, final MethodTarget target) {
        try {
            List<ProviderProfile> providerProfiles = DB_Util.getProviderProfiles();
            for (ProviderProfile prof : providerProfiles) {
                Completion completion = new Completion(prof.getName());
                completions.add(completion);
            }
        } catch (CliInternalException ex) {
            ex.printStackTrace();

        }

        return true;

    }
}
