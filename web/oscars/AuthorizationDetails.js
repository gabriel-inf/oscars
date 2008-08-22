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
tabSelected(contentPaneWidget, oscarsStatus)
resetFields(useSaved)
validate()
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
    if (!oscars.AuthorizationDetails.validate(formNode)) {
        return;
    }
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
    if (!oscars.AuthorizationDetails.validate(formNode)) {
        return;
    }
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
};

// handles all servlet replies
oscars.AuthorizationDetails.handleReply = function (responseObject, ioArgs) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    // set parameter values in form from responseObject
    oscars.Form.applyParams(responseObject);
    if ((responseObject.method != "AuthorizationForm") &&
        (responseObject.method != "AuthorizationAdd")) {
        oscars.AuthorizationDetails.setMenuOptionsEnabled();
        oscarsState.authorizationState.clearAuthState();
        // after deleting or modifying an authorization, refresh the
        // authorizations list and display that tab
        var pane = dijit.byId("authorizationsPane");
        mainTabContainer.selectChild(pane);
        // must refresh when visible
        oscars.Authorizations.refreshAuthGrid();
    } else if (responseObject.method == "AuthorizationAdd") {
        formNode = dijit.byId("authListForm").domNode;
        formNode.authsAdded.value = "changed";
    }
};

// take action based on this tab's selection
oscars.AuthorizationDetails.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {
    oscarsStatus.className = "success";
    oscarsStatus.innerHTML = "Authorization details";
};

oscars.AuthorizationDetails.resetFields = function (useSaved) {
    // TODO:  reset to original authorization if useSaved true
    var formNode = dijit.byId("authDetailsForm").domNode;
    var menu;
    // clear everything
    if (!useSaved) {
        oscars.AuthorizationDetails.setMenuOptionsEnabled();
        oscarsState.authorizationState.clearAuthState();
    } else {
        oscarsState.authorizationState.recoverAuthState(formNode);
    }
};

oscars.AuthorizationDetails.setMenuOptionsEnabled = function () {
    var i;
    var formNode = dijit.byId("authDetailsForm").domNode;
    var menu = formNode.resourceName;
    for (i=0; i < menu.options.length; i++) {
        menu.options[i].disabled = false;
    }
    menu = formNode.permissionName;
    for (i=0; i < menu.options.length; i++) {
        menu.options[i].disabled = false;
    }
    menu = formNode.constraintName;
    for (i=0; i < menu.options.length; i++) {
        menu.options[i].disabled = false;
    }
    menu.disabled = true;
    menu.selectedIndex = 2;
};

oscars.AuthorizationDetails.validate = function (formNode) {
    var menu = formNode.constraintName;
    var constraintName = menu.options[menu.selectedIndex].value;
    var oscarsStatus = dojo.byId("oscarsStatus");
    var constraintValue = formNode.constraintValue.value;
    if (constraintName == 'none') {
        // this will be unnecessary when constrainChoices is complete
        if (constraintValue) {
            oscarsStatus.className = "failure";
            oscarsStatus.innerHTML = "Constraint value must be empty for " +
                                     "the selected constraint option none";
            return false;
        }
    } else {
        if (!constraintValue) {
            oscarsStatus.className = "failure";
            oscarsStatus.innerHTML = "Constraint value must be filled in";
            return false;
        } else {
            menu = formNode.resourceName;
            var resourceName = menu.options[menu.selectedIndex].value;
            menu = formNode.permissionName;
            var permissionName = menu.options[menu.selectedIndex].value;
            var constraintType = 
                oscarsState.authorizationState.getConstraintType(resourceName,
                                                permissionName, constraintName);
            // try converting string to right Javascript type
            if (constraintValue == "true") {
                constraintValue = true;
            } else if (constraintValue == "false") {
                constraintValue = false;
            } else {
                // try converting string to number
                constraintValue = parseInt(constraintValue, 10);
            }
            var newConstraintType = typeof constraintValue;
            if (constraintType == "numeric") {
                constraintType = "number";
            } 
            if (newConstraintType != constraintType) {
                oscarsStatus.className = "failure";
                oscarsStatus.innerHTML = "Constraint value not of type " +
                                         constraintType;
                return false;
            }
        }
    }
    return true;
};
