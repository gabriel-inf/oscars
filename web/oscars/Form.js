/*
Form.js:        General form handling for browser interface.  Functionality
                specific to a single form is in its own module.
                Note that all security is enforced on the server side.
Last modified:  May 30, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
handleError(responseObject, ioArgs)
resetStatus(responseObject, changeStatus);
applyParams(responseObject)
selectedChanged(contentPaneWidget)
initBackForwardState()
*/

dojo.provide("oscars.Form");

oscars.Form.handleError = function (responseObject, ioArgs) {
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "failure";
    oscarsStatus.innerHTML = responseObject.message +
          ".  If it is a servlet problem, contact an admin to restart the Web server.";
};

// handles resetting status message, and checking for valid reply 
oscars.Form.resetStatus = function (responseObject, changeStatus) {
    var oscarsStatus = dojo.byId("oscarsStatus");
    if (responseObject.method == null) {
        oscarsStatus.className = "failure";
        oscarsStatus.innerHTML = "Invalid servlet reply: no method returned; " +
                                 "contact administrator";
        return false;
    }
    if (responseObject.success == null) {
        oscarsStatus.className = "failure";
        oscarsStatus.innerHTML = "Invalid servlet reply: no success status " +
                                 "returned; contact administrator";
        return false;
    }
    if (responseObject.status == null) {
        oscarsStatus.className = "failure";
        oscarsStatus.innerHTML = "Invalid servlet reply: no status returned; " +
                                 "contact administrator";
        return false;
    }
    var status = responseObject.status;
    if (responseObject.success) {
        oscarsStatus.className = "success";
    } else {
        oscarsStatus.className = "failure";
    }
    if (changeStatus || !responseObject.success) {
        oscarsStatus.innerHTML = responseObject.status;
    }
    if (!responseObject.success) {
        return false;
    }
    return true;
};

// NOTE:  Depends on naming  convention agreements between client and server.
// Parameter names ending with Checkboxes, Display and Replace are treated
// differently than other names, which are treated as widget ids.  Note that
// widget id's of "method", "status", and "succeed" will mess things up, since
// they are parameter names used by handleReply.
oscars.Form.applyParams = function (responseObject) {
    for (var param in responseObject) {
        var n = dojo.byId(param);
        var cb = null;
        var opt = null;
        var selected = null;
        // if info for a group of checkboxes
        if (param.match(/Checkboxes$/i) != null) {
            var disabled = false;
            // first search to see if checkboxes can be modified
            for (cb in responseObject[param]) {
                if (cb == "modify") {
                    if (!responseObject[param][cb]) {
                        disabled = true;
                        break;
                    }
                }
            }
            // set checkbox attributes
            for (cb in responseObject[param]) {
                // get check box
                var w = dijit.byId(cb);
                if (w != null) {
                    if (responseObject[param][cb]) {
                        w.setAttribute('checked', true);
                    } else {
                        w.setAttribute('checked', false);
                    }
                    w.setAttribute('disabled', disabled);
                }
            }
        // if info for a group of menu options
        } else if ((result = param.match(/(\w+)Menu$/i)) != null) {
            var newMenu = dojo.byId(result[1]);
            if (newMenu != null) {
                if (responseObject[param] instanceof Array) {
                    newMenu.options.length = 0;
                    for (var i=0; i < responseObject[param].length; i += 2) {
                        if (responseObject[param][i+1] == "true") {
                            selected = true;
                        } else {
                            selected = false;
                        }
                        //console.log(responseObject[param][i]);
                        //console.log(selected);
                        opt = new Option(responseObject[param][i],
                                         responseObject[param][i],
                                         selected, selected);
                        newMenu.add(opt, null);
                    }
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
};

// take action based on which tab was clicked on
oscars.Form.selectedChanged = function (/* ContentPane widget */ contentPane) {
    var mainTabContainer = null;
    // start of back/forward button functionality
    var state = {
        back: function() {
        },
        forward: function() {
        }
    };
    var oscarsStatus = dojo.byId("oscarsStatus");
    // if not currently in error state, change status to reflect current tab
    var changeStatus = oscarsStatus.className == "success" ? true : false;
    var n = null;
    // selected reservations tab
    if (contentPane.id == "reservationsPane") {
        oscars.Reservations.tabSelected(contentPane, oscarsStatus,
                                        changeStatus);
    // selected create reservation tab
    } else if (contentPane.id == "reservationCreatePane") {
        oscars.ReservationCreate.tabSelected(contentPane, oscarsStatus,
                                        changeStatus);
    // selected user details tab
    } else if (contentPane.id == "userProfilePane") {
        oscars.UserProfile.tabSelected(contentPane, oscarsStatus, changeStatus);
    // selected user list tab
    } else if (contentPane.id == "userListPane") {
        oscars.UserList.tabSelected(contentPane, oscarsStatus, changeStatus);
    // selected add user tab
    } else if (contentPane.id == "userAddPane") {
        oscars.UserAdd.tabSelected(contentPane, oscarsStatus, changeStatus);
    // selected institutions management tab
    } else if (contentPane.id == "institutionsPane") {
        oscars.Institutions.tabSelected(contentPane, oscarsStatus,
                                        changeStatus);
    // selected authorization list tab
    } else if (contentPane.id == "authorizationsPane") {
        oscars.Authorizations.tabSelected(contentPane, oscarsStatus,
                                          changeStatus);
    // selected authorization details tab
    } else if (contentPane.id == "authDetailsPane") {
        oscars.AuthorizationDetails.tabSelected(contentPane, oscarsStatus,
                                                changeStatus);
    // selected login/logout tab
    } else if (contentPane.id == "sessionPane") {
        oscars.UserLogin.tabSelected(oscarsStatus, changeStatus);
    }
};

oscars.Form.initBackForwardState = function () {
    var mainTabContainer = null;
    // callbacks handle back/forward button functionality
    var state = {
        back: function() { },
        forward: function() { }
    };
    dojo.back.setInitialState(state);
};

