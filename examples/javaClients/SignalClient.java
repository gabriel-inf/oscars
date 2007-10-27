import net.es.oscars.client.*;
import net.es.oscars.wsdlTypes.*;
import java.rmi.RemoteException;
import net.es.oscars.oscars.*;

public class SignalClient extends Client{
    
    public static void main(String[] args){
    
        SignalClient signalClient = new SignalClient();
        String signalType = "all";
        boolean isAll = true;
        String token = null;
        
        if(args.length < 4){
            System.out.println("Invalid number of parameters");
            System.out.println("Form: java SignalClient repo url gri refreshes [all|create|refresh|teardown] [token]");
            System.exit(1);
        }else if(args.length >= 5){
            signalType = args[4];
            isAll = signalType.equals("all");
        }
        String repo = args[0];
        String url = args[1];
        int refreshIntervals = Integer.parseInt(args[3]);
        
        if(args.length == 6){
            token = args[5];
        }
        
        CreatePathContent createRequest = new CreatePathContent();
        createRequest.setGlobalReservationId(args[2]);
        createRequest.setToken(token);
        RefreshPathContent refreshRequest = new RefreshPathContent();
        refreshRequest.setGlobalReservationId(args[2]);
        refreshRequest.setToken(token);
        TeardownPathContent teardownRequest = new TeardownPathContent();
        teardownRequest.setGlobalReservationId(args[2]);
        teardownRequest.setToken(token);
        try{
            signalClient.setUp(true, url, repo);
            
            /* Create */
            if(isAll || signalType.equals("create")){
                System.out.println("Creating path for reservation " + 
                    args[2] + "...");
                CreatePathResponseContent createResponse = 
                    signalClient.createPath(createRequest);
                System.out.println("Global Reservation Id: " + 
                                    createResponse.getGlobalReservationId());
                System.out.println("Create Status: " + createResponse.getStatus());
            }
			
			/* Refresh */
			if(isAll || signalType.equals("refresh")){
                for(int i = 0; i < refreshIntervals; i++){
                    System.out.println("Refreshing path in 10 seconds...");
                    Thread.sleep (10000); //sleep for 10 seconds
                    RefreshPathResponseContent refreshResponse = 
                        signalClient.refreshPath(refreshRequest);
                    System.out.println("Global Reservation Id: " + 
                                    refreshResponse.getGlobalReservationId());
                    System.out.println("Refresh Status: " + refreshResponse.getStatus());
                    
                }
            }
			
			/* Teardown */
			if(isAll || signalType.equals("teardown")){
                System.out.println("Tearing down path...");
                TeardownPathResponseContent teardownResponse = 
                    signalClient.teardownPath(teardownRequest);
                System.out.println("Global Reservation Id: " + 
                                    teardownResponse.getGlobalReservationId());
                System.out.println("Teardown Status: " + teardownResponse.getStatus());
            }
            
			System.out.println("Done.");
        }catch(AAAFaultMessage e){
            System.out.println("AAA Error: " + e.getMessage());
        }catch(BSSFaultMessage e){
             System.out.println("BSS Error: " + e.getMessage());
        }catch(RemoteException e){
             System.out.println("Remote Error: " + e.getMessage());
        }catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        
        
    }
}
