/*
UserList.js:  Handles user list functionality.  Note that it uses a grid.
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

// handles all servlet replies
oscars.UserList.handleReply = function (responseObject, ioArgs) {
    if (responseObject.method == "UserList") {
        if (!oscars.Form.resetStatus(responseObject, true)) {
            return;
        }
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
        var userListHeaderNode = dojo.byId("userListHeaderDisplay");
        userListHeaderNode.style.display = "";
        var mainTabContainer = dijit.byId("mainTabContainer");
        var userGrid = dijit.byId("userGrid");
        var model = userGrid.model;
        model.setData(responseObject.userData);
        userGrid.update();
        userGrid.resize();
        userGrid.resize();
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
    if (userGrid && (!oscarsState.userGridInitialized)) {
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
    var formNode = dijit.byId("userProfileForm").domNode;
    formNode.reset();
    formNode.profileName.value = profileName;
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
