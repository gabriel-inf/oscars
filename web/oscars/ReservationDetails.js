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
oscars.ReservationDetails.postQueryReservation = function (newGri) {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    if (!newGri) {
        oscars.ReservationDetails.setCurrentGri(formNode);
    } else {
        newGri = dijit.byId("newGri").getValue();
        formNode.gri.value = newGri;
    }
    dojo.xhrPost({
        url: 'servlet/QueryReservation',
        handleAs: "json-comment-filtered",
        load: oscars.ReservationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("reservationDetailsForm").domNode
    });
};

oscars.ReservationDetails.postModify = function () {
    var legalDates = oscars.ReservationDetails.checkDateTimes();
    if (!legalDates) {
        return;
    }
    valid = dijit.byId("reservationDetailsForm").validate();
    if (!valid) {
        return;
    }
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscars.ReservationDetails.setCurrentGri(formNode);
    // non-modifiable fields, but necessary to send to comply with
    // interface
    var bandwidth = dojo.byId("bandwidthReplace").innerHTML;
    formNode.modifyBandwidth.value = bandwidth;
    var description = dojo.byId("descriptionReplace").innerHTML;
    formNode.modifyDescription.value = description;
    dojo.xhrPost({
        url: 'servlet/ModifyReservation',
        handleAs: "json-comment-filtered",
        load: oscars.ReservationDetails.handleReply,
        error: oscars.Form.handleError,
        form: formNode
    });
};

// posts cancel request to server
oscars.ReservationDetails.postCancelReservation = function (dialogFields) {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscars.ReservationDetails.setCurrentGri(formNode);
    dojo.xhrPost({
        url: 'servlet/CancelReservation',
            handleAs: "json-comment-filtered",
            load: oscars.ReservationDetails.handleReply,
            error: oscars.Form.handleError,
            form: formNode
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
        oscars.ReservationDetails.setDateTimes();
    } else if (responseObject.method == "CancelReservation") {
        var statusN = dojo.byId("statusReplace");
        // table cell
        statusN.innerHTML = "CANCELLED";
    }
};

oscars.ReservationDetails.setCurrentGri = function (formNode) {
    var currentGri = dojo.byId("griReplace").innerHTML;
    formNode.gri.value = currentGri;
};

// check modified start and end date and times, and converts hidden form fields
// to seconds
oscars.ReservationDetails.checkDateTimes = function () {
    var msg = null;
    var startSeconds =
        oscars.DigitalClock.widgetsToSeconds("modifyStartDate",
                                             "modifyStartTime");
    var endSeconds =
        oscars.DigitalClock.widgetsToSeconds("modifyEndDate",
                                             "modifyEndTime");
    // additional checks for legality
    if (startSeconds < 0) {
        msg = "Both start date and time must be specified";
    } else if (endSeconds < 0) {
        msg = "Both end date and time must be specified";
    } else if (startSeconds > endSeconds) {
        msg = "End time is before start time";
    } else if (startSeconds == endSeconds) {
        msg = "End time is the same as start time";
    }
    if (msg != null) {
        var oscarsStatus = dojo.byId("oscarsStatus");
        oscarsStatus.className = "failure";
        oscarsStatus.innerHTML = msg;
        return false;
    }
    var startSecondsN = dojo.byId("modifyStartSeconds");
    // set hidden field value, which is what servlet uses
    startSecondsN.value = startSeconds;
    var endSecondsN = dojo.byId("modifyEndSeconds");
    endSecondsN.value = endSeconds;
    return true;
};

oscars.ReservationDetails.setDateTimes = function () {
    var secondsN = dojo.byId("modifyStartSeconds");
    oscars.DigitalClock.secondsToWidgets(secondsN.value, "modifyStartDate",
                                         "modifyStartTime");
    secondsN = dojo.byId("modifyEndSeconds");
    oscars.DigitalClock.secondsToWidgets(secondsN.value, "modifyEndDate",
                                         "modifyEndTime");
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
