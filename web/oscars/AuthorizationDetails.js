/*
AuthorizationDetails.js:  Handles authorization details form.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
init()
postAdd()
postModify()
postDelete()
close()
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget)
resetFields(useSaved)
*/

dojo.provide("oscars.AuthorizationDetails");

oscars.AuthorizationDetails.init = function () {
    dojo.xhrPost({
        url: 'servlet/AuthorizationForm',
        handleAs: "json-comment-filtered",
        load: oscars.AuthorizationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("authDetailsForm").domNode
    });
};

// posts authorization add to server
oscars.AuthorizationDetails.postAdd = function () {
    var formNode = dijit.byId("authDetailsForm").domNode;
    dojo.xhrPost({
        url: 'servlet/AuthorizationAdd',
        handleAs: "json-comment-filtered",
        load: oscars.AuthorizationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("authDetailsForm").domNode
    });
};

oscars.AuthorizationDetails.postModify = function () {
    var formNode = dijit.byId("authDetailsForm").domNode;
    dojo.xhrPost({
        url: 'servlet/AuthorizationModify',
        handleAs: "json-comment-filtered",
        load: oscars.AuthorizationDetails.handleReply,
        error: oscars.Form.handleError,
        form: formNode
    });
};

// posts delete request to server
oscars.AuthorizationDetails.postDelete = function () {
    var formNode = dijit.byId("authDetailsForm").domNode;
    var authGrid = dijit.byId("authGrid");
    // TODO:  need to get selected row, and data from that
    dojo.xhrPost({
        url: 'servlet/AuthorizationRemove',
            handleAs: "json-comment-filtered",
            load: oscars.AuthorizationDetails.handleReply,
            error: oscars.Form.handleError,
            form: formNode
    });
};

// Changes to the add version of the page with the current parameters.
// This is a client-side only method.
oscars.AuthorizationDetails.clone = function () {
    var modifyAuthorizationNode = dojo.byId("modifyAuthorizationDisplay");
    modifyAuthorizationNode.style.display = "none";
    var addAuthorizationNode = dojo.byId("addAuthorizationDisplay");
    addAuthorizationNode.style.display = "";
    oscars.AuthorizationDetails.resetFields(false);
};

// handles all servlet replies
oscars.AuthorizationDetails.handleReply = function (responseObject, ioArgs) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    if (responseObject.method == "AuthorizationForm") {
        if (!oscars.Form.resetStatus(responseObject, false)) {
            return;
        }
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
    } else {
        if (!oscars.Form.resetStatus(responseObject, true)) {
            return;
        }
        oscars.Form.applyParams(responseObject);
    }
};

// take action based on this tab's selection
oscars.AuthorizationDetails.tabSelected = function (
        /* ContentPane widget */ contentPane) {
};

oscars.AuthorizationDetails.resetFields = function (useSaved) {
    // TODO:  reset to original authorization if useSaved true
    var formParam = dijit.byId("authDetailsForm").domNode;
    var menu = null;
    // clear everything
    if (!useSaved) {
        menu = formParam.authAttributeName;
        menu.options[0].disabled = false;
        oscars.Form.setMenuSelected(menu, "None");
        menu = formParam.resourceName;
        menu.options[0].disabled = false;
        oscars.Form.setMenuSelected(menu, "None");
        menu = formParam.permissionName;
        menu.options[0].disabled = false;
        oscars.Form.setMenuSelected(menu, "None");
        menu = formParam.constraintName;
        menu.options[0].disabled = false;
        oscars.Form.setMenuSelected(menu, "None");
        formParam.reset();
    }
};
