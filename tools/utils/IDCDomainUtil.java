import java.util.*;
import java.io.*;

import net.es.oscars.bss.topology.*;
import net.es.oscars.database.*;

import org.apache.log4j.*;
import org.hibernate.*;

/**
 * IDCDomainUtil is a command-line client for adding domains to the database
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCDomainUtil extends IDCCmdUtil{
    
    public IDCDomainUtil(){
        this.log = Logger.getLogger(this.getClass());
        this.dbname = "bss";
    }
    
    /**
     * Main logic that adds organization to the database
     *
     */
    public void addDomain(){
        Scanner in = new Scanner(System.in);

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session bss =
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        bss.beginTransaction();
        Domain domain = new Domain();
        domain.setTopologyIdent(readInput(in, "Topology Identifier (i.e. mydomain.net)", "", true));
        domain.setUrl(readInput(in, "IDC URL", "", true));
        domain.setName(readInput(in, "Descriptive Name (for display purposes)", "", true));
        domain.setAbbrev(readInput(in, "Abbreviated Name (for display purposes)", "", true));
        System.out.print("Is this your IDC's local domain? [y/n] ");
        String ans = in.next();
        if(ans.toLowerCase().startsWith("y")){
            domain.setLocal(true);
        }else{
            domain.setLocal(false);
        }
        bss.save(domain);
        bss.getTransaction().commit();
        
        System.out.println("New domain '" + domain.getTopologyIdent() + "' added.");
    }
    
    /**
     * Main logic that removes domains from the database
     *
     */
    public void removeDomain(){
        Scanner in = new Scanner(System.in);

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session bss =
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        bss.beginTransaction();
        Domain domain = this.selectDomain(in, "domain to delete");
        System.out.print("Are you sure you want to delete '" + 
                            domain.getTopologyIdent() + "'? [y/n] ");
        String ans = in.next();
        
        if(ans.toLowerCase().startsWith("y")){
            domain.setPaths(null);
            bss.delete(domain);
            System.out.println("Domain '" + domain.getTopologyIdent() + "' deleted.");
        }else{
            System.out.println("Operation cancelled. No domain deleted.");
        }
       
        bss.getTransaction().commit();
    }
    
    /**
     * Prints the current list of domains in the database and allows the
     * user to choose one
     *
     * @param in the Scanner to use for accepting input
     * @return the selected Domain
     */
    protected Domain selectDomain(Scanner in, String label){
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        List<Domain> domains = domainDAO.list();
        int i = 1;
        
        System.out.println();
        for(Domain domain : domains){
            System.out.println(i + ". " + domain.getTopologyIdent());
            i++;
        }
        
        System.out.print("Select the " + label + " (by number): ");
        int n = in.nextInt();
        in.nextLine();
        
        if(n <= 0 || n > domains.size()){
            System.err.println("Invalid domain number '" +n + "' entered");
            System.exit(0);
        }
        
        return domains.get(n-1);
    }
    
    public static void main(String[] args){
        IDCDomainUtil util = new IDCDomainUtil();
        if(args[0] != null && args[0].equals("remove")){
            util.removeDomain();
        }else{
            util.addDomain();
        }
    }
}