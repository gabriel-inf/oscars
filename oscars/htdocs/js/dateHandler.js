/*
Javascript object for handling dates and times
Last modified:  September 28, 2006
David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)
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

/* Methods:
init()
update(clock)
timeZoneOptions()
isStandardTime()
localTimeZone()
tzOption(tzOffset, localOffset, tzName)
UTCOffset()
isLeapYear( intYear )
*/

function DateHandler() {
}

DateHandler.monthName = ['January', 'February', 'March', 'April', 'May',
   'June', 'July', 'August', 'September', 'October', 'November', 'December'];

// from http://www.worldtimezone.com/utc/utc-1200.html
DateHandler.standardTzNames = {
    '+00:00': "UTC", '+01:00': "CET", '+02:00': "EET", '+03:00': "MSK",
    '+05:30': "IST", '+08:00': "AWST", '+09:00': "JST", '+09:30': "ACST",
    '+10:00': "AEST", '+12:00': "NZST", '-03:00': "BST", '-03:30': "NST",
    '-04:00': "AST", '-05:00': "EST", '-06:00': "CST", '-07:00': "MST",
    '-08:00': "PST", '-09:00': "AKST", '-10:00': "HST" }

// This is only approximate; too complex for now.
DateHandler.daylightTzNames = {
    '+00:00': "UTC", '+01:00': "WEDT", '+02:00': "CEDT", '+03:00': "EEDT",
    '+04:00': "MSK", '+05:30': "IST", '+08:00': "AWST", '+09:00': "JST",
    '+10:30': "ACDT", '+11:00': "AEDT", '+13:00': "NZDT", '-02:00': "BDT",
    '-02:30': "NDT", '-03:00': "ADT", '-04:00': "EDT", '-05:00': "CDT",
    '-06:00': "MDT", '-07:00': "PDT", '-08:00': "AKDT", '-10:00': "HST"
}


// Updates datetime element with current date (format: July 1, 2005 13:00).
DateHandler.prototype.updateClock = function(clock) {
    var localDate = new Date();
    var digits, readout = '';

    var currentMonth = localDate.getMonth();
    readout += DateHandler.monthName[currentMonth] + " " +
                    localDate.getDate() + ", " + localDate.getFullYear() + " ";

    digits = localDate.getHours();
    readout += (digits > 9 ? '' : '0') + digits + ':';
    digits = localDate.getMinutes();
    readout += (digits > 9 ? '' : '0') + digits;

    clock.innerHTML = readout;
}

// Init and updateClock are adapted from the DHTML Utopia book.
DateHandler.prototype.init = function() {
    var digits, readout = '';
    var localDate = new Date();
    var clock = document.getElementById('clock');

    var currentMonth = localDate.getMonth();
    readout += DateHandler.monthName[currentMonth] + " " +
                    localDate.getDate() + ", " + localDate.getFullYear() + " ";

    digits = localDate.getHours();
    readout += (digits > 9 ? '' : '0') + digits + ':';
    digits = localDate.getMinutes();
    readout += (digits > 9 ? '' : '0') + digits;

    clock.innerHTML = readout;
    setInterval(function() { DateHandler.updateClock(clock); }, 60000);
}

// get string with timezone options
DateHandler.prototype.timeZoneOptions = function() {
    var isStandard = this.isStandardTime();
    var optionsStr = '<select class="SOAP" name="origTimeZone">';
    var localOffset = this.UTCOffset();
    // if standard (not daylight savings) time
    if (isStandard) {
        for (var tzOffset in DateHandler.standardTzNames)  {
            optionsStr += this.tzOption(tzOffset, localOffset,
                                 DateHandler.standardTzNames[tzOffset]) + "\n";
        }
    }
    else {
        for (var tzOffset in DateHandler.daylightTzNames)  {
            optionsStr += this.tzOption(tzOffset, localOffset, 
                                 DateHandler.daylightTzNames[tzOffset]) + "\n";
        }
    }
    optionsStr += "</select>\n";
    return optionsStr;
}


DateHandler.prototype.isStandardTime = function() {
    // TODO:  not every time zone has the same time period during which
    //        it is in effect; for now, U.S.
    var localDate = new Date();
    var offset = -(localDate.getTimezoneOffset());
    // unlikely that daylight savings in effect on January 1.
    var standardTimeDate = new Date(localDate.getFullYear(), 0, 1, 0, 0, 0, 0);
    var standardOffset = -(standardTimeDate.getTimezoneOffset()); 
    return (offset == standardOffset);
}


// Prints local time zone setting
DateHandler.prototype.localTimeZone = function() {
    var localDate = new Date();
    var localTz = 'UTC' + this.UTCOffset() + ' (';
    if (this.isStandardTime()) {
        localTz += DateHandler.standardTzNames[this.UTCOffset()] + ')';
    }
    else { localTz += DateHandler.daylightTzNames[this.UTCOffset()] + ')'; }
    return localTz;
}


// returns string for one timezone option
DateHandler.prototype.tzOption = function(tz, localOffset, tzName) {
    var optionStr;

    optionStr = '<option value="' + tz + '"';
    if (tz == localOffset) { optionStr += " selected"; }
    optionStr += '>' + tz + ' (' + tzName + ')</option>';
    return optionStr;
}


// private functions

// format the time zone offset in MySQL [+/-]hhmm format (ex. +09:30, -05:00)
DateHandler.prototype.UTCOffset = function() {
    var localDate = new Date();
    var offset = -(localDate.getTimezoneOffset());
    var hours = offset / 60;
    var halfIndicator = (offset % 60) / 30;
    var offsetStr;

    if (offset >= 0) {
        offsetStr = '+';
        if (hours < 10) { offsetStr += '0'};
    }
    else {
        offsetStr = '-';
        hours = -hours;
        if (hours < 10) { offsetStr += '0'};
    }
    offsetStr += hours;
    offsetStr += ":";

    if (halfIndicator) { offsetStr += '30'; }
    else { offsetStr += '00'; }
    
    return offsetStr;
}


// check whether a year is a leap year
// Reference: http://javascript.internet.com/forms/val-date.html
DateHandler.prototype.isLeapYear = function(intYear) {
    if ( intYear % 100 == 0 ) {
        if ( intYear % 400 == 0 ) { return true; }
    }
    else {
        if ( intYear % 4 == 0 ) { return true; }
    }
    return false;
}
