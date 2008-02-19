/*
validator.js:      Javascript functions for form validation
Last modified:  February 19, 2008
David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)
*/

/* List of functions:
checkForRequired(form, required)
checkLogin(form)
checkReservation(form)
checkDateFields(form)
checkProfileModification(form)
checkAddUser(form)
isLeapYear( intYear )
isNumeric(value)
isBlank(str)
*/

var validateParams = {
    'AuthenticateUser' : checkLogin,
    'CreateReservation' : checkReservation,
    'ListReservations' : checkListReservations,
    'UserModify' : checkProfileModification,
    'UserAdd' : checkAddUser
}

// TODO:  objects
var loginRequired = {
    'userName': "Please enter your user name.",
    'password': "Please enter your password."
}

var reservationRequired = {
    'source': "Enter a value in the 'Source' field.",
    'destination':  "Enter a value in the 'Destination' field.",
    'bandwidth': "Enter the bandwidth required in the 'Bandwidth' field.",
    'description': "Please describe the purpose of this reservation request."
}

var profileModificationRequired = {
    'lastName': "Please enter the user's last name.",
    'firstName': "Please enter the user's first name.",
    'institutionName':  "Please enter the user's organization.",
    'emailPrimary': "Please enter the user's primary email address.",
    'phonePrimary': "Please enter the user's primary phone number."
}

var addUserRequired = {
    'profileName': "Please enter the new user's distinguished name.",
    'password': "Please enter the new user's password.",
    'lastName': "Please enter the new user's last name.",
    'firstName': "Please enter the new user's first name.",
    'institutionName':  "Please enter the new user's organization.",
    'emailPrimary': "Please enter the new user's primary email address.",
    'phonePrimary': "Please enter the new user's primary phone number."
}

// Checks to make sure all required fields are present.
function checkForRequired(form, required) {
    for (field in required) {
        if (form[field] && isBlank(form[field].value)) {
            alert(required[field]);
            form[field].focus();
            return false;
        }
    }
    return true;
}

// Checks validity of login form.
function checkLogin(form) {
    return checkForRequired(form, loginRequired);
}

// Checks validity of create reservation form.
function checkReservation(form) {
    var valid = checkForRequired(form, reservationRequired);
    if (!valid) { return false; }

    if (!(isNumeric(form.bandwidth.value))) {
        alert("The bandwidth entered is " + form.bandwidth.value +
              ".  It must be a positive integer.");
        form.bandwidth.focus();
        return false;
    }
    else if ((form.bandwidth.value < 1 ) || (form.bandwidth.value > 10000)) {
        alert("The bandwidth entered is " + form.bandwidth.value +
              ".  It must be in the range 1-10000 Mbps.");
        form.bandwidth.focus();
        return false;
    }
    if (form.source.value == form.destination.value) {
        alert("The source and destination cannot be the same." );
        form.source.focus();
        return false;
    }
    // TODO:  needs more work, only this case with layer 3
    var sections = form.source.value.split('/');
    if ((sections.length > 1) && (sections[1] < 24)) {
        alert("Only CIDR blocks >= 24 (class C) are accepted.");
        form.source.focus();
        return false;
    }
    var sections = form.destination.value.split('/');
    if ((sections.length > 1) && (sections[1] < 24)) {
        alert("Only CIDR blocks >= 24 (class C) are accepted.");
        form.destination.focus();
        return false;
    }

    // check layer 2 field if a value has been entered
    if (!isBlank(form.vlanTag.value)) {
        var vlanFields = form.vlanTag.value.split("-");
        for (var i = 0; i < vlanFields.length; i++) {
            if (!(isNumeric(vlanFields[i])) && vlanFields != "any") {
                alert("A vlan tag entered is " + vlanFields[i] +
                      ".  It must be a positive integer.");
                form.vlanTag.focus();
                return false;
            }
            else if (isNumeric(vlanFields[i]) && ((vlanFields[i] < 1) || (vlanFields[i] > 4094))) {
                alert("A vlan tag entered is " + vlanFields[i] +
                      ".  It must be in the range 1-4094.");
                form.vlanTag.focus();
                return false;
            }
        }
    }

    // check layer 3 fields if values have been entered
    if (!isBlank(form.srcPort.value)) {
        if (!(isNumeric(form.srcPort.value))) {
            alert("The source port entered is " + form.srcPort.value +
                  ".  It must be a positive integer.");
            form.srcPort.focus();
            return false;
        }
        else if ((form.srcPort.value < 1024) || (form.srcPort.value > 65535)) {
            alert("The source port entered is " + form.srcPort.value +
                  ".  It must be in the range 1024-65535.");
            form.srcPort.focus();
            return false;
        }
    }
    if (!isBlank(form.destPort.value)) {
        if (!(isNumeric(form.destPort.value))) {
            alert("The destination port entered is " + form.srcPort.value +
                  ".  It must be a positive integer.");
            form.destPort.focus();
            return false;
        }
        else if ((form.destPort.value < 1024) ||
                 (form.destPort.value > 65535)) {
            alert("The destination port entered is " + form.srcPort.value +
                  ".  It must be in the range 1024-65535.");
            form.destPort.focus();
            return false;
        }
    }
    if (!isBlank(form.dscp.value)) {
        if (!(isNumeric(form.dscp.value))) {
            alert("The DSCP entered is " + form.dscp.value +
                  ".  It must be a positive integer.");
            form.dscp.focus();
            return false;
        }
        else if ((form.dscp.value < 0) || (form.dscp.value > 63)) {
            alert("The DSCP entered is " + form.dscp.value +
                  ".  It must be in the range 0-63.");
            form.dscp.focus();
            return false;
        }
    }
    // at this point success only depends on a correct date
    return checkDateFields(form);
}

// Reference: http://javascript.internet.com/forms/val-date.html
function checkDateFields(form) {
    var durationSeconds = 240;

    var localDate = new Date();
    var userMonth = localDate.getMonth() + 1;
    if (isBlank(form.startDofY.value)) {
        form.startDofY.value = localDate.getFullYear() + "-" +
                               userMonth + "-" + localDate.getDate();
    }
    var dOfYFields = form.startDofY.value.split("-");
    if (!checkDateOfYearFields(form.startDofY, dOfYFields)) {
        return false;
    }

    if (isBlank(form.startHourMinute.value)) {
        form.startHourMinute.value = localDate.getHours() + ":" +
                                     localDate.getMinutes();
    }
    var timeFields = form.startHourMinute.value.split(":");
    if (!checkTimeFields(form.startHourMinute, timeFields)) {
        return false;
    }
    if (!isBlank(form.durationHour.value)) {
        if (!(isNumeric(form.durationHour.value))) {
            alert("The duration hour entered is " + form.durationHour.value +
                  ".  This is not a number.");
            form.durationHour.focus();
            return false;
        }
        durationSeconds = form.durationHour.value * 3600;
    }

    reservationDate = new Date(dOfYFields[0], dOfYFields[1] - 1,
                               dOfYFields[2], timeFields[0],
                               timeFields[1], 0, 0);
    // convert local time to seconds since epoch
    var startTime = reservationDate.getTime()/1000;
    var endTime = startTime + durationSeconds;
    form.startTime.value = startTime;
    form.endTime.value = endTime;
    return true;
}


function checkListReservations(form) {

    var localDate = new Date();
    var userMonth = localDate.getMonth() + 1;
    // don't do anything if both blank
    if (!(isBlank(form.startDateSearch.value) && 
          isBlank(form.startTimeSearch.value))) {
        if (isBlank(form.startDateSearch.value)) {
            form.startDateSearch.value = localDate.getFullYear() + "-" +
                                   userMonth + "-" + localDate.getDate();
        }
        if (isBlank(form.startTimeSearch.value)) {
            form.startTimeSearch.value = "00:00";
        }
        var dOfYFields = form.startDateSearch.value.split("-");
        if (!checkDateOfYearFields(form.startDateSearch, dOfYFields)) {
            return false;
        }
        var timeFields = form.startTimeSearch.value.split(":");
        if (!checkTimeFields(form.startTimeSearch, timeFields)) {
            return false;
        }
        var searchDate = new Date(dOfYFields[0], dOfYFields[1] - 1,
                                  dOfYFields[2], timeFields[0],
                                  timeFields[1], 0, 0);
        // convert local time to seconds since epoch
        form.startTimeSeconds.value = searchDate.getTime()/1000;
    } else {
        form.startTimeSeconds.value = null;
    }
    // don't do anything if both blank
    if (!(isBlank(form.endDateSearch.value) && 
          isBlank(form.endTimeSearch.value))) {
        if (isBlank(form.endDateSearch.value)) {
            form.endDateSearch.value = localDate.getFullYear() + "-" +
                                   userMonth + "-" + localDate.getDate();
        }
        if (isBlank(form.endTimeSearch.value)) {
            form.endTimeSearch.value = "00:00";
        }
        var dOfYFields = form.endDateSearch.value.split("-");
        if (!checkDateOfYearFields(form.endDateSearch, dOfYFields)) {
            return false;
        }
        var timeFields = form.endTimeSearch.value.split(":");
        if (!checkTimeFields(form.endTimeSearch, timeFields)) {
            return false;
        }
        var searchDate = new Date(dOfYFields[0], dOfYFields[1] - 1,
                                  dOfYFields[2], timeFields[0],
                                  timeFields[1], 0, 0);
        // convert local time to seconds since epoch
        form.endTimeSeconds.value = searchDate.getTime()/1000;
    } else {
        form.endTimeSeconds.value = null;
    }
    return true;
}


function checkDateOfYearFields(formParam, dOfYFields) {
    if (!(isNumeric(dOfYFields[0]))) {
        alert("The start year entered is " + dOfYFields[0] +
              ".  This is not a number.");
        formParam.focus();
        return false;
    }
    if (dOfYFields[0].length != 4) {
        alert("The start year entered is " + dOfYFields[0] +
              ".  It must be four digits, e.g. 2007.");
        formParam.focus();
        return false;
    }
    if (!(isNumeric(dOfYFields[1].toString()))) {
        alert("The start month entered is " + dOfYFields[1] +
              ".  This is not a number.");
        formParam.focus();
        return false;
    }
    if (!(isNumeric(dOfYFields[2]))) {
        alert("The start date entered is " + dOfYFields[2] +
              ".  This is not a number.");
        formParam.focus();
        return false;
    }
    if ((dOfYFields[1] < 1) || (dOfYFields[1] > 12)) {
        alert("The month entered is " + dOfYFields[1] +
              ".  It must be between 1 and 12, inclusive.");
        formParam.focus();
        return false;
    }
    if ((dOfYFields[1] == 1 || dOfYFields[1] == 3 || dOfYFields[1] == 5 ||
         dOfYFields[1] == 7 || dOfYFields[1] == 8 || dOfYFields[1] == 10 || 
         dOfYFields[1] == 12) && (dOfYFields[2] > 31))
    {
        alert("For the month, " + dOfYFields[1] +
              ", the date must be less than 32.");
        formParam.focus();
        return false;
    }
    if ((dOfYFields[1] == 4 || dOfYFields[1] == 6 || dOfYFields[1] == 9 ||
         dOfYFields[1] == 11) && (dOfYFields[2] > 30))
    {
        alert("For the month, " + dOfYFields[1] +
              ", the date must be less than 31.");
        formParam.focus();
        return false;
    }
    if (dOfYFields[1] == 2) {
        if (isLeapYear(dOfYFields[0])) {
            if (dOfYFields[2] > 29) {
                alert("For the month of February in a leap year, " +
                      "the date must be less than 30.");
                formParam.focus();
                return false;
            }
        }
        else if (dOfYFields[2] > 28) {
            alert("For the month of February in a non-leap year, " +
                  "the date must be less than 29.");
            formParam.focus();
            return false;
        }
    }
    return true;
}

function checkTimeFields(formParam, timeFields) {
    if (!(isNumeric(timeFields[0]))) {
        alert("The start hour entered is " + timeFields[0] +
              ".  This is not a number.");
        formParam.focus();
        return false;
    }
    if (timeFields[0] < 0 || timeFields[0] > 23) {
        alert("The start hour entered is " + timeFields[0] +
              ".  It must be between 0 and 23, inclusive.");
        formParam.focus();
        return false;
    }
    if (!(isNumeric(timeFields[1]))) {
        alert("The start minute entered is " + timeFields[1] +
              ".  This is not a number.");
        formParam.focus();
        return false;
    }
    if (timeFields[1] < 0 || timeFields[1] > 59) {
        alert("The start minute entered is " + timeFields[1] +
              ".  It must be between 0 and 59, inclusive.");
        formParam.focus();
        return false;
    }
    return true;
}

// Checks validity of user profile form.
function checkProfileModification(form) {
    var valid = checkForRequired(form, profileModificationRequired);
    if (!valid) { return false; }
    return true;
}

// Checks validity of add user form.
function checkAddUser(form) {
    var valid = checkForRequired(form, addUserRequired);
    if (!valid) { return false; }

    if (!(isBlank(form.password.value))) {
        if (form.password.value != form.passwordConfirmation.value) {
            alert("Please enter the same new password twice for verification.");
            form.password.focus();
            return false;
        }
    }
    return true;
}

// check whether a year is a leap year
// Reference: http://javascript.internet.com/forms/val-date.html
function isLeapYear(intYear) {
    if (intYear % 100 == 0) {
        if (intYear % 400 == 0) { return true; }
    }
    else {
        if (intYear % 4 == 0) { return true; }
    }
    return false;
}

function isNumeric(s) {
   return( s.match(/(\d)+/) );
}

// From Javascript book, p. 264

function isBlank(s) {
    for (var i = 0; i < s.length; i++) {
        var c = s.charAt(i);
        if ((c != ' ') && (c != '\n') && (c != '')) return false;
    }
    return true;
}
