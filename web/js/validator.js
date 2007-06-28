/*
validator.js:      Javascript functions for form validation
Last modified:  February 27, 2007
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
    'UserModify' : checkProfileModification,
    'UserAdd' : checkAddUser
}

// TODO:  objects
var loginRequired = {
    'userName': "Please enter your user name.",
    'password': "Please enter your password."
}

var reservationRequired = {
    'srcHost': "Please enter starting host name, or its IP address, in the 'Source' field.",
    'destHost':  "Please enter destination host name, or its IP address, in the 'Destination' field.",
    'bandwidth': "Please enter the amount of bandwidth you require in the 'Bandwidth' field.",
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
function checkForRequired( form, required ) {
    for (field in required) {
        if ( form[field] && isBlank(form[field].value) ) {
            alert( required[field] );
            form[field].focus();
            return false;
        }
    }
    return true;
}

// Checks validity of login form.
function checkLogin( form ) {
    return checkForRequired( form, loginRequired );
}

// Checks validity of create reservation form.
function checkReservation( form ) {
    var valid = checkForRequired( form, reservationRequired );
    if (!valid) { return false; }

    if (!(isNumeric(form.bandwidth.value))) {
        alert( "The bandwidth entered is " + form.bandwidth.value + ".  It must be a positive integer." );
        form.bandwidth.focus();
        return false;
    }
    else if ( (form.bandwidth.value < 1 ) || (form.bandwidth.value > 10000)) {
        alert( "The bandwidth entered is " + form.bandwidth.value + ".  It must be in the range 1-10000 Mbps." );
        form.bandwidth.focus();
        return false;
    }

    if ( form.srcHost.value == form.destHost.value ) {
        alert( "The host names or IP addresses for the source and destination cannot be the same." );
        form.srcHost.focus();
        return false;
    }
    // TODO:  needs more work
    var sections = form.srcHost.value.split('/');
    if ((sections.length > 1) && (sections[1] < 24)) {
        alert( "Only CIDR blocks >= 24 (class C) are accepted." );
        form.srcHost.focus();
        return false;
    }
    var sections = form.destHost.value.split('/');
    if ((sections.length > 1) && (sections[1] < 24)) {
        alert( "Only CIDR blocks >= 24 (class C) are accepted." );
        form.destHost.focus();
        return false;
    }

    // check non-required fields if a value has been entered
    if ( !isBlank(form.srcPort.value) ) {
        if (!(isNumeric(form.srcPort.value))) {
            alert( "The source port entered is " + form.srcPort.value + ".  It must be a positive integer." );
            form.srcPort.focus();
            return false;
        }
        else if ( (form.srcPort.value < 1024) ||
                (form.srcPort.value > 65535) ) {
            alert( "The source port entered is " + form.srcPort.value + ".  It must be in the range 1024-65535." );
            form.srcPort.focus();
            return false;
        }
    }
    if ( !isBlank(form.destPort.value) ) {
        if (!(isNumeric(form.destPort.value))) {
            alert( "The destination port entered is " + form.srcPort.value + ".  It must be a positive integer." );
            form.destPort.focus();
            return false;
        }
        else if ( (form.destPort.value < 1024) ||
                (form.destPort.value > 65535) ) {
            alert( "The destination port entered is " + form.srcPort.value + ".  It must be in the range 1024-65535." );
            form.destPort.focus();
            return false;
        }
    }
    if ( !isBlank(form.dscp.value) ) {
        if (!(isNumeric(form.dscp.value))) {
            alert( "The DSCP entered is " + form.dscp.value + ".  It must be a positive integer." );
            form.dscp.focus();
            return false;
        }
        else if ( (form.dscp.value < 0) || (form.dscp.value > 63) ) {
            alert( "The DSCP entered is " + form.dscp.value + ".  It must be in the range 0-63." );
            form.dscp.focus();
            return false;
        }
    }
    // at this point success only depends on a correct date
    return checkDateFields(form);
}

// Reference: http://javascript.internet.com/forms/val-date.html
function checkDateFields(form) {
    var durationMilliseconds = 240000;

    var localDate = new Date();
    if ( isBlank(form.startYear.value) ) {
        form.startYear.value = localDate.getFullYear();
    }
    else {
        if (!(isNumeric(form.startYear.value))) {
            alert("The start year entered is " + form.startYear.value + ".  This is not a number.");
            form.startYear.focus();
            return false;
        }
        if ( form.startYear.value.length != 4 ) {
            alert("The start year entered is " + form.startYear.value + ".  It must be four digits, e.g. 2007.");
            form.startYear.focus();
            return false;
        }
    }

    if ( isBlank(form.startMonth.value) ) {
        form.startMonth.value = localDate.getMonth() + 1;
    }
    else {
        if (!(isNumeric(form.startMonth.value))) {
            alert("The start month entered is " + form.startMonth.value + ".  This is not a number.");
            form.startDate.focus();
            return false;
        }
    }

    if ( isBlank(form.startDate.value) ) {
        form.startDate.value = localDate.getDate();
    }
    else {
        if (!(isNumeric(form.startDate.value))) {
            alert("The start date entered is " + form.startDate.value + ".  This is not a number.");
            form.startDate.focus();
            return false;
        }
    }

    if ( isBlank(form.startHour.value) ) {
        form.startHour.value = localDate.getHours();
    }
    else {
        if (!(isNumeric(form.startHour.value))) {
            alert("The start hour entered is " + form.startHour.value + ".  This is not a number.");
            form.startHour.focus();
            return false;
        }
        if ( form.startHour.value < 0 || form.startHour.value > 23 ) {
            alert("The start hour entered is " + form.startHour.value + ".  It must be between 0 and 23, inclusive.");
            form.startHour.focus();
            return false;
        }
    }

    if ( isBlank(form.startMinute.value) ) {
        form.startMinute.value = localDate.getMinutes();
    }

    if ( !isBlank(form.durationHour.value) ) {
        if (!(isNumeric(form.durationHour.value))) {
            alert("The duration hour entered is " + form.durationHour.value + ".  This is not a number.");
            form.durationHour.focus();
            return false;
        }
        durationMilliseconds = form.durationHour.value * 3600000;
    }

    if ( ( form.startMonth.value == 1 || form.startMonth.value == 3 || 
           form.startMonth.value == 5 || form.startMonth.value == 7 || 
           form.startMonth.value == 8 || form.startMonth.value == 10 || 
           form.startMonth.value == 12 ) && ( form.startDate.value > 31 ) )
    {
        alert("For the month, " + form.startMonth.value + ", the date must be less than 32.");
        form.startDate.focus();
        return false;
    }

    if ( ( form.startMonth.value == 4 || form.startMonth.value == 6 || 
           form.startMonth.value == 9 || form.startMonth.value == 11 ) && 
         ( form.startDate.value > 30 ) )
    {
        alert("For the month, " + form.startMonth.value + ", the date must be less than 31.");
        form.startDate.focus();
        return false;
    }

    if ( form.startMonth.value == 2) {
        if (isLeapYear( form.startYear.value )) {
            if ( form.startDate.value > 29 ) {
                alert("For the month of February in a leap year, the date must be less than 30.");
                form.startDate.focus();
                return false;
            }
        }
        else if (form.startDate.value > 28) {
            alert("For the month of February in a non-leap year, the date must be less than 29.");
            form.startDate.focus();
            return false;
        }
    }
    reservationDate = new Date(form.startYear.value, form.startMonth.value - 1,
                               form.startDate.value, form.startHour.value,
                               form.startMinute.value, 0, 0);
    // convert local time to milliseconds since epoch
    var startTime = reservationDate.getTime();

    if (form.persistent && form.persistent.checked) {
        // For now, persistent reservation lasts 4 years
        durationMilliseconds = 86400 * 365 * 4 * 1000;
    }
    var endTime = startTime + durationMilliseconds;
    form.startTime.value = startTime;
    form.endTime.value = endTime;
    return true;
}


// Checks validity of user profile form.
function checkProfileModification( form )
{
    var valid = checkForRequired( form, profileModificationRequired );
    if (!valid) { return false; }
    return true;
}

// Checks validity of add user form.
function checkAddUser( form )
{
    var valid = checkForRequired( form, addUserRequired );
    if (!valid) { return false; }

    if ( !(isBlank(form.password.value)) ) {
        if ( form.password.value != form.passwordConfirmation.value ) {
            alert( "Please enter the same new password twice for verification." );
            form.password.focus();
            return false;
        }
    }
    return true;
}

// check whether a year is a leap year
// Reference: http://javascript.internet.com/forms/val-date.html
function isLeapYear(intYear) {
    if ( intYear % 100 == 0 ) {
        if ( intYear % 400 == 0 ) { return true; }
    }
    else {
        if ( intYear % 4 == 0 ) { return true; }
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
