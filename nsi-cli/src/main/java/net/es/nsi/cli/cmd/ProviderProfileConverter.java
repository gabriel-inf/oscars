package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.ProviderProfile;
import net.es.nsi.cli.config.ResvProfile;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.db.DB_Util;
import net.es.oscars.nsibridge.config.SpringContext;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;

import java.util.List;
import java.util.Map;

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
            ProviderProfile prof = DB_Util.getSpringProvProfile(name);
            if (prof != null) {
                return prof;
            }
            prof = DB_Util.getProviderProfile(name);
            return prof;
        } catch (CliInternalException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean getAllPossibleValues(final List<Completion> completions, final Class<?> requiredType, final String existingData, final String optionContext, final MethodTarget target) {
        ApplicationContext ax = SpringContext.getInstance().getContext();
        Map<String, ProviderProfile> beans = ax.getBeansOfType(ProviderProfile.class);

        for (ProviderProfile prof : beans.values()) {
            Completion completion = new Completion(prof.getName());
            completions.add(completion);
        }
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
