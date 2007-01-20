package net.es.oscars.database;

import java.util.*;

import org.hibernate.*;
import org.hibernate.cfg.*;

import net.es.oscars.PropHandler;

/**
 * HibernateUtil is adapted from the tutorial in the Hibernate 3.1
 * distribution.
 * Currently it maintains an array of session factories: one
 * for the aaa database, and one for the bss database.
 *
 * @author (besides Hibernate developers) dwrobertson@lbl.gov
 */
public class HibernateUtil {


    private static final Map<String, SessionFactory> sessionFactories =
        new HashMap<String,SessionFactory>();
    
    /**
     * Called from OSCARSSkeleton and test setup. When running in axis, we need
     *     to set the classLoader in order for it to find the .xml files.
     *
     * @param CL classloader for the calling class
     */
    public static void initSessionFactories(ClassLoader CL) {
        try {
            PropHandler propHandler =
                new PropHandler("/oscars.config/properties/oscars.properties");
            if (propHandler == null) {
                throw new ExceptionInInitializerError(
                            "Could not find properties file");
            }
            Properties props =
                propHandler.getPropertyGroup("hibernate", false);
            Configuration cfg = new Configuration();
            cfg.setProperties(props);
            
            ClassLoader clsave =
                    Thread.currentThread().getContextClassLoader();
            //Thread.currentThread().setContextClassLoader(CL);

            putSessionFactory("aaa",
                    cfg.configure("aaa.cfg.xml").buildSessionFactory());
            putSessionFactory("bss",
                    cfg.configure("bss.cfg.xml").buildSessionFactory());
            //Thread.currentThread().setContextClassLoader(clsave);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex.getMessage());
        }
    }

    public static SessionFactory closeSessionFactory(String factoryName) {
        return sessionFactories.remove(factoryName);
    }

    public static SessionFactory getSessionFactory(String factoryName) {
        return sessionFactories.get(factoryName);
    }

    public static void putSessionFactory (String name, SessionFactory SF) {
        sessionFactories.put(name, SF);
    }
}
