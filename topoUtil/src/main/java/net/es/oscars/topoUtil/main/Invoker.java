package net.es.oscars.topoUtil.main;
import freemarker.template.TemplateException;
import net.es.oscars.topoUtil.config.JsonNetworkConfigProvider;
import net.es.oscars.topoUtil.config.SpringContext;
import net.es.oscars.topoUtil.output.OscarsOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public class Invoker {
    static final Logger LOG = LoggerFactory.getLogger(Invoker.class);
    public static void main(String[] args) {
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.initContext("config/beans.xml");


        JsonNetworkConfigProvider jnp = ax.getBean("networkConfigProvider", JsonNetworkConfigProvider.class);
        try {
            jnp.loadConfig();
            String oscars = OscarsOutputter.outputOscars(jnp.getNetworkSpec());


            //String viz = VizJsonOutputter.outputViz(n);
            // System.out.println(viz);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }


    }

}
