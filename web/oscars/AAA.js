/*
AAA.js:     Handles form for basic AAA tables.
Last modified:  June 24, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
postInstitutionAdd()
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget)
*/

dojo.provide("oscars.AAA");

oscars.AAA.postTableOp = function () { 
    var valid = dijit.byId("aaaForm").validate();
    if (!valid) {
        return;
    }
};

// handles reply from request to server to add user
oscars.AAA.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject, true)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
};

// take action based on this tab being selected
oscars.AAA.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* Boolean */ oscarsStatus,
        /* Boolean */ changeStatus) {
    if (changeStatus) {
        oscarsStatus.innerHTML = "Basic AAA Management";
    }
    // refresh all grids initially
};
