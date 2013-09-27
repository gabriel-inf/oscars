package net.es.nsi.cli.db;

import net.es.nsi.cli.config.*;
import net.es.nsi.cli.core.CliInternalException;
import net.es.oscars.nsibridge.config.SpringContext;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class DB_Util {
    private static void prepare() {
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getEntityManagerFactory().getCache().evictAll();

    }

    public static void save(Object prof) {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        em.persist(prof);
        em.getTransaction().commit();
    }
    public static void delete(Object prof) {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        em.remove(prof);
        em.getTransaction().commit();
    }

    public static ResvProfile getSpringResvProfile(String name) {
        ApplicationContext ax = SpringContext.getInstance().getContext();
        Map<String, ResvProfile> beans = ax.getBeansOfType(ResvProfile.class);
        for (ResvProfile prof : beans.values()) {
            if (prof.getName().equals(name)) {
                return prof;
            }
        }
        return null;
    }

    public static ProviderProfile getSpringProvProfile(String name)  {

        ApplicationContext ax = SpringContext.getInstance().getContext();
        Map<String, ProviderProfile> beans = ax.getBeansOfType(ProviderProfile.class);
        for (ProviderProfile prof : beans.values()) {
            if (prof.getName().equals(name)) {
                return prof;
            }
        }
        return null;
    }

    public static RequesterProfile getSpringRequesterProfile(String name) {

        ApplicationContext ax = SpringContext.getInstance().getContext();
        Map<String, RequesterProfile> beans = ax.getBeansOfType(RequesterProfile.class);
        for (RequesterProfile prof : beans.values()) {
            if (prof.getName().equals(name)) {
                return prof;
            }
        }
        return null;
    }


    public static ResvProfile getResvProfile(String name) throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        String query = "SELECT c FROM ResvProfile c WHERE c.name  = '"+name+"'";
        em.getTransaction().begin();
        List<ResvProfile> recordList = em.createQuery(query, ResvProfile.class).getResultList();
        em.getTransaction().commit();

        if (recordList.size() == 0) {
            return null;
        } else if (recordList.size() > 1) {
            throw new CliInternalException("internal error: found multiple ResvProfiles ("+recordList.size()+") with name: "+name);
        } else {
            em.refresh(recordList.get(0));
            return recordList.get(0);
        }
    }
    public static List<ResvProfile> getResvProfiles() throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        String query = "SELECT c FROM ResvProfile c";

        em.getTransaction().begin();
        List<ResvProfile> recordList = em.createQuery(query, ResvProfile.class).getResultList();
        em.getTransaction().commit();
        return recordList;
    }

    public static ResvProfile copyResvProfile(ResvProfile prof, String inName) throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        ResvProfile newProf = new ResvProfile();
        try {
            BeanUtils.copyProperties(newProf, prof);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            throw new CliInternalException(ex.getMessage());
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw new CliInternalException(ex.getMessage());
        }
        newProf.setName(inName);
        newProf.setId(null);
        em.getTransaction().begin();
        em.persist(newProf);
        em.getTransaction().commit();

        return newProf;
    }





    public static ProviderProfile getProviderProfile(String name) throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        String query = "SELECT c FROM ProviderProfile c WHERE c.name  = '"+name+"'";

        em.getTransaction().begin();
        List<ProviderProfile> recordList = em.createQuery(query, ProviderProfile.class).getResultList();
        em.getTransaction().commit();

        if (recordList.size() == 0) {
            return null;
        } else if (recordList.size() > 1) {
            throw new CliInternalException("internal error: found multiple ProviderProfiles ("+recordList.size()+") with name: "+name);
        } else {
            em.refresh(recordList.get(0));
            return recordList.get(0);
        }
    }
    public static List<ProviderProfile> getProviderProfiles() throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        String query = "SELECT c FROM ProviderProfile c";
        List<ProviderProfile> recordList = em.createQuery(query, ProviderProfile.class).getResultList();
        em.getTransaction().commit();
        return recordList;
    }


    public static ProviderProfile copyProviderProfile(ProviderProfile prof, String inName) throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        ProviderProfile newProf = new ProviderProfile();
        ProviderServer  newPs = new ProviderServer();
        NsiAuth         newAuth = new NsiAuth();

        try {
            BeanUtils.copyProperties(newProf,   prof);
            BeanUtils.copyProperties(newPs,     prof.getProviderServer());
            BeanUtils.copyProperties(newAuth,   prof.getProviderServer().getAuth());
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            throw new CliInternalException(ex.getMessage());
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw new CliInternalException(ex.getMessage());
        }
        newProf.setProviderServer(newPs);
        newProf.setName(inName);
        newPs.setAuth(newAuth);

        newProf.setId(null);
        newPs.setId(null);
        newAuth.setId(null);


        try {
            em.getTransaction().begin();
            em.persist(newProf);
            em.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return newProf;
    }


    public static DefaultProfiles getDefaults() throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        String query = "SELECT c FROM DefaultProfiles c";
        em.getTransaction().begin();
        List<DefaultProfiles> recordList = em.createQuery(query, DefaultProfiles.class).getResultList();
        em.getTransaction().commit();

        if (recordList.size() == 0) {
            return null;
        } else if (recordList.size() > 1) {
            throw new CliInternalException("internal error: found multiple DefaultProfiles ("+recordList.size()+")");
        } else {
            em.refresh(recordList.get(0));
            return recordList.get(0);
        }
    }





    public static RequesterProfile getRequesterProfile(String name) throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        String query = "SELECT c FROM RequesterProfile c WHERE c.name  = '"+name+"'";

        em.getTransaction().begin();
        List<RequesterProfile> recordList = em.createQuery(query, RequesterProfile.class).getResultList();
        em.getTransaction().commit();

        if (recordList.size() == 0) {
            return null;
        } else if (recordList.size() > 1) {
            throw new CliInternalException("internal error: found multiple RequesterProfile ("+recordList.size()+") with name: "+name);
        } else {
            em.refresh(recordList.get(0));
            return recordList.get(0);
        }
    }
    public static List<RequesterProfile> getRequesterProfiles() throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        String query = "SELECT c FROM RequesterProfile c";
        em.getTransaction().begin();
        List<RequesterProfile> recordList = em.createQuery(query, RequesterProfile.class).getResultList();
        em.getTransaction().commit();
        return recordList;
    }

    public static RequesterProfile copyRequesterProfile(RequesterProfile prof, String inName) throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();

        RequesterProfile newProf = new RequesterProfile();
        try {
            BeanUtils.copyProperties(newProf, prof);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            throw new CliInternalException(ex.getMessage());
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw new CliInternalException(ex.getMessage());
        }
        newProf.setName(inName);
        newProf.setId(null);
        em.getTransaction().begin();
        em.persist(newProf);
        em.getTransaction().commit();

        return newProf;
    }



}
