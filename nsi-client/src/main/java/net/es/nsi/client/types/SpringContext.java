package net.es.nsi.client.types;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public interface SpringContext {

    public ApplicationContext getContext() ;

    public ApplicationContext initContext(String filename);

}
