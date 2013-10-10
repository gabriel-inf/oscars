package net.es.oscars.topoUtil.main;
import freemarker.template.TemplateException;
import net.es.oscars.topoUtil.beans.Network;
import net.es.oscars.topoUtil.beans.VlanFormatException;
import net.es.oscars.topoUtil.config.NetworkSpecProvider;
import net.es.oscars.topoUtil.config.SpringContext;
import net.es.oscars.topoUtil.input.SpecConverter;
import net.es.oscars.topoUtil.output.OscarsOutputter;
import net.es.oscars.topoUtil.output.OutputterRunner;
import net.es.oscars.topoUtil.output.VizJsonOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public class Invoker {
    static final Logger LOG = LoggerFactory.getLogger(Invoker.class);
    public static void main(String[] args) {
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.initContext("config/beans.xml");



        NetworkSpecProvider jnp = ax.getBean("networkSpecProvider", NetworkSpecProvider.class);
        try {
            jnp.loadConfig();
            Network net = SpecConverter.fromSpec(jnp.getNetworkSpec());
            OutputterRunner runner = ax.getBean("outputterRunner", OutputterRunner.class);
            runner.executeOutput(net);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (VlanFormatException e) {
            e.printStackTrace();
        }


    }

}
