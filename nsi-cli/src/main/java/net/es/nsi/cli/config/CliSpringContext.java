package net.es.nsi.cli.config;

import net.es.nsi.client.types.SpringContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class CliSpringContext implements SpringContext {
    private static CliSpringContext instance;
    public static CliSpringContext getInstance() {
        if (instance == null) {
            instance = new CliSpringContext();
        }
        return instance;
    }
    private CliSpringContext() {}

    private ApplicationContext context;

    public ApplicationContext getContext() {
        return context;
    }
    public ApplicationContext initContext(String filename) {
        context = new FileSystemXmlApplicationContext(filename);
        return context;
    }

}
