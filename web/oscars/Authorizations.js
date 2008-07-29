/*
Authorizations.js:  Handles authorizations list functionality.
                    Note that it uses a grid.
Last modified:  July 23, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
init()
handleReply(responseObject, ioArgs)
tabSelected(contentPane, changeStatus)
refreshAuthGrid()
onAuthRowSelect(evt)
*/

dojo.provide("oscars.Authorizations");

oscars.Authorizations.init = function () {
};

// handles all servlet replies
oscars.Authorizations.handleReply = function (responseObject, ioArgs) {
    if (responseObject.method == "AuthorizationsForm") {
        // necessary for correct status message upon login
        if (!oscars.Form.resetStatus(responseObject, false)) {
            return;
        }
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
    } else if (responseObject.method == "AuthorizationList") {
        if (!oscars.Form.resetStatus(responseObject, true)) {
            return;
        }
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
        var mainTabContainer = dijit.byId("mainTabContainer");
        var authGrid = dijit.byId("authGrid");
        var model = authGrid.model;
        model.setData(responseObject.authData);
        authGrid.setSortIndex(0, true);
        authGrid.sort();
        authGrid.update();
        authGrid.resize();
        authGrid.resize();
        oscarsState.authGridInitialized = true;
    }
};

// takes action based on this tab being clicked on
oscars.Authorizations.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* Boolean */ oscarsStatus,
        /* Boolean */ changeStatus) {
    if (changeStatus) {
        oscarsStatus.innerHTML = "Authorizations list";
    }
    var authGrid = dijit.byId("authGrid");
    // Creation apparently needs to be programmatic, after the ContentPane
    // has been selected and its style no longer display:none
    if ((authGrid != null) && (!oscarsState.authGridInitialized)) {
        //dojo.connect(authGrid, "onRowClick", oscars.Authorizations.onAuthRowSelect);
        oscars.Authorizations.refreshAuthGrid();
    }
};

// refresh authorizations list from servlet
oscars.Authorizations.refreshAuthGrid = function () {
    dojo.xhrPost({
        url: 'servlet/AuthorizationList',
        handleAs: "json-comment-filtered",
        load: oscars.Authorizations.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("authListForm").domNode
    });
};

// select authorization details based on row select in grid
oscars.Authorizations.onAuthRowSelect = function (/*Event*/ evt) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    var authDetailsPane = dijit.byId("authDetailsPane");
    var authGrid = dijit.byId("authGrid");
    // get user login name
    var profileName = userGrid.model.getDatum(evt.rowIndex, 0);
    var formParam = dijit.byId("userProfileForm").domNode;
    formParam.reset();
    formParam.profileName.value = profileName;
    // get user details
    dojo.xhrPost({
        url: 'servlet/AuthorizationQuery',
        handleAs: "json-comment-filtered",
        load: oscars.AuthorizationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("authListForm").domNode
    });
    // set tab to authorization details
    mainTabContainer.selectChild(authDetailsPane);
};
