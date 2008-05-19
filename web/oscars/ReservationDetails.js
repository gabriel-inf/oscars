/*
ReservationDetails.js:  Handles reservation details form.
Last modified:  May 19, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
postQueryReservation()
postCancelReservation(dialogFields)
handleReply(responseObject, ioArgs)
hideParams(responseObject)
tabSelected(contentPaneWidget)
*/

dojo.provide("oscars.ReservationDetails");

// posts reservation query to server
oscars.ReservationDetails.postQueryReservation = function () {
    valid = dijit.byId("reservationDetailsForm").validate();
    if (!valid) {
        return;
    }
    dojo.xhrPost({
        url: 'servlet/QueryReservation',
        handleAs: "json-comment-filtered",
        load: oscars.ReservationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("reservationDetailsForm").domNode
    });
};

// posts cancel request to server
oscars.ReservationDetails.postCancelReservation = function (dialogFields) {
    dojo.xhrPost({
        url: 'servlet/CancelReservation',
            handleAs: "json-comment-filtered",
            load: oscars.ReservationDetails.handleReply,
            error: oscars.Form.handleError,
            form: dijit.byId("reservationDetailsForm").domNode
    });
};

// handles all servlet replies
oscars.ReservationDetails.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    if (responseObject.method == "QueryReservation") {
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
        // for displaying only layer 2 or layer 3 fields
        oscars.ReservationDetails.hideParams(responseObject);
    } else if (responseObject.method == "CancelReservation") {
        var statusN = dojo.byId("statusReplace");
        // table cell
        statusN.innerHTML = "CANCELLED";
    }
};

// chooses which params to display in reservation details page
oscars.ReservationDetails.hideParams = function (responseObject) {
    var i;
    var n = dojo.byId("vlanReplace");
    var layer2Nodes = dojo.query(".layer2Replace");
    var layer3Nodes = dojo.query(".layer3Replace");
    if (!oscars.Utils.isBlank(n.innerHTML)) {
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

// take action based on this tab's selection
oscars.ReservationDetails.tabSelected = function (
        /* ContentPane widget */ contentPane) {
};
