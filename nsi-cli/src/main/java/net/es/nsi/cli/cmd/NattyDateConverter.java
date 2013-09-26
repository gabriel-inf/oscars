package net.es.nsi.cli.cmd;



import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;

public class NattyDateConverter implements Converter<Date> {


    public NattyDateConverter() {
    }


    public Date convertFromText(final String value, final Class<?> requiredType, final String optionContext) {
        Date result = null;
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(value);
        for (DateGroup group : groups) {
            List<Date> dates = group.getDates();
            result = dates.get(0);
        }
        if (result == null) {
            throw new IllegalArgumentException("Could not parse a date: " + value);
        }
        return result;
    }

    public boolean getAllPossibleValues(final List<Completion> completions, final Class<?> requiredType, final String existingData, final String optionContext, final MethodTarget target) {
        return false;
    }

    public boolean supports(final Class<?> requiredType, final String optionContext) {
        return Date.class.isAssignableFrom(requiredType);
    }
}