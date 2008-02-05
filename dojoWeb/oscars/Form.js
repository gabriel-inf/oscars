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
    oscarsStatus.innerHTML = responseObject.status;
    if (responseObject.method == "AuthenticateUser") {
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

            var reservationDetailsPaneTab =
                dijit.byId("reservationDetailsPane");
            if (reservationDetailsPaneTab == null) {
              reservationDetailsPaneTab = new dojox.layout.ContentPane(
                {title:'Reservation Details', id: 'reservationDetailsPane'},
                 dojo.doc.createElement('div'));
                   reservationDetailsPaneTab.setHref("forms/reservation.html");
            }
            mainTabContainer.addChild(reservationDetailsPaneTab, 1);
            reservationDetailsPaneTab.startup();

            var createReservationPaneTab = dijit.byId("createReservationPane");
            if (createReservationPaneTab == null) {
              createReservationPaneTab = new dojox.layout.ContentPane(
                {title:'Create Reservation', id: 'createReservationPane'},
                 dojo.doc.createElement('div'));
            }
            mainTabContainer.addChild(createReservationPaneTab, 2);
            createReservationPaneTab.startup();

            var userDetailsPaneTab = dijit.byId("userDetailsPane");
            if (userDetailsPaneTab == null) {
                userDetailsPaneTab = new dojox.layout.ContentPane(
                 {title:'User Profile', id: 'userDetailsPane'},
                  dojo.doc.createElement('div'));
            }
            mainTabContainer.addChild(userDetailsPaneTab, 3);
            userDetailsPaneTab.startup();

            if (responseObject.authorizedTabs != null) {
                if (responseObject.authorizedTabs["usersPane"]) {
                    var usersPaneTab = dijit.byId("usersPane");
                    if (usersPaneTab == null) {
                        usersPaneTab = new dojox.layout.ContentPane(
                          {title:'User List', id: 'usersPane'},
                           dojo.doc.createElement('div'));
                    }
                    mainTabContainer.addChild(usersPaneTab, 3);
                    usersPaneTab.startup();
                }
                if (responseObject.authorizedTabs["userAddPane"]) {
                    var userAddPaneTab = dijit.byId("userAddPane");
                    if (userAddPaneTab == null) {
                        userAddPaneTab = new dojox.layout.ContentPane(
                          {title:'Add User', id: 'userAddPane'},
                          dojo.doc.createElement('div'));
                    }
                    mainTabContainer.addChild(userAddPaneTab, 4);
                    userAddPaneTab.startup();
                }
            }
        }
    } else if (responseObject.method == "UserLogout") {
        var sessionPane = dijit.byId("sessionPane");
        sessionPane.setHref("forms/login.html");
        if (dijit.byId("reservationsPane") != null) {
            mainTabContainer.removeChild(dijit.byId("reservationsPane"));
        }
        if (dijit.byId("createReservationPane") != null) {
            mainTabContainer.removeChild(dijit.byId("createReservationPane"));
        }
        if (dijit.byId("reservationDetailsPane") != null) {
            mainTabContainer.removeChild(dijit.byId("reservationDetailsPane"));
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
    } else if ((responseObject.method == "CreateReservationForm") ||
                (responseObject.method == "UserQuery") ||
                (responseObject.method == "UserAddForm") ||
                (responseObject.method == "QueryReservation")) {
        oscars.Form.applyParams(responseObject);
    }
    if (responseObject.method == "QueryReservation") {
        // for displaying only layer 2 or layer 3 fields
        oscars.Form.hideParams(responseObject, "reservationDetailsForm");
    }
}

oscars.Form.handleError = function(responseObject, ioArgs) {
}

// NOTE:  Depends on naming  convention agreements between client and server.
// Parameter names ending with Checkboxes, Display and Replace are treated
// differently than other names, which are treated as widget ids.  Note that
// widget id's of "method", "status", and "succeed" will mess things up, since
// they are parameter names used by handleReply.
oscars.Form.applyParams = function(responseObject) {
    for (var param in responseObject) {
        var n = dojo.byId(param);
        // if info for a group of checkboxes
        if (param.match(/Checkboxes$/i) != null) {
            var disabled = false;
            // first search to see if checkboxes can be modified
            for (var cb in responseObject[param]) {
                if (cb == "modify") {
                    if (!responseObject[param][cb]) {
                        disabled = true;
                        break;
                    }
                }
            }
            // set checkbox attributes
            for (var cb in responseObject[param]) {
                // get check box
                var w = dijit.byId(cb);
                if (w != null) {
                    if (responseObject[param][cb]) {
                        w.setChecked(true);
                    } else {
                        w.setChecked(false);
                    }
                    w.setDisabled(disabled);
                }
            }
        } else if (param.match(/Display$/i) != null) {
            if (n != null) {
                n.style.display= responseObject[param] ? "" : "none";
            }
        } else if (param.match(/TimeConvert$/i) != null) {
            if (n != null) {
                n.innerHTML = oscars.DigitalClock.convertFromSeconds(
                                                        responseObject[param]);
            }
        } else if (n == null) {
            continue;
        // if info to replace div section with; must be existing div with that
        // id
        } else if (param.match(/Replace$/i) != null) {
            n.innerHTML = responseObject[param];
        // set widget value
        } else {
            n.value = responseObject[param];
        }   
    }
}

oscars.Form.hideParams = function(responseObject, formId) {
    var w = dijit.byId(formId);
    // TODO
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
    var oscarsStatus = dojo.byId("oscarsStatus");
    // if not currently in error state, change status to reflect current tab
    var changeStatus = oscarsStatus.className == "success" ? true : false;
    if (contentPane.id == "reservationsPane") {
        if (changeStatus) {
            oscarsStatus.innerHTML = "Reservations list";
        }
    } else if (contentPane.id == "createReservationPane") {
        if (changeStatus) {
            oscarsStatus.innerHTML = "Reservation creation form";
        }
        var n = dojo.byId("reservationLogin");
        // only do first time
        if (n == null) {
            contentPane.setHref("forms/createReservation.html");
        }
    } else if (contentPane.id == "usersPane") {
        if (changeStatus) {
            oscarsStatus.innerHTML = "Users list";
        }
        var n = dojo.byId("usersLogin");
        // only do first time
        if (n == null) {
            contentPane.setHref("forms/users.html");
        }
    } else if (contentPane.id == "userAddPane") {
        if (changeStatus) {
            oscarsStatus.innerHTML = "Add a user";
        }
        var n = dojo.byId("addingUserLogin");
        if (n == null) {
            contentPane.setHref("forms/userAdd.html");
        }
    } else if (contentPane.id == "userDetailsPane") {
        // TODO:  FIX when coming in from user list when it is implemented
        if (changeStatus) {
            oscarsStatus.innerHTML = "Profile for user " + oscarsState.login;
        }
        var n = dojo.byId("userDetailsLogin");
        // only do first time
        if (n == null) {
            contentPane.setHref("forms/user.html");
        }
    } else if (contentPane.id == "sessionPane") {
        if (changeStatus) {
            oscarsStatus.innerHTML = "User " + oscarsState.login +
                                     " logged in";
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

oscars.Form.checkDateTimes = function() {
    var currentDate = new Date();
    var startSeconds =
        oscars.DigitalClock.convertDateTime(currentDate, "startDate",
                                                         "startTime");
    var endDate = new Date(startSeconds*1000 + 60*4*1000);
    var endSeconds =
        oscars.DigitalClock.convertDateTime(endDate, "endDate", "endTime");
    // additional checks for legality
    var msg = null;
    // check for start time more than four minutes in the past
    if (startSeconds < (currentDate.getTime()/1000 - 240)) {
        msg = "Start time is more than four minutes in the past";
    } else if (startSeconds > endSeconds) {
        msg = "End time is before start time";
    } else if (startSeconds == endSeconds) {
        msg = "End time is the same as start time";
    }
    if (msg != null) {
        var oscarsStatus = dojo.byId("oscarsStatus");
        oscarsStatus.className = "failure";
        oscarsStatus.innerHTML = msg;
        return false;
    }
    var startSecondsN = dojo.byId("hiddenStartSeconds");
    // set hidden field value, which is what servlet uses
    startSecondsN.value = startSeconds;
    var endSecondsN = dojo.byId("hiddenEndSeconds");
    endSecondsN.value = endSeconds;
    return true;
}
