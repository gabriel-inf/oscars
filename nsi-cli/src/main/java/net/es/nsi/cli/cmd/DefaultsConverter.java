package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.DefaultProfiles;
import net.es.nsi.cli.core.CliInternalException;
import net.es.nsi.cli.db.DB_Util;
import net.es.oscars.nsibridge.config.SpringContext;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;

import java.util.List;
import java.util.Map;

public class DefaultsConverter implements Converter<DefaultProfiles> {


    @Override
    public boolean supports(final Class<?> requiredType, final String optionContext) {
        return DefaultProfiles.class.isAssignableFrom(requiredType);
    }

    @Override
    public DefaultProfiles convertFromText(final String value, final Class<?> requiredType, final String optionContext) {
        String name = value;
        if (value == null || value.isEmpty() ) {
            return null;
        }

        try {
            DefaultProfiles prof = DB_Util.getDefaults();
            return prof;
        } catch (CliInternalException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean getAllPossibleValues(final List<Completion> completions, final Class<?> requiredType, final String existingData, final String optionContext, final MethodTarget target) {

        return false;

    }
}
