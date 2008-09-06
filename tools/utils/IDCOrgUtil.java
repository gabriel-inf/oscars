import java.util.*;
import java.io.*;

import net.es.oscars.aaa.*;
import net.es.oscars.database.*;

import org.apache.log4j.*;
import org.hibernate.*;

/**
 * IDCOrgUtil is a command-line client for adding organizations to the database
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCOrgUtil extends IDCCmdUtil{
    
    public IDCOrgUtil(){
        this.log = Logger.getLogger(this.getClass());
        this.dbname = "aaa";
    }
    
     public String addOrg(){
        return this.addOrg(true);
     }
    
    /**
     * Main logic that adds organization to the database
     *
     */
    public String addOrg(boolean initDb){
        Scanner in = new Scanner(System.in);
        String name = readInput(in, "Organization Name", "", true);

        /* Init database */
        if(initDb){
            Initializer initializer = new Initializer();
            ArrayList<String> dbnames = new ArrayList<String>();
            dbnames.add(this.dbname);
            initializer.initDatabase(dbnames);
        }
        try { // if there is a unique index defined on the institution name 
              // hibernate will throw an exception if you try to add a duplicate name
            Session aaa =
                HibernateUtil.getSessionFactory("aaa").getCurrentSession();
            aaa.beginTransaction();
            Institution org = new Institution();
            org.setName(name);
            aaa.save(org);
            aaa.getTransaction().commit();
        } catch (HibernateException e){
            e.printStackTrace();
            System.out.println("caught " + e.getClass().getName() + ": " + e.getMessage());
            System.out.println( name + " not added.");
            return null;
        }
        
        System.out.println("New organization '" + name + "' added.");
        return name;
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
        Institution org = this.selectInstitution(in, "organization to delete");
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
    
    public static void main(String[] args){
        IDCOrgUtil util = new IDCOrgUtil();
        if(args[0] != null && args[0].equals("remove")){
            util.removeOrg();
        }else{
            util.addOrg();
        }
    }
}