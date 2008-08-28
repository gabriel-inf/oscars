/*
ReservationDetails.js:  Handles reservation details form.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
postQueryReservation()
postCancelReservation(dialogFields)
postCreatePath(dialogFields)
postTeardownPath(dialogFields)
postOverrideStatuus(dialogFields)
handleReply(responseObject, ioArgs)
hideParams(responseObject)
tabSelected(contentPaneWidget, oscarsStatus)
*/

dojo.provide("oscars.ReservationDetails");

// posts reservation query to server
oscars.ReservationDetails.postQueryReservation = function (newGri) {
    if (oscarsState.reservationDetailsEntered) {
        return;
    }
    oscarsState.reservationDetailsEntered = true;
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    if (!newGri) {
        oscars.ReservationDetails.setCurrentGri(formNode);
    } else {
        newGri = dijit.byId("newGri").getValue();
        // can happen if hit enter accidentally more than once
        if (oscars.Utils.isBlank(newGri)) {
            oscarsState.reservationDetailsEntered = false;
            return;
        }
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

// posts create path request to server
oscars.ReservationDetails.postCreatePath = function (dialogFields) {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscars.ReservationDetails.setCurrentGri(formNode);
    dojo.xhrPost({
        url: 'servlet/PathSetupReservation',
            handleAs: "json-comment-filtered",
            load: oscars.ReservationDetails.handleReply,
            error: oscars.Form.handleError,
            form: formNode
    });
};

// posts teardown path request to server
oscars.ReservationDetails.postTeardownPath = function (dialogFields) {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscars.ReservationDetails.setCurrentGri(formNode);
    dojo.xhrPost({
        url: 'servlet/PathTeardownReservation',
            handleAs: "json-comment-filtered",
            load: oscars.ReservationDetails.handleReply,
            error: oscars.Form.handleError,
            form: formNode
    });
};

// posts override status request to server
oscars.ReservationDetails.postOverrideStatus = function (dialogFields) {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscars.ReservationDetails.setCurrentGri(formNode);
    dojo.xhrPost({
        url: 'servlet/OverrideStatusReservation',
            handleAs: "json-comment-filtered",
            load: oscars.ReservationDetails.handleReply,
            error: oscars.Form.handleError,
            form: formNode
    });
};

oscars.ReservationDetails.forcedStatus = function () {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    var menu = dojo.byId("forcedStatusMenu");
    var selectedChoice = menu.selectedIndex;
    formNode.forcedStatus = menu.options[selectedChoice].value;
};

// Clones current reservation except for date/times, changing to the
// create reservation page with those parameters filled in.  This is a
// client-side only method.
oscars.ReservationDetails.cloneReservation = function () {
    var layer2Reservation = true;  // default is layer 2
    var i;

    oscars.ReservationCreate.resetFields();
    // copy fields from reservation details form to reservation creation form
    var node = dojo.byId("descriptionReplace");
    dijit.byId("reservationDescription").setValue(node.innerHTML);
    node = dojo.byId("bandwidthReplace");
    dijit.byId("bandwidth").setValue(node.innerHTML);
    node = dojo.byId("sourceReplace");
    dijit.byId("source").setValue(node.innerHTML);
    node = dojo.byId("destinationReplace");
    dijit.byId("destination").setValue(node.innerHTML);
    // see if path widget on create reservation page is displayed before
    // cloning path
    var pathSectionNode = dojo.byId("authorizedPathDisplay");
    if (pathSectionNode.style.display != "none") {
        var tableNode = dojo.byId("pathReplace");
        var tbodyNode = tableNode.firstChild;
        var trNodes = tbodyNode.childNodes;
        var pathStr = "";
        for (i = 0; i < trNodes.length; i++) {
            // get contents of text element in td (hop)
            pathStr += trNodes[i].firstChild.firstChild.data + "\n";
        }
        // don't clone layer 3 if no longer valid
        if (!pathStr.match(/Out of date IP/)) {
            // set path text area on create reservation page
            var textareaWidget = dijit.byId("explicitPath");
            textareaWidget.setValue(pathStr);
        }
    }
    node = dojo.byId("vlanReplace");
    if (oscars.Utils.isBlank(node.innerHTML)) {
        layer2Reservation = false;
    }
    if (layer2Reservation) {
        dijit.byId("vlanTag").setValue(node.innerHTML);
        node = dojo.byId("taggedReplace");
        var tagSrcPort = dojo.byId("tagSrcPort");
        var tagDestPort = dojo.byId("tagDestPort");
        if (node.innerHTML == "true") {
            tagSrcPort.selectedIndex = 0;
            tagDestPort.selectedIndex = 0;
        } else {
            tagSrcPort.selectedIndex = 1;
            tagDestPort.selectedIndex = 1;
        }
    } else {
        var radioWidget = dijit.byId("layer3");
        radioWidget.setValue(true);
        // show layer 3 parameters
        oscars.ReservationCreate.toggleLayer("layer3");
        node = dojo.byId("sourcePortReplace");
        dijit.byId("srcPort").setValue(node.innerHTML);
        node = dojo.byId("destinationPortReplace");
        dijit.byId("destPort").setValue(node.innerHTML);
        node = dojo.byId("protocolReplace");
        dijit.byId("protocol").setValue(node.innerHTML);
        node = dojo.byId("dscpReplace");
        dijit.byId("dscp").setValue(node.innerHTML);
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    // set to create reservation tab
    var resvCreatePane = dijit.byId("reservationCreatePane");
    mainTabContainer.selectChild(resvCreatePane);
};

// handles all servlet replies
oscars.ReservationDetails.handleReply = function (responseObject, ioArgs) {
    oscarsState.reservationDetailsEntered = false;
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    if (responseObject.method == "QueryReservation") {
        // reset node which indicates whether layer 2 or layer 3 before
        // applying results of query
        var node = dojo.byId("vlanReplace");
        node.innerHTML = "";
        var refreshButton = dojo.byId("resvRefreshDisplay");
        refreshButton.style.display = ""; 
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
        // for displaying only layer 2 or layer 3 fields
        oscars.ReservationDetails.hideParams(responseObject);
        oscars.ReservationDetails.setDateTimes();
        var reservationDetailsNode = dojo.byId("reservationDetailsDisplay");
        reservationDetailsNode.style.display = "";
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
    var msg;
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
    if (msg) {
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
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscarsStatus.className = "success";
    if (formNode.gri && formNode.gri.value) {
        oscarsStatus.innerHTML = "Reservation details for " + formNode.gri.value;
    } else {
        oscarsStatus.innerHTML = "Reservation details";
    }
};
