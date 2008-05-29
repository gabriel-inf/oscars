/*
UserList.js:  Handles user list functionality.  Note that it uses a grid.
Last modified:  May 30, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
handleReply(responseObject, ioArgs)
tabSelected(contentPane, changeStatus)
refreshUserGrid()
onUserRowSelect(evt)
*/

dojo.provide("oscars.UserList");

// handles all servlet replies
oscars.UserList.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject, true)) {
        return;
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
    // The current implementation of grids is buggy.
    // This should not be necessary.
    if ((userGrid != null) && (!oscarsState.userGridInitialized)) {
        oscars.UserList.refreshUserGrid();
        dojo.connect(userGrid, "onRowClick", oscars.UserList.onUserRowSelect);
        oscarsState.userGridInitialized = true;
    }
};

// refresh user list from servlet
oscars.UserList.refreshUserGrid = function () {
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
oscars.UserList.onUserRowSelect = function (/*Event*/ evt) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    var userProfilePane = dijit.byId("userProfilePane");
    var userGrid = dijit.byId("userGrid");
    // get user name
    var profileName = userGrid.model.getDatum(evt.rowIndex, 1);
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
