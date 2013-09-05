package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.nsa.NSAStubConfig;
import net.es.oscars.nsibridge.prov.RequesterPortHolder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class PrepareCallbackTask extends Task  {
    private static final Logger log = Logger.getLogger(PrepareCallbackTask.class);
    private String replyTo;

    public PrepareCallbackTask(String replyTo) {
        this.scope = UUID.randomUUID().toString();
        this.replyTo = replyTo;
    }


    public void onRun() throws TaskException {
        log.debug(this.id+" starting");
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();
        NSAStubConfig stubConfig = ax.getBean("nsaStubConfig", NSAStubConfig.class);

        if (!stubConfig.isPerformCallback()) {
            log.debug("not preparing callback - this NSA is a stub");
            this.onSuccess();
            return;
        }


        URL url;
        try {
            url = new URL(replyTo);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        ConnectionRequesterPort port = RequesterPortHolder.getInstance().getPort(url);

        log.debug(this.id+" starting");

        this.onSuccess();
    }

}