/*
UserLogin.js:   Handles user login.  Displays all tabs if user authenticated.
                Note that all security is enforced on the server side.
Last modified:  May 19, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
authenticateUser()
handleReply(responseObject, ioArgs)
tabSelected(changeStatus)
*/

dojo.provide("oscars.UserLogin");

// posts login request to AuthenticateUser servlet
oscars.UserLogin.authenticateUser = function () {
    var valid = dijit.byId("AuthenticateUser").validate();
    if (!valid) {
        return;
    }
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "inprocess";
    oscarsStatus.innerHTML = "Authenticating...";
    dojo.xhrPost({
        url: 'servlet/AuthenticateUser',
        handleAs: "json-comment-filtered",
        load: oscars.UserLogin.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("AuthenticateUser").domNode
    });
};

// handles reply from AuthenticateUser servlet
oscars.UserLogin.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    var sessionPane = dijit.byId("sessionPane");
    var userNameInput = dojo.byId("userName");
    oscarsState.login = userNameInput.value;
    oscarsState.firstPage = true;
    // toggle display of login/logout section of page
    var loginSection = dojo.byId("loginSection");
    loginSection.style.display = "none"; 
    var logoutSection = dojo.byId("logoutSection");
    logoutSection.style.display = ""; 

    // programmatically create all tabs that user is authorized for
    // list reservations form
    var reservationsPane = new dojox.layout.ContentPane(
          {title:'Reservations', id: 'reservationsPane'},
           dojo.doc.createElement('div'));
           reservationsPane.setHref("forms/reservations.html");
    mainTabContainer.addChild(reservationsPane, 0);
    reservationsPane.startup();

    // reservation details form
    var reservationDetailsPane = new dojox.layout.ContentPane(
        {title:'Reservation Details', id: 'reservationDetailsPane'},
         dojo.doc.createElement('div'));
           reservationDetailsPane.setHref("forms/reservationDetails.html");
    mainTabContainer.addChild(reservationDetailsPane, 1);
    reservationDetailsPane.startup();

    // create reservation form
    var reservationCreatePane = new dojox.layout.ContentPane(
        {title:'Create Reservation', id: 'reservationCreatePane'},
         dojo.doc.createElement('div'));
    mainTabContainer.addChild(reservationCreatePane, 2);
    reservationCreatePane.startup();

    // user details form
    var userProfilePane = new dojox.layout.ContentPane(
         {title:'User Profile', id: 'userProfilePane'},
          dojo.doc.createElement('div'));
    userProfilePane.setHref("forms/userProfile.html");
    mainTabContainer.addChild(userProfilePane, 3);
    userProfilePane.startup();

    // tabs requiring additional authorization
    if (responseObject.authorizedTabs != null) {
        // user list form
        if (responseObject.authorizedTabs.usersPane) {
            var userListPane = new dojox.layout.ContentPane(
                  {title:'User List', id: 'userListPane'},
                   dojo.doc.createElement('div'));
                userListPane.setHref("forms/userList.html");
            mainTabContainer.addChild(userListPane, 3);
            userListPane.startup();
        }
        // add user form
        if (responseObject.authorizedTabs.userAddPane) {
            var userAddPane = new dojox.layout.ContentPane(
                  {title:'Add User', id: 'userAddPane'},
                   dojo.doc.createElement('div'));
            mainTabContainer.addChild(userAddPane, 4);
            userAddPane.startup();
        }
    }
};

// action to take on initial tab select
oscars.UserLogin.tabSelected = function (
        /* Boolean */ oscarsStatus,
        /* Boolean */ changeStatus) {
    if (changeStatus) {
        oscarsStatus.innerHTML = "User " + oscarsState.login +
                                 " logged in";
    }
};
