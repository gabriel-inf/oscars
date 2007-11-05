import java.util.*;
import java.io.*;
import java.lang.Throwable;

import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationDAO;

/**
 * DeleteReservation deletes a reservation with the given
 * global reservation id, and all its associated information.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */

public class DeleteReservation {

    // time in seconds to look into the future for reservations
    private static final Integer reservationInterval = 300;

    // shutdown hook delay time in seconds
    private static final int shutdownTime = 2;     

    public static void main (String[] args) {

        String gri = null;

        for (int i=0; i < args.length; i++) {
            if (args[i].equals("-help") ||
                args[i].equals("-h")) {
                System.out.println("usage: deleteResv.sh -gri id");
                System.exit(0);
            } else if (args[i].equals("-gri")) {
                gri = args[i+1];
            }
        }
        if (gri == null) {
            System.out.println("usage: deleteResv.sh -gri id");
            System.exit(0);
        }

        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");
        initializer.initDatabase(dbnames);
        Session session =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        session.beginTransaction();
        ReservationDAO dao = new ReservationDAO("bss");
        Reservation resv = dao.queryByParam("globalReservationId", gri);
        // cascading delete gets rid of all associated data
        dao.remove(resv);
        session.getTransaction().commit();
    }
}
