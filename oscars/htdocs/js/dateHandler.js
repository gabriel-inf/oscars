/*
Javascript object for handling dates and times
Last modified:  July 15, 2006
David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)
*/

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


// Init and update are adapted from the DHTML Utopia book.
DateHandler.prototype.init = function() {
    var clock = document.getElementById('clock');
    this.update(clock);
    setInterval(function() { this.update(clock); }, 60000);
}

// Updates datetime element with current date (format: July 1, 2005 13:00).
DateHandler.prototype.update = function(clock) {
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
