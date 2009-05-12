package net.es.oscars.client.improved.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.es.oscars.client.improved.ConfigHelper;

public class ListOutputterFactory {

    public static ListOutputterInterface getOutputter(String className) {
        if (className.equals("ListConsoleOutputter")) {
            return new ListConsoleOutputter();
        } else if (className.equals("ListMaintDbOutputter")) {
            return new ListMaintDbOutputter();
        } else if (className.equals("ListDotOutputter")) {
            return new ListDotOutputter();
        }
        return null;
    }

    public static List<ListOutputterInterface> getConfiguredOutputters() {
        return getConfiguredOutputters(null);
    }

    @SuppressWarnings("unchecked")
    public static List<ListOutputterInterface> getConfiguredOutputters(String configFile) {
        if (configFile == null) {
            configFile = ListClient.DEFAULT_CONFIG_FILE;
        }

        Map config = ConfigHelper.getInstance().getConfiguration(configFile);

        ArrayList<ListOutputterInterface> outputters = new ArrayList<ListOutputterInterface>();
        Map output = (Map) config.get("output");
        Iterator it = output.keySet().iterator();
        while (it.hasNext()) {
            String name = (String) it.next();

            ListOutputterInterface outputter = getOutputter(name);
            if (outputter != null) {
                outputters.add(outputter);
            }
        }



        return outputters;
    }

}
