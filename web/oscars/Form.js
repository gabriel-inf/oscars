/*
Form.js:        Javascript form callback handling using Dojo functionality
Last modified:  March 3, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
handleReply(responseObject, ioArgs)
handleError(responseObject, ioArgs)
handleAuthenticateReply(responseObject, mainTabContainer)
handleLogout(responseObject, mainTabContainer)
applyParams(responseObject)
layerChooser(evt);
hideParams(responseObject)
selectedChanged(contentPaneWidget)
refreshUserGrid()
onUserRowSelect(evt)
onResvRowSelect(evt)
hrefChanged(newUrl)
checkDateTimes()
convertSearchTimes()
convertReservationTimes()
isBlank(str)
initBackForwardState()
*/

dojo.provide("oscars.Form");

// handles all servlet replies
oscars.Form.handleReply = function (responseObject, ioArgs) {
    var status = responseObject.status;
    var mainTabContainer = dijit.byId("mainTabContainer");
    var oscarsStatus = dojo.byId("oscarsStatus");
    if (responseObject.success) {
        oscarsStatus.className = "success";
    } else {
        oscarsStatus.className = "failure";
    }
    // hack to prevent wrong status message on first page
    if (!oscarsState.firstPage || (responseObject.method != "UserQuery")) {
        oscarsStatus.innerHTML = responseObject.status;
        oscarsState.firstPage = false;
    }
    if (!responseObject.success) {
        return;
    }
    if (responseObject.method == "AuthenticateUser") {
        oscars.Form.handleAuthenticateReply(responseObject, mainTabContainer);
    } else if (responseObject.method == "UserLogout") {
        oscars.Form.handleLogout(responseObject, mainTabContainer);
    } else if (responseObject.method == "QueryReservation") {
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
        // for displaying only layer 2 or layer 3 fields
        oscars.Form.hideParams(responseObject);
    } else if ((responseObject.method == "CreateReservationForm") ||
                (responseObject.method == "UserQuery") ||
                (responseObject.method == "UserModify") ||
                (responseObject.method == "UserAddForm")) {
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
    } else if ((responseObject.method == "UserRemove") ||
                (responseObject.method == "UserAdd")) {
        // after adding or removing a user, refresh the user list and
        // display that tab
        var usersPaneTab = dijit.byId("usersPane");
        mainTabContainer.selectChild(usersPaneTab);
        oscars.Form.refreshUserGrid();
    } else if (responseObject.method == "CreateReservation") {
        // transition to reservation details tab on successful creation
        var formParam = dojo.byId("reservationDetailsForm");
        formParam.gri.value = responseObject.gri;
        dojo.xhrPost({
            url: 'servlet/QueryReservation',
            handleAs: "json-comment-filtered",
            load: oscars.Form.handleReply,
            error: oscars.Form.handleError,
            form: dojo.byId("reservationDetailsForm")
        });
        // set tab to reservation details
        var resvDetailsPaneTab = dijit.byId("reservationDetailsPane");
        mainTabContainer.selectChild(resvDetailsPaneTab);
    } else if (responseObject.method == "CancelReservation") {
        var statusN = dojo.byId("statusReplace");
        // table cell
        statusN.innerHTML = "CANCELLED";
    } else if (responseObject.method == "ListReservations") {
        var resvGrid = dijit.byId("resvGrid");
        var model = resvGrid.model;
        // convert seconds to datetime format before displaying
        oscars.Form.convertReservationTimes(responseObject.resvData);
        model.setData(responseObject.resvData);
        // workaround for bug where doesn't show up on first setData
        if (oscarsState.resvGridInitialized == 1) {
            model.setData(responseObject.resvData);
            oscarsState.resvGridInitialized = 2;
        }
    }
};

oscars.Form.handleError = function (responseObject, ioArgs) {
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "failure";
    oscarsStatus.innerHTML = responseObject.message +
          ".  If it is a servlet problem, contact an admin to restart the Web server.";
};

// handles successful reply from AuthenticateUser servlet
oscars.Form.handleAuthenticateReply = function (responseObject,
                                                mainTabContainer) {
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
    var reservationsPaneTab = new dojox.layout.ContentPane(
          {title:'Reservations', id: 'reservationsPane'},
           dojo.doc.createElement('div'));
           reservationsPaneTab.setHref("forms/reservations.html");
    mainTabContainer.addChild(reservationsPaneTab, 0);
    reservationsPaneTab.startup();

    // reservation details form
    var reservationDetailsPaneTab = new dojox.layout.ContentPane(
        {title:'Reservation Details', id: 'reservationDetailsPane'},
         dojo.doc.createElement('div'));
           reservationDetailsPaneTab.setHref("forms/reservation.html");
    mainTabContainer.addChild(reservationDetailsPaneTab, 1);
    reservationDetailsPaneTab.startup();

    // create reservation form
    var createReservationPaneTab = new dojox.layout.ContentPane(
        {title:'Create Reservation', id: 'createReservationPane'},
         dojo.doc.createElement('div'));
    mainTabContainer.addChild(createReservationPaneTab, 2);
    createReservationPaneTab.startup();

    // user details form
    var userDetailsPaneTab = new dojox.layout.ContentPane(
         {title:'User Profile', id: 'userDetailsPane'},
          dojo.doc.createElement('div'));
    userDetailsPaneTab.setHref("forms/user.html");
    mainTabContainer.addChild(userDetailsPaneTab, 3);
    userDetailsPaneTab.startup();

    // tabs requiring additional authorization
    if (responseObject.authorizedTabs != null) {
        // user list form
        if (responseObject.authorizedTabs.usersPane) {
            var usersPaneTab = new dojox.layout.ContentPane(
                  {title:'User List', id: 'usersPane'},
                   dojo.doc.createElement('div'));
                usersPaneTab.setHref("forms/users.html");
            mainTabContainer.addChild(usersPaneTab, 3);
            usersPaneTab.startup();
        }
        // add user form
        if (responseObject.authorizedTabs.userAddPane) {
            var userAddPaneTab = new dojox.layout.ContentPane(
                  {title:'Add User', id: 'userAddPane'},
                   dojo.doc.createElement('div'));
            mainTabContainer.addChild(userAddPaneTab, 4);
            userAddPaneTab.startup();
        }
    }
};

// handles user logout
oscars.Form.handleLogout = function (responseObject, mainTabContainer) {
    var sessionPane = dijit.byId("sessionPane");
    // toggle display of login/logout section of page
    var loginSection = dojo.byId("loginSection");
    loginSection.style.display = ""; 
    var logoutSection = dojo.byId("logoutSection");
    logoutSection.style.display = "none"; 
    // destroy all other tabs
    if (dijit.byId("reservationsPane") != null) {
        mainTabContainer.closeChild(dijit.byId("reservationsPane"));
    }
    if (dijit.byId("createReservationPane") != null) {
        mainTabContainer.closeChild(dijit.byId("createReservationPane"));
    }
    if (dijit.byId("reservationDetailsPane") != null) {
        mainTabContainer.closeChild(dijit.byId("reservationDetailsPane"));
    }
    if (dijit.byId("usersPane") != null) {
        mainTabContainer.closeChild(dijit.byId("usersPane"));
    }
    if (dijit.byId("userAddPane") != null) {
        mainTabContainer.closeChild(dijit.byId("userAddPane"));
    }
    if (dijit.byId("userDetailsPane") != null) {
        mainTabContainer.closeChild(dijit.byId("userDetailsPane"));
    }
    // reset global state
    oscarsState.userGridInitialized = false;
    oscarsState.resvGridInitialized = 0;
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
};

// chooses which input parameters to display in create reservation page
oscars.Form.layerChooser = function (/*Event*/ evt) {
    var i;
    var layer2Nodes = dojo.query(".layer2");
    var layer3Nodes = dojo.query(".layer3");
    if (evt.target.id == "layer2") {
        for (i = 0; i < layer2Nodes.length; i++) {
            layer2Nodes[i].style.display = ""; 
        }
        for (i = 0; i < layer3Nodes.length; i++) {
            layer3Nodes[i].style.display = "none"; 
        }
    } else if (evt.target.id == "layer3") {
        for (i = 0; i < layer2Nodes.length; i++) {
            layer2Nodes[i].style.display = "none"; 
        }
        for (i = 0; i < layer3Nodes.length; i++) {
            layer3Nodes[i].style.display = ""; 
        }
    }
};

// chooses which params to display in reservation details page
oscars.Form.hideParams = function (responseObject) {
    var i;
    var n = dojo.byId("vlanReplace");
    var layer2Nodes = dojo.query(".layer2Replace");
    var layer3Nodes = dojo.query(".layer3Replace");
    if (!oscars.Form.isBlank(n.innerHTML)) {
        for (i = 0; i < layer2Nodes.length; i++) {
            layer2Nodes[i].style.display = ""; 
        }
        for (i = 0; i < layer3Nodes.length; i++) {
            layer3Nodes[i].style.display = "none"; 
        }
    } else {
        for (i = 0; i < layer2Nodes.length; i++) {
            layer2Nodes[i].style.display = "none"; 
        }
        for (i = 0; i < layer3Nodes.length; i++) {
            layer3Nodes[i].style.display = ""; 
        }
    }
};

// take action based on which tab was clicked on
oscars.Form.selectedChanged = function (/* ContentPane widget */ contentPane) {
    var oscarsStatus = dojo.byId("oscarsStatus");
    // if not currently in error state, change status to reflect current tab
    var changeStatus = oscarsStatus.className == "success" ? true : false;
    var n = null;
    // selected reservations tab
    if (contentPane.id == "reservationsPane") {
        if (changeStatus) {
            oscarsStatus.innerHTML = "Reservations list";
        }
        // refresh reservations grid
        var resvGrid = dijit.byId("resvGrid");
        if ((resvGrid != null) && (oscarsState.resvGridInitialized == 0)) {
            oscarsState.resvGridInitialized = 1;
            dojo.connect(resvGrid, "onRowClick", oscars.Form.onResvRowSelect);
            dojo.xhrPost({
                url: 'servlet/ListReservations',
                handleAs: "json-comment-filtered",
                load: oscars.Form.handleReply,
                error: oscars.Form.handleError,
                form: dojo.byId("reservationListForm")
            });
        }
    // selected create reservation tab
    } else if (contentPane.id == "createReservationPane") {
        if (changeStatus) {
            oscarsStatus.innerHTML = "Reservation creation form";
        }
        n = dojo.byId("reservationLogin");
        // only do first time
        if (n == null) {
            contentPane.setHref("forms/createReservation.html");
        }
    // selected user details tab
    } else if (contentPane.id == "userDetailsPane") {
        if (changeStatus) {
            var node = dojo.byId("userDetailsForm");
            if (node == null) {
                console.log("userDetailsForm not instantiated");
            }
            if (node != null) {
                if (oscars.Form.isBlank(n.profileName.value)) {
                    oscarsStatus.innerHTML = "Profile for user " +
                                             oscarsState.login;
                } else {
                    oscarsStatus.innerHTML = "Profile for user " +
                                             n.profileName.value;
                }
            }
        }
    // selected user list tab
    } else if (contentPane.id == "usersPane") {
        if (changeStatus) {
            oscarsStatus.innerHTML = "Users list";
        }
        var userGrid = dijit.byId("userGrid");
        // The current implementation of grids is buggy.
        // This should not be necessary.
        if ((userGrid != null) && (!oscarsState.userGridInitialized)) {
            oscars.Form.refreshUserGrid();
            dojo.connect(userGrid, "onRowClick", oscars.Form.onUserRowSelect);
            oscarsState.userGridInitialized = true;
        }
    // selected add user tab
    } else if (contentPane.id == "userAddPane") {
        if (changeStatus) {
            oscarsStatus.innerHTML = "Add a user";
        }
        n = dojo.byId("addingUserLogin");
        if (n == null) {
            contentPane.setHref("forms/userAdd.html");
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
        }
    };
    dojo.back.addToHistory(state);
};

// refresh user list from servlet
oscars.Form.refreshUserGrid = function () {
    var userGrid = dijit.byId("userGrid");
    var newStore = new dojo.data.ItemFileReadStore(
                      {url: 'servlet/UserList'});
    var newModel = new dojox.grid.data.DojoData(
                      null, newStore,
                      {query: {login: '*'}, clientSort: true});
    userGrid.setModel(newModel);
    userGrid.refresh();
};

// select user details based on row select in grid
oscars.Form.onUserRowSelect = function (/*Event*/ evt) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    var userDetailsPaneTab = dijit.byId("userDetailsPane");
    var userGrid = dijit.byId("userGrid");
    // get user name
    var profileName = userGrid.model.getDatum(evt.rowIndex, 1);
    var formParam = dojo.byId("userDetailsForm");
    formParam.profileName.value = profileName;
    // get user details
    dojo.xhrPost({
        url: 'servlet/UserQuery',
        handleAs: "json-comment-filtered",
        load: oscars.Form.handleReply,
        error: oscars.Form.handleError,
        form: dojo.byId("userDetailsForm")
    });
    // set tab to user details
    mainTabContainer.selectChild(userDetailsPaneTab);
};

// select reservation based on grid row select
// TODO:  should be based on grid cell select; want to be able to copy
//        source or destination to link id's search tab
oscars.Form.onResvRowSelect = function (/*Event*/ evt) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    var resvDetailsPaneTab = dijit.byId("reservationDetailsPane");
    var resvGrid = dijit.byId("resvGrid");
    // get reservation's GRI; data in row starts at 0 unlike with
    // ItemFileReadStore
    var gri = resvGrid.model.getDatum(evt.rowIndex, 0);
    var formParam = dojo.byId("reservationDetailsForm");
    formParam.gri.value = gri;
    // get reservation details
    dojo.xhrPost({
        url: 'servlet/QueryReservation',
        handleAs: "json-comment-filtered",
        load: oscars.Form.handleReply,
        error: oscars.Form.handleError,
        form: dojo.byId("reservationDetailsForm")
    });
    // Set tab to reservation details.
    // Note that this generates an apparently harmless error message in
    // Firebug console.
    mainTabContainer.selectChild(resvDetailsPaneTab);
};

oscars.Form.hrefChanged = function (newUrl) {
    // start of back/forward button functionality
    var state = {
        back: function() { console.log("Back was clicked!"); },
        forward: function() { console.log("Forward was clicked!"); }
    };
    dojo.back.addToHistory(state);
};

// check create reservation form's start and end date and time's, and
// converts hidden form fields to seconds
oscars.Form.checkDateTimes = function () {
    var currentDate = new Date();
    var msg = null;
    var startSeconds =
        oscars.DigitalClock.convertDateTime(currentDate, "startDate",
                                            "startTime", true);
    // default is 4 minutes in the future
    var endDate = new Date(startSeconds*1000 + 60*4*1000);
    var endSeconds =
            oscars.DigitalClock.convertDateTime(endDate, "endDate", "endTime",
                                                true);
    // additional checks for legality
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
};

// sets hidden form fields' seconds values from search date and time
// constraints in list reservations form
oscars.Form.convertSearchTimes = function () {
    var currentDate = new Date();
    var startSeconds = null;
    var endSeconds = null;
    var startDateWidget = dijit.byId("startDateSearch");
    var startTimeWidget = dijit.byId("startTimeSearch");
    // don't do anything if both blank
    if (!(oscars.Form.isBlank(startDateWidget.getDisplayedValue()) &&
          oscars.Form.isBlank(startTimeWidget.getValue()))) {
        startSeconds =
            oscars.DigitalClock.convertDateTime(currentDate, "startDateSearch",
                                                "startTimeSearch", false);
    }
    var endDateWidget = dijit.byId("endDateSearch");
    var endTimeWidget = dijit.byId("endTimeSearch");
    if (!(oscars.Form.isBlank(endDateWidget.getDisplayedValue()) &&
          oscars.Form.isBlank(endTimeWidget.getValue()))) {
        endSeconds =
            oscars.DigitalClock.convertDateTime(currentDate, "endDateSearch",
                                                "endTimeSearch", false);
    }
    var startSecondsN = dojo.byId("startTimeSeconds");
    // set hidden field value, which is what servlet uses
    startSecondsN.value = startSeconds;
    var endSecondsN = dojo.byId("endTimeSeconds");
    endSecondsN.value = endSeconds;
};

// convert seconds in incoming reservations list to date and time
oscars.Form.convertReservationTimes = function (data) {
    for (var i=0; i < data.length; i++) {
        // these fields are in seconds
        data[i][2] = oscars.DigitalClock.convertFromSeconds(data[i][2]);
        data[i][3] = oscars.DigitalClock.convertFromSeconds(data[i][3]);
    }
};

// From Javascript book, p. 264

// check to see if no parameter set
oscars.Form.isBlank = function (str) {
    if (str == null) {
        return true;
    }
    for (var i = 0; i < str.length; i++) {
        var c = str.charAt(i);
        if ((c != ' ') && (c != '\n') && (c != '')) { return false; }
    }
    return true;
};

oscars.Form.initBackForwardState = function () {
    // initially no state
    var state = {
        back: function() { },
        forward: function() { }
    };
    dojo.back.setInitialState(state);
};

