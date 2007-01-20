package net.es.oscars.aaa;

import java.util.*;

import org.hibernate.*;
import net.es.oscars.database.GenericHibernateDAO;

/**
 * UserDAO is the data access object for the aaa.users table.
 * Some methods in this class require authorization.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class UserDAO extends GenericHibernateDAO<User, Integer> {

    /**
     * Finds a system user based on their login name.
     *
     * @param login A user's login name
     * @return user The corresponding user instance, if any
     */
    public User query(String login) {
        User user = (User) this.queryByParam("login", login);
        return user;
    }

    /**
     * Creates a system user by persisting information into the users table.
     *
     * @param user A user instance to persist
     */
    public void create(User user) {
        this.makePersistent(user);
    }

    /**
     * Removes a reservation system user.
     *
     * @param user A user instance to remove from the database
     */
    public void remove(User user) {
        this.makeTransient(user);
    }

    /**
     * List all users.
     *
     * @return A list of users
     */
    public List<User> list() {
        List<User> users = null;

        String hsql = "from User";
        users = this.getSession().createQuery(hsql).list();
        return users;
    }

    /**
     * Persists requested modification to a user's profile.
     *
     * @param user A user instance with the modified parameters
     */
    public void update(User user) {
        this.makePersistent(user);
    }

    /**
     * Makes sure user corresponding to DN has an entry in the db.
     *
     * @param certSubject A string containing the subject from the certificate
     * @return user the associated user instance, if any.
     */
    public User fromDN(String certSubject) {
        User user = (User) this.queryByParam("certSubject", certSubject);
        return user;
    }

    /**
     * Currently a noop.
     */
    public boolean isAuthenticated(String userName) {
        return false;
    }
}
