package net.es.oscars.dojoServlets;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

public class Utils {

    public void handleError(HttpServletResponse response, int status,
                            String message) throws IOException {

        // Javascript isn't smart enough to separate into status code
        // and message, so adding **** to be able to easily parse.
        response.sendError(status, "**** " + message);
    }
}
