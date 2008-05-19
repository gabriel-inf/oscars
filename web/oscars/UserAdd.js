/*
UserAdd.js:     Handles add user form.
Last modified:  May 19, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
init()
postUserAdd()
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget)
*/

dojo.provide("oscars.UserAdd");

oscars.UserAdd.init = function () {
    dojo.xhrPost({
        url: 'servlet/UserAddForm',
        handleAs: "json-comment-filtered",
        load: oscars.UserAdd.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("userAddForm").domNode
    });
};

oscars.UserAdd.postUserAdd = function () { 
    var valid = dijit.byId("userAddForm").validate();
    if (!valid) {
        return;
    }
    dojo.xhrPost({
        url: 'servlet/UserAdd',
        handleAs: "json-comment-filtered",
        load: oscars.UserAdd.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("userAddForm").domNode
    });
};

// handles reply from request to server to add user
oscars.UserAdd.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    if (responseObject.method == "UserAddForm") {
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
    } else if (responseObject.method == "UserAdd") {
        // after adding a user, refresh the user list and
        // display that tab
        var userListPane = dijit.byId("userListPane");
        mainTabContainer.selectChild(userListPane);
        oscars.UserList.refreshUserGrid();
    }
};

// take action based on this tab being selected
oscars.UserAdd.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* Boolean */ oscarsStatus,
        /* Boolean */ changeStatus) {
    if (changeStatus) {
        oscarsStatus.innerHTML = "Add a user";
    }
    if (contentPane.href == "") {
        contentPane.setHref("forms/userAdd.html");
    }
};
