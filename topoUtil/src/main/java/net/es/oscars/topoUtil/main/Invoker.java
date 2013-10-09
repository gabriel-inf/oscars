package net.es.oscars.topoUtil.main;
import freemarker.template.TemplateException;
import net.es.oscars.topoUtil.beans.Network;
import net.es.oscars.topoUtil.beans.VlanFormatException;
import net.es.oscars.topoUtil.config.JsonNetworkConfigProvider;
import net.es.oscars.topoUtil.config.SpringContext;
import net.es.oscars.topoUtil.input.SpecConverter;
import net.es.oscars.topoUtil.output.OscarsOutputter;
import net.es.oscars.topoUtil.output.VizJsonOutputter;
import net.es.oscars.topoUtil.util.IfceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.List;

public class Invoker {
    static final Logger LOG = LoggerFactory.getLogger(Invoker.class);
    public static void main(String[] args) {
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.initContext("config/beans.xml");



        JsonNetworkConfigProvider jnp = ax.getBean("networkConfigProvider", JsonNetworkConfigProvider.class);
        try {
            jnp.loadConfig();
            Network net = SpecConverter.fromSpec(jnp.getNetworkSpec());

            String oscars = OscarsOutputter.outputOscars(net);
            System.out.println(oscars);



            String viz = VizJsonOutputter.outputViz(net);
            System.out.println(viz);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (VlanFormatException e) {
            e.printStackTrace();
        }


    }

}
