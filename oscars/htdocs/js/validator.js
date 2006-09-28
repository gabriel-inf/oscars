/*
validator.js:      Javascript functions for form validation
Last modified:  September 28, 2006
David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)
*/

/*
Copyright (c) 2006, The Regents of the University of California, through
Lawrence Berkeley National Laboratory (subject to receipt of any required
approvals from the U.S. Dept. of Energy). All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

(1) Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

(2) Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

(3) Neither the name of the University of California, Lawrence Berkeley
    National Laboratory, U.S. Dept. of Energy nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

You are under no obligation whatsoever to provide any bug fixes, patches,
or upgrades to the features, functionality or performance of the source
code ("Enhancements") to anyone; however, if you choose to make your
Enhancements available either publicly, or directly to Lawrence Berkeley
National Laboratory, without imposing a separate written license agreement
for such Enhancements, then you hereby grant the following license: a
non-exclusive, royalty-free perpetual license to install, use, modify,
prepare derivative works, incorporate into other computer software,
distribute, and sublicense such enhancements or derivative works thereof,
in binary and source code form. */

/* List of functions:
checkForRequired(form, required)
checkLogin(form)
checkReservation(form)
checkDateFields(form)
checkProfileModification(form)
checkAddUser(form)
isNumeric(value)
isBlank(str)
*/

// TODO:  objects
var loginRequired = {
    'login': "Please enter your user name.",
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
    'selectedUser': "Please enter the new user's distinguished name.",
    'passwordNewOnce': "Please enter the new user's password.",
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
function checkLogin( form )
{
    return checkForRequired( form, loginRequired );
}

// Checks validity of create reservation form.
function checkReservation( form )
{
    var valid = checkForRequired( form, reservationRequired );
    if (!valid) { return false; }

    // Temporary hack: (TODO:  FIX)
    if ( (form.login.value == 'dtyu@bnl.gov') ||
         (form.login.value == 'wenji@fnal.gov'))
    {
        if (form.ingressRouter.value && (form.ingressRouter.value != 'chi-sl-sdn1'))
        {
             alert( "Only 'chi-sl-sdn1', or a blank value, is permissible in the 'Ingress loopback' field." );
             form.ingressRouter.focus();
             return false;
        }
        if (form.egressRouter.value && (form.egressRouter.value != 'chi-sl-sdn1'))
        {
             alert( "Only 'chi-sl-sdn1', or a blank value, is permissible in the 'Egress loopback' field." );
             form.egressRouter.focus();
             return false;
        }
    }
    if (!(isNumeric(form.bandwidth.value))) {
        alert( "The bandwidth must be a positive integer." );
        form.bandwidth.focus();
        return false;
    }
    else if ( (form.bandwidth.value < 1 ) || (form.bandwidth.value > 10000)) {
        alert( "The amount of bandwidth must be in the range 1-10000 Mbps." );
        form.bandwidth.focus();
        return false;
    }

    if ( form.srcHost.value == form.destHost.value ) {
        alert( "Please provide different host names or IP addresses for the source and destination." );
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
            alert( "The source port must be a positive integer." );
            form.srcPort.focus();
            return false;
        }
        else if ( (form.srcPort.value < 1024) ||
                (form.srcPort.value > 65535) ) {
            alert( "The source port, if given, must be in the range 1024-65535." );
            form.srcPort.focus();
            return false;
        }
    }
    if ( !isBlank(form.destPort.value) ) {
        if (!(isNumeric(form.destPort.value))) {
            alert( "The destination port must be a positive integer." );
            form.destPort.focus();
            return false;
        }
        else if ( (form.destPort.value < 1024) ||
                (form.destPort.value > 65535) ) {
            alert( "The destination port, if given, must be in the range 1024-65535." );
            form.destPort.focus();
            return false;
        }
    }
    if ( !isBlank(form.dscp.value) ) {
        if (!(isNumeric(form.dscp.value))) {
            alert( "The DSCP must be a positive integer." );
            form.dscp.focus();
            return false;
        }
        else if ( (form.dscp.value < 0) || (form.dscp.value > 63) ) {
            alert( "The DSCP, if given, must be in the range 0-63." );
            form.dscp.focus();
            return false;
        }
    }
    // at this point success only depends on a correct date
    return checkDateFields(form);
}

// Reference: http://javascript.internet.com/forms/val-date.html
function checkDateFields(form) {
    var defaultYear = 0;
    var defaultMonth = 0;
    var defaultDate = 0;
    var defaultHour = 0;
    var defaultMinute = 0;
    var durationSeconds = 240;

    var localDate = new Date();
    if ( isBlank(form.startYear.value) ) {
        form.startYear.value = localDate.getFullYear();
        defaultYear = 1;
    }
    else {
        if (!(isNumeric(form.startYear.value))) {
            alert("The reservation start year is not a number. Please check again.");
            form.startYear.focus();
            return false;
        }
        if ( form.startYear.value.length != 4 ) {
            alert("The reservation start year must be in four digits.");
            form.startYear.focus();
            return false;
        }
    }

    if ( isBlank(form.startMonth.value) ) {
        form.startMonth.value = localDate.getMonth();
        defaultMonth = 0;
    }
    else {
        if ( !(isNumeric(form.startMonth.value))) {
            alert("The reservation start month is not a number. Please check again.");
            form.startMonth.focus();
            return false;
        }
        form.startMonth.value = parseInt(form.startMonth.value) - 1;
        if ( form.startMonth.value < 0 || form.startMonth.value > 11 ) {
            alert("The reservation start month is out of proper range. Please check again.");
            form.startMonth.focus();
            return false;
        }
    }

    if ( isBlank(form.startDate.value) ) {
        form.startDate.value = localDate.getDate();
        defaultDate = 1;
    }
    else {
        if (!(isNumeric(form.startDate.value))) {
            alert("The reservation start date is not a number. Please check again.");
            form.startDate.focus();
            return false;
        }
    }

    if ( isBlank(form.startHour.value) ) {
        form.startHour.value = localDate.getHours();
        defaultHour = 1;
    }
    else {
        if (!(isNumeric(form.startHour.value))) {
            alert("The reservation start hour is not a number. Please check again.");
            form.startHour.focus();
            return false;
        }
        if ( form.startHour.value < 0 || form.startHour.value > 23 ) {
            alert("The reservation start hour is out of proper range. Please check again.");
            form.startHour.focus();
            return false;
        }
    }

    if ( isBlank(form.startMinute.value) ) {
        form.startMinute.value = localDate.getMinutes();
        defaultMinute = 1;
    }

    if ( isBlank(form.durationHour.value) ) {
        form.durationHour.value = 0.05;
    }
    else {
        if (!(isNumeric(form.durationHour.value))) {
            alert("The reservation duration hour is not a number. Please check again.");
            form.durationHour.focus();
            return false;
        }
        durationSeconds = form.durationHour.value * 3600;
    }

    if (!defaultMonth || !defaultDate) {
        if ( ( form.startMonth.value == 1 || form.startMonth.value == 3 || 
              form.startMonth.value == 5 || form.startMonth.value == 7 || 
              form.startMonth.value == 8 || form.startMonth.value == 10 || 
              form.startMonth.value == 12 ) && ( form.startDate.value > 31 || 
               form.startDate.value < 1 ) )
        {
            alert("The reservation start date is out of proper range. Please check again.");
            form.startDate.focus();
            return false;
        }

        if ( ( form.startMonth.value == 4 || form.startMonth.value == 6 || 
               form.startMonth.value == 9 || form.startMonth.value == 11 ) && 
             ( form.startDate.value > 30 || form.startDate.value < 1 ) )
        {
            alert("The reservation start date is out of proper range. Please check again.");
            form.startDate.focus();
            return false;
        }

        if ( form.startMonth.value == 2 ) {
            if ( form.startDate.value < 1 ) {
                alert("The reservation start date is out of proper range. Please check again.");
                form.startDate.focus();
                return false;
            }
        }
    }
    if (!defaultYear || !defaultDate) {
        dateHandler = new DateHandler();
        dateHandler.init();
        if ( dateHandler.isLeapYear(form.startYear.value) ) {
            if ( form.startDate.value > 29 ) {
                alert("The reservation start date is out of proper range. Please check again.");
                form.startDate.focus();
                return false;
            }
        }
        else {
            if ( form.startDate.value > 28 ) {
                alert("The reservation start date is out of proper range. Please check again.");
                form.startDate.focus();
                return false;
            }
        }
    }
   reservationDate = new Date(form.startYear.value, form.startMonth.value,
                                form.startDate.value, form.startHour.value,
                                form.startMinute.value, 0, 0);
    form.startMonth.value = parseInt(form.startMonth.value) + 1;
    // convert local time to seconds since epoch
    var startTime = reservationDate.getTime() / 1000;

    if (form.persistent && form.persistent.checked) {
        // For now, persistent reservation lasts 4 years
        durationSeconds = 86400 * 365 * 4;
    }
    var endTime = startTime + durationSeconds;
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

    if ( !(isBlank(form.passwordNewOnce.value)) ) {
        if ( form.passwordNewOnce.value != form.passwordNewTwice.value ) {
            alert( "Please enter the same new password twice for verification." );
            form.passwordNewOnce.focus();
            return false;
        }
    }
    return true;
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
