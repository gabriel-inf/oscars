package net.es.oscars.topoUtil.output;

import freemarker.template.TemplateException;
import net.es.oscars.topoUtil.beans.Network;
import net.es.oscars.topoUtil.config.SpringContext;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Map;

public class OutputterRunner {
    public void executeOutput(Network net) {
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();
        Map<String, Outputter> outputters = ax.getBeansOfType(Outputter.class);
        for (Outputter out : outputters.values()) {
            try {
                out.output(net);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TemplateException e) {
                e.printStackTrace();
            }
        }
    }

}
