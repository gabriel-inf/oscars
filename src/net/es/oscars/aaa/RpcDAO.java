package net.es.oscars.aaa;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * RpcDAO is the data access object for the aaa.rpcs table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class RpcDAO extends GenericHibernateDAO<Rpc, Integer> {

    public RpcDAO(String dbname) {
        this.setDatabase(dbname);
    }
}
