import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.cert.*;
import java.security.Principal;
import java.util.Properties;
import java.util.ArrayList;

import org.hibernate.*;

import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.*;

public class ModifyCertificate {

    /**
     * Command to update values in users table for the given user login.
     * 
     * @param args
     *            args[0] is user login args[1] is filename of X.509 certificate
     *            in pem format
     */

    private SessionFactory sessionFac;

    private UserManager mgr;

    public static void main(String[] args) {

        String loginName = null;
        String password = null;
        String certFile = null;

        if (args.length < 2) {
            System.out
                    .println("usage:  ModifyCertificate -l loginId  [-p <password>] [ -c <certFile>]");
            System.exit(1);
        }

        int i = 0;
        while (i < args.length) {
            if (args[i].equals("-l")) {
                loginName = args[i + 1];
            } else if (args[i].equals("-p")) {
                password = args[i + 1];
            } else if (args[i].equals("-c")) {
                certFile = args[i + 1];
            }
            i = i + 2;
        }
        if (loginName == null) {
            System.out.println("Must enter a login name");
            System.out
                    .println("usage:  ModifyCertificate -l loginId  [-p <password>] [ -c <certFile>]");
            System.exit(1);
        }
        ModifyCertificate mc = new ModifyCertificate();
        if (certFile != null || password != null) {
            try {
                mc.setUp();
                User user = mc.mgr.query(loginName);
                if (user == null) {
                    System.out.println("user not found: " + loginName);
                    System.exit(1);
                }
                if (certFile != null) {
                    FileInputStream fis = new FileInputStream(certFile);
                    DataInputStream dis = new DataInputStream(fis);

                    CertificateFactory cf = CertificateFactory
                            .getInstance("X.509");
                    X509Certificate cert = (X509Certificate) cf
                            .generateCertificate(mc.filter(dis));
                    System.out.println(" subject name is: "
                            + cert.getSubjectDN().getName());
                    System.out.println("issuer DN is; "
                            + cert.getIssuerDN().getName());

                    user.setCertSubject(cert.getSubjectDN().getName());
                }
                if (password != null) {
                    user.setPassword(password);
                }

                mc.mgr.update(user, false);

            } catch (AAAException e) {
                System.out.println("caught AAAexception: " + e.getMessage());
            } catch (FileNotFoundException e) {
                System.out.println("could not open " + args[1]);
            } catch (CertificateException e) {
                System.out.println("CertificateException: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IoException: " + e.getMessage());
            }
        }
    }

    /**
     * Removes all extra stuff.
     * 
     * Works only for pem encoding.
     */
    private ByteArrayInputStream filter(InputStream in) throws IOException {
        InputStreamReader isr = new InputStreamReader(in);
        LineNumberReader lnr = new LineNumberReader(isr);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(baos);
        PrintWriter pw = new PrintWriter(osw);

        String line = lnr.readLine();
        while (line != null) {
            if (line.equals("-----BEGIN CERTIFICATE-----")) {
                pw.println(line);
                line = lnr.readLine();

                while (line != null) {
                    pw.println(line);

                    if (line.equals("-----END CERTIFICATE-----")) {
                        line = lnr.readLine();
                        break;
                    }

                    line = lnr.readLine();
                }
            } else {
                line = lnr.readLine();
            }
        }

        osw.flush();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    protected void setUp() {
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add("aaa");
        initializer.initDatabase(dbnames);
        this.mgr = new UserManager("aaa");
        this.sessionFac = HibernateUtil.getSessionFactory("aaa");
        this.sessionFac.getCurrentSession().beginTransaction();
    }
}
