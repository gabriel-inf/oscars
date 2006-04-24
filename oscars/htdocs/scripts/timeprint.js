/*
Javascript functions for getting dates and times in various formats
Last modified:  April 24, 2006
Soo-yeon Hwang (dapi@umich.edu)
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
print_current_date( frame )
date_str()
check_date_fields( form )
time_zone_options()
local_time_settings()
is_standard_time()
UTC_offset()
time_zone_option( tz_offset, local_offset, tz_name )
is_leap_year( intYear )
*/


var month_name = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

// from http://www.worldtimezone.com/utc/utc-1200.html
var standard_tz_name = {
    '+00:00': "UTC", '+01:00': "CET", '+02:00': "EET", '+03:00': "MSK",
    '+05:30': "IST", '+08:00': "AWST", '+09:00': "JST", '+09:30': "ACST",
    '+10:00': "AEST", '+12:00': "NZST", '-03:00': "BST", '-03:30': "NST",
    '-04:00': "AST", '-05:00': "EST", '-06:00': "CST", '-07:00': "MST",
    '-08:00': "PST", '-09:00': "AKST", '-10:00': "HST" }

// This is only approximate; too complex for now.
var daylight_tz_name = {
    '+00:00': "UTC", '+01:00': "WEDT", '+02:00': "CEDT", '+03:00': "EEDT",
    '+04:00': "MSK", '+05:30': "IST", '+08:00': "AWST", '+09:00': "JST",
    '+10:30': "ACDT", '+11:00': "AEDT", '+13:00': "NZDT", '-02:00': "BDT",
    '-02:30': "NDT", '-03:00': "ADT", '-04:00': "EDT", '-05:00': "CDT",
    '-06:00': "MDT", '-07:00': "PDT", '-08:00': "AKDT", '-10:00': "HST"
}


// Only called from initial login page.
function print_current_date(frame) {
    frame.write( date_str() );
}


// Outputs string with current date (format: July 1, 2005 13:00).
function date_str() {
    var local_date = new Date();
    var current_month = local_date.getMonth();

    var current_minutes = local_date.getMinutes();
    var date_str = month_name[current_month] + " " + local_date.getDate() + ", " + local_date.getFullYear() + " " + local_date.getHours() + ":";

    if (current_minutes < 10) { date_str += "0" } ;
    date_str += current_minutes;
    return date_str;
}


// Reference: http://javascript.internet.com/forms/val-date.html
function check_date_fields( form ) {
    var default_year = 0;
    var default_month = 0;
    var default_date = 0;
    var default_hour = 0;
    var default_minute = 0;
    var duration_in_seconds = 240;

    var local_date = new Date();
    if ( is_blank(form.startYear.value) ) {
        form.startYear.value = local_date.getFullYear();
        default_year = 1;
    }
    else {
        if (!(is_numeric(form.startYear.value))) {
            alert( "The reservation start year is not a number. Please check again." );
            form.startYear.focus();
            return false;
        }
        if ( form.startYear.value.length != 4 ) {
            alert( "The reservation start year must be in four digits." );
            form.startYear.focus();
            return false;
        }
    }

    if ( is_blank(form.startMonth.value) ) {
        form.startMonth.value = local_date.getMonth();
        default_month = 0;
    }
    else {
        if ( !(is_numeric(form.startMonth.value))) {
            alert( "The reservation start month is not a number. Please check again." );
            form.startMonth.focus();
            return false;
        }
        form.startMonth.value = parseInt(form.startMonth.value) - 1;
        if ( form.startMonth.value < 0 || form.startMonth.value > 11 ) {
            alert( "The reservation start month is out of proper range. Please check again." );
            form.startMonth.focus();
            return false;
        }
    }

    if ( is_blank(form.startDate.value) ) {
        form.startDate.value = local_date.getDate();
        default_date = 1;
    }
    else {
        if (!(is_numeric(form.startDate.value))) {
            alert( "The reservation start date is not a number. Please check again." );
            form.startDate.focus();
            return false;
        }
    }

    if ( is_blank(form.startHour.value) ) {
        form.startHour.value = local_date.getHours();
        default_hour = 1;
    }
    else {
        if (!(is_numeric(form.startHour.value))) {
            alert( "The reservation start hour is not a number. Please check again." );
            form.startHour.focus();
            return false;
        }
        if ( form.startHour.value < 0 || form.startHour.value > 23 ) {
            alert( "The reservation start hour is out of proper range. Please check again." );
            form.startHour.focus();
            return false;
        }
    }

    if ( is_blank(form.startMinute.value) ) {
        form.startMinute.value = local_date.getMinutes();
        default_minute = 1;
    }

    if ( is_blank(form.durationHour.value) ) {
        form.durationHour.value = 0.05;
    }
    else {
        if (!(is_numeric(form.durationHour.value))) {
            alert( "The reservation duration hour is not a number. Please check again." );
            form.durationHour.focus();
            return false;
        }
        duration_in_seconds = form.durationHour.value * 3600;
    }

    if (!default_month || !default_date) {
        if ( ( form.startMonth.value == 1 || form.startMonth.value == 3 || 
              form.startMonth.value == 5 || form.startMonth.value == 7 || 
              form.startMonth.value == 8 || form.startMonth.value == 10 || 
              form.startMonth.value == 12 ) && ( form.startDate.value > 31 || 
               form.startDate.value < 1 ) )
        {
            alert( "The reservation start date is out of proper range. Please check again." );
            form.startDate.focus();
            return false;
        }

        if ( ( form.startMonth.value == 4 || form.startMonth.value == 6 || 
               form.startMonth.value == 9 || form.startMonth.value == 11 ) && 
             ( form.startDate.value > 30 || form.startDate.value < 1 ) )
        {
            alert( "The reservation start date is out of proper range. Please check again." );
            form.startDate.focus();
            return false;
        }

        if ( form.startMonth.value == 2 ) {
            if ( form.startDate.value < 1 ) {
                alert( "The reservation start date is out of proper range. Please check again." );
                form.startDate.focus();
                return false;
            }
        }
    }
    if (!default_year || !default_date) {
        if ( is_leap_year(form.startYear.value) ) {
            if ( form.startDate.value > 29 ) {
                alert( "The reservation start date is out of proper range. Please check again." );
                form.startDate.focus();
                return false;
            }
        }
        else {
            if ( form.startDate.value > 28 ) {
                alert( "The reservation start date is out of proper range. Please check again." );
                form.startDate.focus();
                return false;
            }
        }
    }
   reservation_date = new Date(form.startYear.value, form.startMonth.value,
                                form.startDate.value, form.startHour.value,
                                form.startMinute.value, 0, 0);
    form.startMonth.value = parseInt(form.startMonth.value) + 1;
    // convert local time to seconds since epoch
    var start_time = reservation_date.getTime() / 1000;

    if (form.persistent && form.persistent.checked) {
        // For now, persistent reservation lasts 4 years
        duration_in_seconds = 86400 * 365 * 4;
    }
    var end_time = start_time + duration_in_seconds;
    form.startTime.value = start_time;
    form.endTime.value = end_time;
    return true;
}


// get string with timezone options
function time_zone_options() {
    var is_standard = is_standard_time();
    var options_str = '<select class="SOAP" name="origTimeZone">';
    var local_offset = UTC_offset();
    // if standard (not daylight savings) time
    if (is_standard) {
        for (var tz_offset in standard_tz_name)  {
            options_str += time_zone_option(tz_offset, local_offset, standard_tz_name[tz_offset]) + "\n";
        }
    }
    else {
        for (var tz_offset in daylight_tz_name)  {
            options_str += time_zone_option(tz_offset, local_offset, daylight_tz_name[tz_offset]) + "\n";
        }
    }
    options_str += "</select>\n";
    return options_str;
}


function is_standard_time() {
    // TODO:  not every time zone has the same time period during which
    //        it is in effect; for now, U.S.
    var local_date = new Date();
    var offset = -(local_date.getTimezoneOffset());
    // unlikely that daylight savings in effect on January 1.
    var standard_time_date = new Date(local_date.getFullYear(), 0, 1, 0, 0, 0, 0);
    var standard_offset = -(standard_time_date.getTimezoneOffset()); 
    return (offset == standard_offset);
}


// Prints local time zone setting
function local_time_zone() {
    var local_date = new Date();
    var local_tz = 'UTC' + UTC_offset() + ' (';
    if (is_standard_time()) {
        local_tz += standard_tz_name[UTC_offset()] + ')';
    }
    else { local_tz += daylight_tz_name[UTC_offset()] + ')'; }
    return local_tz;
}


// private functions

// format the time zone offset in MySQL [+/-]hhmm format (ex. +09:30, -05:00)
function UTC_offset() {
    var local_date = new Date();
    var offset = -(local_date.getTimezoneOffset());
    var hours = offset / 60;
    var half_indicator = (offset % 60) / 30;
    var offset_str;

    if (offset >= 0) {
        offset_str = '+';
        if (hours < 10) { offset_str += '0'};
    }
    else {
        offset_str = '-';
        hours = -hours;
        if (hours < 10) { offset_str += '0'};
    }
    offset_str += hours;
    offset_str += ":";

    if (half_indicator) { offset_str += '30'; }
    else { offset_str += '00'; }
    
    return offset_str;
}


// returns string for one timezone option
function time_zone_option(tz, local_offset, tz_name) {
    var option_str;

    option_str = '<option value="' + tz + '"';
    if (tz == local_offset) { option_str += " selected"; }
    option_str += '>' + tz + ' (' + tz_name + ')</option>';
    return option_str;
}


// check whether a year is a leap year
// Reference: http://javascript.internet.com/forms/val-date.html
function is_leap_year( intYear ) {
    if ( intYear % 100 == 0 ) {
        if ( intYear % 400 == 0 ) { return true; }
    }
    else {
        if ( intYear % 4 == 0 ) { return true; }
    }
    return false;
}
