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
        this.log.info("CreateReservation.start");

        PrintWriter out = response.getWriter();
        UserSession userSession = new UserSession();
        String methodName= "CreateReservation";
        net.es.oscars.servlets.Utils utils =
            new net.es.oscars.servlets.Utils();
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        response.setContentType("text/json-comment-filtered");

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
            utils.handleFailure(out, "CreateReservation not completed: " + ex.getMessage(), 
                    methodName, null, null);
            this.log.error("Error calling rmiClient for CreateReservation", ex);
            return;
        }
        String errorMsg = (String)outputMap.get("error");
        if (errorMsg != null) {
            utils.handleFailure(out, errorMsg, methodName, null,null);
            this.log.error(errorMsg);
            return;
        }
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        this.log.info("CreateReservation.end - success");
        return;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }
}
