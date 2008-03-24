import java.util.*;
import java.io.*;

import net.es.oscars.aaa.*;
import net.es.oscars.database.*;

import org.apache.log4j.*;
import org.hibernate.*;

/**
 * IDCUserAddUtil is a command-line client for adding users to the database
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCOrgAddUtil{
    private String dbname;
    private Logger log;
    
    public IDCOrgAddUtil(){
        this.log = Logger.getLogger(this.getClass());
        this.dbname = "aaa";
    }
    
    /**
     * Main logic that adds organization to the database
     *
     */
    public void addOrg(){
        Scanner in = new Scanner(System.in);
        String name = readInput(in, "Organization Name", "", true);

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        Institution org = new Institution();
        org.setName(name);
        aaa.save(org);
        aaa.getTransaction().commit();
        
        System.out.println("New organization '" + name + "' added.");
    }
    
    /**
     * Main logic that removes organization from the database
     *
     */
    public void removeOrg(){
        Scanner in = new Scanner(System.in);

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        Institution org = this.selectInstitution(in);
        System.out.print("Are you sure you want to delete '" + 
                            org.getName() + "'? [y/n] ");
        String ans = in.next();
        
        if(ans.toLowerCase().startsWith("y")){
            aaa.delete(org);
            System.out.println("Organization '" + org.getName() + "' deleted.");
        }else{
            System.out.println("Operation cancelled. No organization deleted.");
        }
       
        aaa.getTransaction().commit();
    }
    
    /**
     * Method to read in user input strings
     *
     * @param in a Scanner used to accept input
     * @param label a String describing the requested input to the user
     * @param defaultVal the default value to assign if no input provided
     * @param req boolean indicating whether this field is required
     * @return the String input by the user
     */
    private String readInput(Scanner in, String label, String defaultVal, boolean req){
        System.out.print(label + (req?"*":""));// + " [" + defaultVal + "]: ");
        System.out.print(": ");
        String input = in.nextLine().trim();
        
        if(input.equals("") && (!defaultVal.equals(""))){
            input = defaultVal;
        }else if(input.equals("") && defaultVal.equals("") && req){
            System.err.println("The field '" + label + "' is required.");
            System.exit(0);
        }else if(input.equals("")){
            return null;
        }
        
        return input;
    }
    
    /**
     * Prints the current list of institutions in the database and allows the
     * user to choose one
     *
     * @param in the Scanner to use for accepting input
     * @return the selected Institution
     */
    private Institution selectInstitution(Scanner in){
        InstitutionDAO instDAO = new InstitutionDAO(this.dbname);
        List<Institution> institutions = instDAO.list();
        int i = 1;
        
        System.out.println();
        for(Institution inst : institutions){
            System.out.println(i + ". " + inst.getName());
            i++;
        }
        
        System.out.print("Select the organization to delete(by number): ");
        int n = in.nextInt();
        in.nextLine();
        
        if(n < 0 || n > institutions.size()){
            System.err.println("Invalid organization number '" +n + "' entered");
            System.exit(0);
        }
        
        return institutions.get(n-1);
    }
    
    public static void main(String[] args){
        IDCOrgAddUtil util = new IDCOrgAddUtil();
        if(args[0] != null && args[0].equals("remove")){
            util.removeOrg();
        }else{
            util.addOrg();
        }
    }
}