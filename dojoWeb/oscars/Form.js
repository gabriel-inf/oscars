/*
Form.js:        Javascript form callback handling
Last modified:  January 24, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
handleReply(responseObject, ioArgs)
handleError(responseObject, ioArgs)
*/

dojo.provide("oscars.Form");

oscars.Form.handleReply = function (responseObject, ioArgs) {
    var status = responseObject.status;
    var mainTabContainer = dijit.byId("mainTabContainer");
    var oscarsStatus = dojo.byId("oscarsStatus");
    if (responseObject.success) {
        oscarsStatus.className = "success";
    } else {
        oscarsStatus.className = "failure";
    }
    if (responseObject.method == "AuthenticateUser") {
        oscarsStatus.innerHTML = responseObject.status;
        var sessionPane = dijit.byId("sessionPane");
        if (responseObject.success) {
            var userNameInput = dojo.byId("userName");
            oscarsState.login = userNameInput.value;
            sessionPane.setHref("forms/logout.html");
            var reservationsPaneTab = dijit.byId("reservationsPane");
            if (reservationsPaneTab == null) {
                reservationsPaneTab = new dojox.layout.ContentPane(
                  {title:'Reservations', id: 'reservationsPane'},
                   dojo.doc.createElement('div'));
                   reservationsPaneTab.setHref("forms/reservations.html");
            }
            mainTabContainer.addChild(reservationsPaneTab, 0);
            reservationsPaneTab.startup();
            var createReservationPaneTab = dijit.byId("createReservationPane");
            if (createReservationPaneTab == null) {
              createReservationPaneTab = new dojox.layout.ContentPane(
                {title:'Create Reservation', id: 'createReservationPane'},
                 dojo.doc.createElement('div'));
                 createReservationPaneTab.setHref("forms/createReservation.html");
            }
            mainTabContainer.addChild(createReservationPaneTab, 1);
            createReservationPaneTab.startup();
            var userDetailsPaneTab = dijit.byId("userDetailsPane");
            if (userDetailsPaneTab == null) {
                userDetailsPaneTab = new dojox.layout.ContentPane(
                 {title:'User Profile', id: 'userDetailsPane'},
                  dojo.doc.createElement('div'));
                userDetailsPaneTab.setHref("forms/user.html");
            }
            mainTabContainer.addChild(userDetailsPaneTab, 2);
            userDetailsPaneTab.startup();
            if (responseObject.authorizedTabs != null) {
                if (responseObject.authorizedTabs["usersPane"]) {
                    var usersPaneTab = dijit.byId("usersPane");
                    if (usersPaneTab == null) {
                        usersPaneTab = new dojox.layout.ContentPane(
                          {title:'User List', id: 'usersPane'},
                           dojo.doc.createElement('div'));
                        usersPaneTab.setHref("forms/users.html");
                    }
                    mainTabContainer.addChild(usersPaneTab, 2);
                    usersPaneTab.startup();
                }
                if (responseObject.authorizedTabs["userAddPane"]) {
                    var userAddPaneTab = dijit.byId("userAddPane");
                    if (userAddPaneTab == null) {
                        userAddPaneTab = new dojox.layout.ContentPane(
                          {title:'Add User', id: 'userAddPane'},
                          dojo.doc.createElement('div'));
                        userAddPaneTab.setHref("forms/userAdd.html");
                    }
                    mainTabContainer.addChild(userAddPaneTab, 3);
                    userAddPaneTab.startup();
                }
            }
        }
    } else if (responseObject.method == "UserLogout") {
        var oscarsStatus = dojo.byId("oscarsStatus");
        var sessionPane = dijit.byId("sessionPane");
        sessionPane.setHref("forms/login.html");
        if (dijit.byId("reservationsPane") != null) {
            mainTabContainer.removeChild(dijit.byId("reservationsPane"));
        }
        if (dijit.byId("createReservationPane") != null) {
            mainTabContainer.removeChild(dijit.byId("createReservationPane"));
        }
        if (dijit.byId("usersPane") != null) {
            mainTabContainer.removeChild(dijit.byId("usersPane"));
        }
        if (dijit.byId("userAddPane") != null) {
            mainTabContainer.removeChild(dijit.byId("userAddPane"));
        }
        if (dijit.byId("userDetailsPane") != null) {
            mainTabContainer.removeChild(dijit.byId("userDetailsPane"));
        }
    // called when tab instantiated, so don't update status here
    } else if (responseObject.method == "UserQuery") {
        oscars.Form.applyParams(responseObject);
    }
}

oscars.Form.handleError = function(responseObject, ioArgs) {
}

oscars.Form.applyParams = function(responseObject) {
    for (var param in responseObject) {
        var w = dojo.byId(param);
        // NOTE:  have to be careful with widget id's on page
        //        can't have id's of method or success
        if (w == null) {
            continue;
        } else if (param.match(/Div$/i) != null) {
            w.innerHTML = responseObject[param];
        } else {
            w.value = responseObject[param];
        }   
    }
}

oscars.Form.initState = function() {
    var state = {
        back: function() { console.log("Back was clicked!"); },
        forward: function() { console.log("Forward was clicked!"); },
        changeUrl: "login",
    };
    dojo.back.setInitialState(state);
}

oscars.Form.selectedChanged = function(contentPane) {
    if (contentPane.id == "userDetailsPane") {
        var oscarsStatus = dojo.byId("oscarsStatus");
        var w = dojo.byId("userDetailsLogin");
        if (w != null) {
            oscarsStatus.innerHTML = "Profile for " + w.value;
        }
    }
    // start of back/forward button functionality
    var state = {
        back: function() {
            console.log("Back was clicked! ");
        },
        forward: function() {
            console.log("Forward was clicked! ");
        },
        changeUrl: contentPane.id,
    };
    dojo.back.addToHistory(state);
}

oscars.Form.hrefChanged = function(newUrl) {
    // start of back/forward button functionality
    var state = {
        back: function() { console.log("Back was clicked!"); },
        forward: function() { console.log("Forward was clicked!"); },
        changeUrl: newUrl,
    };
    dojo.back.addToHistory(state);
}

