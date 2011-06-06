package edu.internet2.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class DerbyUtil {
    private static Logger log =  Logger.getLogger(DerbyUtil.class);
    
    public static boolean loadJDBCDriver(String derbyHome){
        if(derbyHome != null){
            System.setProperty("derby.system.home", derbyHome);
            log.debug("DERBY.SYSTEM.HOME=" + derbyHome);
        }
        
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (InstantiationException e) {
            log.error(e.getMessage());
            return false;
        } catch (IllegalAccessException e) {
            log.error(e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            return false;
        }
        
        return true;
    }
    
    public static void closeConnection(Connection conn){
        try {
            if(conn != null && (!conn.isClosed())){
                conn.close();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}
