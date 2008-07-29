/*
AuthorizationDetails.js:  Handles authorization details form.
Last modified:  July 25, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
init()
postQuery()
postModify()
postDelete()
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget)
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

// posts authorization query to server
oscars.AuthorizationDetails.postQuery = function (newWhat) {
    var formNode = dijit.byId("authDetailsForm").domNode;
    dojo.xhrPost({
        url: 'servlet/AuthorizationQuery',
        handleAs: "json-comment-filtered",
        load: oscars.AuthorizationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("authDetailsForm").domNode
    });
};

oscars.AuthorizationDetails.postModify = function () {
    valid = dijit.byId("authDetailsForm").validate();
    if (!valid) {
        return;
    }
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
    dojo.xhrPost({
        url: 'servlet/AuthorizationRemove',
            handleAs: "json-comment-filtered",
            load: oscars.AuthorizationDetails.handleReply,
            error: oscars.Form.handleError,
            form: formNode
    });
};

// Clones current authorization, changing to the  add authorization page with
// those parameters filled in.  This is a client-side only method.
oscars.AuthorizationDetails.clone = function () {
    var i = null;

    oscars.AuthorizationAdd.resetFields();
    // copy fields from authorization details form to add authorization form
    var mainTabContainer = dijit.byId("mainTabContainer");
    // set to authorization add tab
    var authAddPane = dijit.byId("authorizationAddPane");
    mainTabContainer.selectChild(authAddPane);
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
