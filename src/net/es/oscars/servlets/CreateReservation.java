package net.es.oscars.servlets;

/**
 * CreateReservation servlet
 * 
 * @author David Robertson, Evangelos Chaniotak
 * 
 */
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.*;
import net.es.oscars.rmi.*;
import net.sf.json.JSONObject;

public class CreateReservation extends HttpServlet {
    private Logger log;

    /**
     * doGet
     * 
     * @param request HttpServlerRequest - contains start and end times, bandwidth, description,
     *          productionType, pathinfo
     * @param response HttpServler Response - contain gri and success or error status
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        String methodName= "CreateReservation";
        this.log.info("servlet.start");

        PrintWriter out = response.getWriter();
        UserSession userSession = new UserSession();
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        response.setContentType("application/json");

        HashMap<String, String[]> inputMap = new HashMap<String, String[]>();
        HashMap<String, Object> outputMap = new HashMap<String, Object>();

        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            inputMap.put(paramName, paramValues);
        }
        try {
            CoreRmiInterface rmiClient = new CoreRmiClient();
            rmiClient.init();
            outputMap = rmiClient.createReservation(inputMap, userName);
        } catch (Exception ex) {
            this.log.error("rmiClient failed with " + ex.getMessage());
            Utils.handleFailure(out, "CreateReservation not completed: " +
                    ex.getMessage(), methodName, null);
            return;
        }
        String errorMsg = (String)outputMap.get("error");
        if (errorMsg != null) {
            this.log.error(errorMsg);
            Utils.handleFailure(out, errorMsg, methodName, null);
            return;
        }
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info("servlet.end");
        return;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }
}
