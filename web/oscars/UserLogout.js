/*
Logout.js:        Handles user logout.
Last modified:  May 30, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
postLogout()
handleReply(responseObject, ioArgs)
*/

dojo.provide("oscars.UserLogout");

// Handles reply from UserLogout servlet.  Closes all tabs and returns
// interface to original state.
oscars.UserLogout.postLogout = function () {
    dojo.xhrPost({
        url: 'servlet/UserLogout',
        handleAs: "json-comment-filtered",
        load: oscars.UserLogout.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("UserLogout").domNode
    });
};

// Handles reply from UserLogout servlet.  Closes all tabs and returns
// interface to original state.
oscars.UserLogout.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject, true)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    var sessionPane = dijit.byId("sessionPane");
    // Reset login values because otherwise valid to login again by
    // anyone accessing the browser.
    dijit.byId("AuthenticateUser").domNode.reset(); 
    // toggle display of login/logout section of page
    var loginSection = dojo.byId("loginSection");
    loginSection.style.display = ""; 
    var logoutSection = dojo.byId("logoutSection");
    logoutSection.style.display = "none"; 
    dijit.byId("cancelDialog").destroy();
    // destroy all other tabs
    if (dijit.byId("reservationsPane") != null) {
        mainTabContainer.closeChild(dijit.byId("reservationsPane"));
    }
    if (dijit.byId("reservationCreatePane") != null) {
        mainTabContainer.closeChild(dijit.byId("reservationCreatePane"));
    }
    if (dijit.byId("reservationDetailsPane") != null) {
        mainTabContainer.closeChild(dijit.byId("reservationDetailsPane"));
    }
    if (dijit.byId("userListPane") != null) {
        mainTabContainer.closeChild(dijit.byId("userListPane"));
    }
    if (dijit.byId("userAddPane") != null) {
        mainTabContainer.closeChild(dijit.byId("userAddPane"));
    }
    if (dijit.byId("userProfilePane") != null) {
        mainTabContainer.closeChild(dijit.byId("userProfilePane"));
    }
    if (dijit.byId("aaaPane") != null) {
        mainTabContainer.closeChild(dijit.byId("aaaPane"));
    }
    // reset global state
    oscarsState.userGridInitialized = false;
    oscarsState.resvGridInitialized = false;
    oscarsState.aaaGridsInitialized = false;
};
