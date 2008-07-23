/*
UserList.js:  Handles user list functionality.  Note that it uses a grid.
Last modified:  July 21, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
init()
handleReply(responseObject, ioArgs)
tabSelected(contentPane, changeStatus)
refreshUserGrid()
onUserRowSelect(evt)
*/

dojo.provide("oscars.UserList");

oscars.UserList.init = function () {
    dojo.xhrPost({
        url: 'servlet/UserListForm',
        handleAs: "json-comment-filtered",
        load: oscars.UserList.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("userListForm").domNode
    });
};

// handles all servlet replies
oscars.UserList.handleReply = function (responseObject, ioArgs) {
    if (responseObject.method == "UserListForm") {
        // necessary for correct status message upon login
        if (!oscars.Form.resetStatus(responseObject, false)) {
            return;
        }
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
    } else if (responseObject.method == "UserList") {
        if (!oscars.Form.resetStatus(responseObject, true)) {
            return;
        }
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
        var mainTabContainer = dijit.byId("mainTabContainer");
        var userGrid = dijit.byId("userGrid");
        var model = userGrid.model;
        model.setData(responseObject.userData);
        userGrid.resize();
        userGrid.render();
        oscarsState.userGridInitialized = true;
    }
};

// takes action based on this tab being clicked on
oscars.UserList.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* Boolean */ oscarsStatus,
        /* Boolean */ changeStatus) {
    if (changeStatus) {
        oscarsStatus.innerHTML = "Users list";
    }
    var userGrid = dijit.byId("userGrid");
    // Creation apparently needs to be programmatic, after the ContentPane
    // has been selected and its style no longer display:none
    if ((userGrid != null) && (!oscarsState.userGridInitialized)) {
        dojo.connect(userGrid, "onRowClick", oscars.UserList.onUserRowSelect);
        oscars.UserList.refreshUserGrid();
    }
};

// refresh user list from servlet
oscars.UserList.refreshUserGrid = function () {
    dojo.xhrPost({
        url: 'servlet/UserList',
        handleAs: "json-comment-filtered",
        load: oscars.UserList.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("userListForm").domNode
    });
};

// select user details based on row select in grid
oscars.UserList.onUserRowSelect = function (/*Event*/ evt) {
    if (!oscarsState.userRowSelectable) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    var userProfilePane = dijit.byId("userProfilePane");
    var userGrid = dijit.byId("userGrid");
    // get user login name
    var profileName = userGrid.model.getDatum(evt.rowIndex, 0);
    var formParam = dijit.byId("userProfileForm").domNode;
    formParam.reset();
    formParam.profileName.value = profileName;
    // get user details
    dojo.xhrPost({
        url: 'servlet/UserQuery',
        handleAs: "json-comment-filtered",
        load: oscars.UserProfile.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("userProfileForm").domNode
    });
    // set tab to user details
    mainTabContainer.selectChild(userProfilePane);
};
