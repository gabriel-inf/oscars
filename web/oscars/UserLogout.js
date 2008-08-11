/*
UserLogout.js:        Handles user logout.
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
    var loggedInSection = dojo.byId("loggedInSection");
    loggedInSection.style.display = "none"; 
    if (dijit.byId("cancelDialog")) {
        dijit.byId("cancelDialog").destroy();
    }
    // destroy all other tabs
    if (dijit.byId("reservationsPane")) {
        mainTabContainer.closeChild(dijit.byId("reservationsPane"));
    }
    if (dijit.byId("reservationCreatePane")) {
        mainTabContainer.closeChild(dijit.byId("reservationCreatePane"));
    }
    if (dijit.byId("reservationDetailsPane")) {
        mainTabContainer.closeChild(dijit.byId("reservationDetailsPane"));
    }
    if (dijit.byId("userListPane")) {
        mainTabContainer.closeChild(dijit.byId("userListPane"));
    }
    if (dijit.byId("userAddPane")) {
        mainTabContainer.closeChild(dijit.byId("userAddPane"));
    }
    if (dijit.byId("userProfilePane")) {
        mainTabContainer.closeChild(dijit.byId("userProfilePane"));
    }
    if (dijit.byId("institutionsPane")) {
        mainTabContainer.closeChild(dijit.byId("institutionsPane"));
    }
    if (dijit.byId("authorizationsPane")) {
        mainTabContainer.closeChild(dijit.byId("authorizationsPane"));
    }
    if (dijit.byId("authDetailsPane")) {
        mainTabContainer.closeChild(dijit.byId("authDetailsPane"));
    }
    // reset global state
    oscarsState.userGridInitialized = false;
    oscarsState.userRowSelectable = false;
    oscarsState.resvGridInitialized = false;
    oscarsState.institutionGridInitialized = false;
    oscarsState.authGridInitialized = false;
};
