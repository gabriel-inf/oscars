package net.es.oscars.aaa;

import java.io.Serializable;

/**
 * Authorization values generally returned by UserManager.checkAccess. <br>
 * DENIED means the requested action is not allowed<br>
 * ALLUSERS means the requested action is allowed on objects that belong
 *     to any user<br>
 * MYSITE means the requested actions is allowed only on objects that
 * 	    belong to the same site as the requester<br>
 * SELFONLY  means the requested action is allowed only on objects that
 *      belong to the requester.
 * @author Mary Thompson, Evangelos Chaniotakis
 *
 */
public enum AuthValue implements Serializable {

     DENIED, ALLUSERS, MYSITE, SELFONLY;
}
