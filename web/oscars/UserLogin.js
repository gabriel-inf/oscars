/*
UserLogin.js:   Handles user login.  Displays all tabs if user authenticated.
                Note that all security is enforced on the server side.
Last modified:  May 30, 2008
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
    if (!oscars.Form.resetStatus(responseObject, true)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    var sessionPane = dijit.byId("sessionPane");
    var userNameInput = dojo.byId("userName");
    oscarsState.login = userNameInput.value;
    // toggle display of login/logout section of page
    var loginSection = dojo.byId("loginSection");
    loginSection.style.display = "none"; 
    var logoutSection = dojo.byId("logoutSection");
    logoutSection.style.display = ""; 

    // Programmatically create all tabs that user is authorized for.
    // All tabs except Login/Logout require authorization.
    //
    if (responseObject.authorizedTabs == null) {
        return;
    }
    var authorizedLength = 0;
    for (var tab in responseObject.authorizedTabs) {
        authorizedLength++;
    }
    if (authorizedLength == 0) {
        var oscarsStatus = dojo.byId("oscarsStatus");
        oscarsStatus.innerHTML = "User " + oscarsState.login +
                                 " is not authorized to view any tabs.";
        return;
    }
    if (responseObject.selectableRows.users) {
        oscarsState.userRowSelectable = true;
    }
    // add in reverse order
    // institutions management tab
    if (responseObject.authorizedTabs.institutionsPane) {
        var institutionsPane = new dojox.layout.ContentPane(
              {title:'Institutions', id: 'institutionsPane'},
               dojo.doc.createElement('div'));
        institutionsPane.setHref("forms/institutions.html");
        mainTabContainer.addChild(institutionsPane, 0);
        institutionsPane.startup();
    }
    // add user form
    if (responseObject.authorizedTabs.userAddPane) {
        var userAddPane = new dojox.layout.ContentPane(
              {title:'Add User', id: 'userAddPane'},
               dojo.doc.createElement('div'));
        mainTabContainer.addChild(userAddPane, 0);
        userAddPane.startup();
    }
    // user list form
    if (responseObject.authorizedTabs.userListPane) {
        var userListPane = new dojox.layout.ContentPane(
              {title:'User List', id: 'userListPane'},
               dojo.doc.createElement('div'));
        userListPane.setHref("forms/userList.html");
        mainTabContainer.addChild(userListPane, 0);
        userListPane.startup();
    }
    // user details form
    if (responseObject.authorizedTabs.userProfilePane) {
        var userProfilePane = new dojox.layout.ContentPane(
             {title:'User Profile', id: 'userProfilePane'},
              dojo.doc.createElement('div'));
        userProfilePane.setHref("forms/userProfile.html");
        mainTabContainer.addChild(userProfilePane, 0);
        userProfilePane.startup();
    }
    // create reservation form
    if (responseObject.authorizedTabs.reservationCreatePane) {
        var reservationCreatePane = new dojox.layout.ContentPane(
            {title:'Create Reservation', id: 'reservationCreatePane'},
             dojo.doc.createElement('div'));
        reservationCreatePane.setHref("forms/reservationCreate.html");
        mainTabContainer.addChild(reservationCreatePane, 0);
        reservationCreatePane.startup();
    }
    // reservation details form
    if (responseObject.authorizedTabs.reservationDetailsPane) {
        var reservationDetailsPane = new dojox.layout.ContentPane(
            {title:'Reservation Details', id: 'reservationDetailsPane'},
             dojo.doc.createElement('div'));
        reservationDetailsPane.setHref("forms/reservationDetails.html");
        mainTabContainer.addChild(reservationDetailsPane, 0);
        reservationDetailsPane.startup();
    }
    // list reservations form
    if (responseObject.authorizedTabs.reservationsPane) {
        var reservationsPane = new dojox.layout.ContentPane(
              {title:'Reservations', id: 'reservationsPane'},
               dojo.doc.createElement('div'));
        reservationsPane.setHref("forms/reservations.html");
        mainTabContainer.addChild(reservationsPane, 0);
        reservationsPane.startup();
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
