package net.es.nsi.cli.db;

import net.es.nsi.cli.config.DefaultProfiles;
import net.es.nsi.cli.config.RequesterProfile;
import net.es.nsi.cli.config.ProviderProfile;
import net.es.nsi.cli.config.ResvProfile;
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
        em.getTransaction().begin();
        String query = "SELECT c FROM ResvProfile c WHERE c.name  = '"+name+"'";
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
        em.getTransaction().begin();
        String query = "SELECT c FROM ResvProfile c";
        List<ResvProfile> recordList = em.createQuery(query, ResvProfile.class).getResultList();
        em.getTransaction().commit();
        return recordList;
    }

    public static ResvProfile copyResvProfile(ResvProfile prof, String inName) throws CliInternalException {
        String name = prof.getName();
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        if (em.contains(prof)) {
            em.detach(prof);
            prof.setName(inName);
            prof.setId(null);
            em.getTransaction().begin();
            em.merge(prof);
            em.getTransaction().commit();
            getResvProfile(name);
            return prof;
        } else {
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
    }





    public static ProviderProfile getProviderProfile(String name) throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        String query = "SELECT c FROM ProviderProfile c WHERE c.name  = '"+name+"'";
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
        String name = prof.getName();
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        if (em.contains(prof)) {
            em.detach(prof);
            prof.setName(inName);
            prof.setId(null);
            prof.getProviderServer().setId(null);
            prof.getProviderServer().getAuth().setId(null);
            em.getTransaction().begin();
            em.merge(prof);
            em.getTransaction().commit();
            getProviderProfile(name);
            return prof;
        } else {
            ProviderProfile newProf = new ProviderProfile();
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
            newProf.getProviderServer().setId(null);
            newProf.getProviderServer().getAuth().setId(null);

            em.getTransaction().begin();
            em.persist(newProf);
            em.getTransaction().commit();

            return newProf;
        }
    }


    public static DefaultProfiles getDefaults() throws CliInternalException {
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        em.getTransaction().begin();
        String query = "SELECT c FROM DefaultProfiles c";
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
        em.getTransaction().begin();
        String query = "SELECT c FROM RequesterProfile c WHERE c.name  = '"+name+"'";
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
        em.getTransaction().begin();
        String query = "SELECT c FROM RequesterProfile c";
        List<RequesterProfile> recordList = em.createQuery(query, RequesterProfile.class).getResultList();
        em.getTransaction().commit();
        return recordList;
    }

    public static RequesterProfile copyRequesterProfile(RequesterProfile prof, String inName) throws CliInternalException {
        String name = prof.getName();
        prepare();
        EntityManager em = PersistenceHolder.getEntityManager();
        if (em.contains(prof)) {
            em.detach(prof);
            prof.setName(inName);
            prof.setId(null);
            em.getTransaction().begin();
            em.merge(prof);
            em.getTransaction().commit();
            getResvProfile(name);
            return prof;
        } else {
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



}
