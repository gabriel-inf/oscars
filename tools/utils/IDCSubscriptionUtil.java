import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.notifybroker.db.Subscription;
import net.es.oscars.notifybroker.db.SubscriptionDAO;
import net.es.oscars.notifybroker.db.SubscriptionFilter;

import org.hibernate.Session;

import java.util.List;


public class IDCSubscriptionUtil {
    final private String DBNAME = "notify";
    final private int INACTIVE_STATUS = 0;
    final private int ACTIVE_STATUS = 1;
    final private int PAUSED_STATUS = 2;
    
    public void listSubscriptions(boolean all){
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(DBNAME);
        initializer.initDatabase(dbnames);
        Session notify =
            HibernateUtil.getSessionFactory(DBNAME).getCurrentSession();
        notify.beginTransaction();
        try{
            SubscriptionDAO subscriptionDAO = new SubscriptionDAO(DBNAME);
            
            List<Subscription> subList = null;
            if(all){
                subList = subscriptionDAO.list();
            }else{
                subList = subscriptionDAO.getAllActive();
            }
            
            int i = 1;
            System.out.println("USER - Subscription ID");
            System.out.println("======================");
            if(subList == null || subList.isEmpty()){
                System.out.println("No subscriptions in list");
                notify.getTransaction().commit();
                return;
            }
            for(Subscription sub : subList){
                System.out.println(i + ". " + sub.getUserLogin() + 
                        " - " + sub.getReferenceId());
                i++;
            }
            notify.getTransaction().commit();
        }catch(Exception e){
            notify.getTransaction().rollback();
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
    }
    
    private void printSubscription(String id) {
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(DBNAME);
        initializer.initDatabase(dbnames);
        Session notify =
            HibernateUtil.getSessionFactory(DBNAME).getCurrentSession();
        notify.beginTransaction();
        try{
            SubscriptionDAO subscriptionDAO = new SubscriptionDAO(DBNAME);
            Subscription subscription = subscriptionDAO.queryByRefId(id, null);
            if(subscription == null){
                System.err.println("No reservation found with id " + id);
                System.exit(1);
            }
            
            String statusString = "";
            if(subscription.getStatus() == ACTIVE_STATUS &&
                    subscription.getTerminationTime() > System.currentTimeMillis()){
                statusString = "ACTIVE";
            }else if(subscription.getStatus() == ACTIVE_STATUS){
                statusString = "EXPIRED";
            }else if(subscription.getStatus() == INACTIVE_STATUS){
                statusString = "INACTIVE";
            }else if(subscription.getStatus() == PAUSED_STATUS){
                statusString = "PAUSED";
            }else{
                statusString = "**INVALID STATUS**";
            }
            
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z");
            System.out.println("ID: " + subscription.getReferenceId());
            System.out.println("User: " + subscription.getUserLogin());
            System.out.println("Consumer URL: " + subscription.getUrl());
            System.out.println("Created Time: " + df.format(new Date(subscription.getCreatedTime()*1000L)));
            System.out.println("Termination Time: " + df.format(new Date(subscription.getTerminationTime()*1000L)));
            System.out.println("Status: " + statusString);
            System.out.println("Filters: ");
            Iterator<SubscriptionFilter> iterator = subscription.getFilters().iterator();
            while(iterator.hasNext()){
                SubscriptionFilter filter = iterator.next();
                System.out.println("    Type: " + filter.getType());
                System.out.println("    Value: " + filter.getValue());
                System.out.println();
            }
            notify.getTransaction().commit();
        }catch(Exception e){
            notify.getTransaction().rollback();
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
    }
    
    private void printHelp() {
        System.out.println("Parameters:");
        System.out.println("\t<none>\t\tif no parameters then lists all active subscriptions");
        System.out.println("\t-help\t\t displays this message.");
        System.out.println("\t-id <id>\t\tif specified then command displays details of subscription with <id>");
        System.out.println("\t-all\tif specified then displays all subscriptions regardless of status");
    }
    
    public static void main(String[] args){
        IDCSubscriptionUtil util = new IDCSubscriptionUtil();
        boolean showAll = false;
        String id = null; 
        for(String arg : args){
            if(arg.equals("-all")){
                showAll = true;
            }else if(arg.equals("-id")){
                id = "";
            }else if("".equals(id)){
                id = arg;
            }else if(arg.equals("-help")){
                util.printHelp();
                System.exit(0);
            }
        }
        
        if(showAll && id != null){
            System.err.println("Please specify -id OR -all (not both)");
            System.exit(1);
        }else if("".equals(id)){
            System.err.println("Please specify a value after -id");
            System.exit(1);
        }else if(id != null){
            util.printSubscription(id);
        }else{
            util.listSubscriptions(showAll);
        }
    }
}
