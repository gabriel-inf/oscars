import java.util.*;
import java.io.*;

import net.es.oscars.*;
import net.es.oscars.aaa.*;
import net.es.oscars.database.*;
import net.es.oscars.servlets.Utils;

import org.apache.log4j.*;
import org.hibernate.*;

/**
 * IDCUserUtil is a command-line client for adding/deleting users
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCUserUtil extends IDCCmdUtil{
    private Properties props;
    
    public IDCUserUtil(){
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("aaa", true);
        this.dbname = "aaa"; 
    }
    
    /**
     * Main logic that adds user to the database
     *
     */
    public void addUser(){
        Scanner in = new Scanner(System.in);
        String input = null;
        EraserThread et = new EraserThread();
        Thread mask = new Thread(et);
        Utils utils = new Utils();
        ArrayList<UserAttribute> userAttrs = new ArrayList<UserAttribute>();
        String salt = this.props.getProperty("salt");

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        User user = new User();
        System.out.println("* indicates a required field");
        /* Get login name */
        user.setLogin(this.readInput(in, "Login", "", true));
        
        /* Get password with mask */
        mask.start();
        String pwd1 =  this.readInput(in, "Password", "", true);
        String pwd2 =  this.readInput(in, "Confirm Password", "", true);
        et.stopMasking();
        try{
            String password = utils.checkPassword(pwd1, pwd2);
            pwd1= Jcrypt.crypt(salt, password);
        }catch(AAAException e){
            System.err.println(e.getMessage());
            System.exit(0);
        }
        user.setPassword(pwd1);
        
        /* Get name and contact info */
        user.setFirstName(this.readInput(in, "\010First Name", "", true));
        user.setLastName(this.readInput(in, "Last Name", "", true));
        input = this.readInput(in, "Cert Subject", "", false);
        if (input != null) {
            try{
                input = utils.checkDN(input);
            }catch(AAAException e){
                System.err.println(e.getMessage());
                System.exit(0);
            }
        }else { input = ""; }
        user.setCertSubject(input);
        input = this.readInput(in, "Cert Issuer", "", false);
        if (input != null) {
            try{
                input = utils.checkDN(input);
            }catch(AAAException e){
                System.err.println(e.getMessage());
                System.exit(0);
            }
        }else { input = ""; }
        user.setCertIssuer(input);
        user.setInstitution(this.selectInstitution(in, "user's organization"));
        userAttrs = this.selectRoles(in);
        user.setDescription(this.readInput(in, "Personal Description", "", false));
        user.setEmailPrimary(this.readInput(in, "Email(Primary)", "", true));
        user.setEmailSecondary(this.readInput(in, "Email(Secondary)", "", false));
        user.setPhonePrimary(this.readInput(in, "Phone(Primary)", "", true));
        user.setPhoneSecondary(this.readInput(in, "Phone(Secondary)", "", false));
        
        /* Save the user and attributes */
        aaa.save(user);
        for(UserAttribute userAttr : userAttrs){
            userAttr.setUserId(user.getId());
            aaa.save(userAttr);
        }
        aaa.getTransaction().commit();
        
        System.out.println("New user '" + user.getLogin() + "' added.");
    }
    
    /**
     * Main llogic for deleting a user from the database
     */
    public void removeUser(){
        Scanner in = new Scanner(System.in);

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        User user = this.selectUser(in);
        System.out.print("Are you sure you want to delete '" + 
                            user.getLogin() + "'? [y/n] ");
        String ans = in.next();
        
        if(ans.toLowerCase().startsWith("y")){
            aaa.delete(user);
            System.out.println("User '" + user.getLogin() + "' deleted.");
        }else{
            System.out.println("Operation cancelled. No user deleted.");
        }
       
        aaa.getTransaction().commit();
    }
    
    
    /**
     * Prints the current list of users in the database and allows the
     * user to choose one
     *
     * @param in the Scanner to use for accepting input
     * @return the selected Institution
     */
    private User selectUser(Scanner in){
        UserDAO userDAO = new UserDAO(this.dbname);
        List<User> users = userDAO.list();
        int i = 1;
        
        System.out.println();
        for(User user : users){
            System.out.println(i + ". " + user.getLogin());
            i++;
        }
        
        System.out.print("Select the user to delete (by number): ");
        int n = in.nextInt();
        in.nextLine();
        
        if(n <= 0 || n > users.size()){
            System.err.println("Invalid user number '" +n + "' entered");
            System.exit(0);
        }
        
        return users.get(n-1);
    }
    
    /**
     * Prints the current list of attributes in the database and allows the
     * user to choose one or more from the list
     *
     * @param in the Scanner to use for accepting input
     * @return the selected UserAttributes (with userid set to null)
     */
    private ArrayList<UserAttribute> selectRoles(Scanner in){
        AttributeDAO attrDAO = new AttributeDAO(this.dbname);
        List<Attribute> attrs = attrDAO.list();
        ArrayList<UserAttribute> userAttrs = new ArrayList<UserAttribute>();
        int i = 1;
        
        System.out.println();
        for(Attribute attr : attrs){
            System.out.println(i + ". " + attr.getName());
            i++;
        }
        
        System.out.print("Select the user's role(s) (numbers separated by spaces): ");
        String line = in.nextLine();
        StringTokenizer st = new StringTokenizer(line, " ");
        while(st.hasMoreTokens()){
            int n = 0;
            try{
                n = Integer.parseInt(st.nextToken());
            }catch(Exception e){
                System.out.println("Non-numeric value entered in role list");
                System.exit(0);
            }
            if(n <= 0 || n > attrs.size()){
                System.err.println("Invalid role number '" + n + "' entered");
                System.exit(0);
            }
            UserAttribute userAttr = new UserAttribute();
            userAttr.setAttributeId(attrs.get(n-1).getId());
            userAttrs.add(userAttr);
        }
        
        return userAttrs;
    }
    
    /**
     * Private class that masks password input
     */
    private class EraserThread implements Runnable {
        private boolean stop;
        
        /**
        * Begin masking...display asterisks (*)
        */
        public void run () {
            stop = true;
            while (stop) {
                System.out.print("\010 ");
                try{
                    Thread.currentThread().sleep(1);
                }catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            }
        }
        
        /**
        * Instruct the thread to stop masking
        */
        public void stopMasking() {
            this.stop = false;
        }
    }
    
    public static void main(String[] args){
        IDCUserUtil util = new IDCUserUtil();
        if(args[0] != null && args[0].equals("remove")){
            util.removeUser();
        }else{
            util.addUser();
        }
    }
}