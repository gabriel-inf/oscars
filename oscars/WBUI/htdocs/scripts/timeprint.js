/*
Javascript functions for getting dates and times in various formats
Last modified: November 8, 2005
Soo-yeon Hwang (dapi@umich.edu)
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
print_current_date(frame)
date_str()
check_date_fields(form)
tz_option_list()
time_settings_example()
is_standard_time()
UTC_offset()
tz_option(tz_offset, local_offset, tz_name)
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
function print_current_date(frame)
{
    frame.write( date_str() );
}


// Outputs string with current date (format: July 1, 2005 13:00).
function date_str()
{
    var local_date = new Date();
    var current_month = local_date.getMonth();

    var current_minutes = local_date.getMinutes();
    var date_str = month_name[current_month] + " " + local_date.getDate() + ", " + local_date.getFullYear() + " " + local_date.getHours() + ":";

    if (current_minutes < 10) { date_str += "0" } ;
    date_str += current_minutes;
    return date_str;
}


// Reference: http://javascript.internet.com/forms/val-date.html
function check_date_fields( form )
{
    var default_year = 0;
    var default_month = 0;
    var default_date = 0;
    var default_hour = 0;
    var default_minute = 0;

    var local_date = new Date();
    if ( isblank(form.start_year.value) ) {
        form.start_year.value = local_date.getFullYear();
        default_year = 1;
    }
    else {
        if (!(is_numeric(form.start_year.value))) {
            alert( "The reservation start year is not a number. Please check again." );
            form.start_year.focus();
            return false;
        }
        if ( form.start_year.value.length != 4 ) {
            alert( "The reservation start year must be in four digits." );
            form.start_year.focus();
            return false;
        }
    }

    if ( isblank(form.start_month.value) ) {
        form.start_month.value = local_date.getMonth();
        default_month = 0;
    }
    else {
        if ( !(is_numeric(form.start_month.value))) {
            alert( "The reservation start month is not a number. Please check again." );
            form.start_month.focus();
            return false;
        }
        form.start_month.value = parseInt(form.start_month.value) - 1;
        if ( form.start_month.value < 0 || form.start_month.value > 11 ) {
            alert( "The reservation start month is out of proper range. Please check again." );
            form.start_month.focus();
            return false;
        }
    }

    if ( isblank(form.start_date.value) ) {
        form.start_date.value = local_date.getDate();
        default_date = 1;
    }
    else {
        if (!(is_numeric(form.start_date.value))) {
            alert( "The reservation start date is not a number. Please check again." );
            form.start_date.focus();
            return false;
        }
    }

    if ( isblank(form.start_hour.value) ) {
        form.start_hour.value = local_date.getHours();
        default_hour = 1;
    }
    else {
        if (!(is_numeric(form.start_hour.value))) {
            alert( "The reservation start hour is not a number. Please check again." );
            form.start_hour.focus();
            return false;
        }
        if ( form.start_hour.value < 0 || form.start_hour.value > 23 ) {
            alert( "The reservation start hour is out of proper range. Please check again." );
            form.start_hour.focus();
            return false;
        }
    }

    if ( isblank(form.start_minute.value) ) {
        form.start_minute.value = local_date.getMinutes();
        default_minute = 1;
    }

    if ( isblank(form.duration_hour.value) ) {
        form.duration_hour.value = 0.05;
    }
    else {
        if (!(is_numeric(form.duration_hour.value))) {
            alert( "The reservation duration hour is not a number. Please check again." );
            form.duration_hour.focus();
            return false;
        }
    }

    if (!default_month || !default_date) {
        if ( ( form.start_month.value == 1 || form.start_month.value == 3 || 
              form.start_month.value == 5 || form.start_month.value == 7 || 
              form.start_month.value == 8 || form.start_month.value == 10 || 
              form.start_month.value == 12 ) && ( form.start_date.value > 31 || 
               form.start_date.value < 1 ) )
        {
            alert( "The reservation start date is out of proper range. Please check again." );
            form.start_date.focus();
            return false;
        }

        if ( ( form.start_month.value == 4 || form.start_month.value == 6 || 
               form.start_month.value == 9 || form.start_month.value == 11 ) && 
             ( form.start_date.value > 30 || form.start_date.value < 1 ) )
        {
            alert( "The reservation start date is out of proper range. Please check again." );
            form.start_date.focus();
            return false;
        }

        if ( form.start_month.value == 2 ) {
            if ( form.start_date.value < 1 ) {
                alert( "The reservation start date is out of proper range. Please check again." );
                form.start_date.focus();
                return false;
            }
        }
    }
    if (!default_year || !default_date) {
        if ( is_leap_year(form.start_year.value) ) {
            if ( form.start_date.value > 29 ) {
                alert( "The reservation start date is out of proper range. Please check again." );
                form.start_date.focus();
                return false;
            }
        }
        else {
            if ( form.start_date.value > 28 ) {
                alert( "The reservation start date is out of proper range. Please check again." );
                form.start_date.focus();
                return false;
            }
        }
    }
   reservation_date = new Date(form.start_year.value,
                                form.start_month.value,
                                form.start_date.value,
                                form.start_hour.value,
                                form.start_minute.value,
                                0, 0);
    form.start_month.value = parseInt(form.start_month.value) + 1;
    // convert local time to seconds since epoch
    form.reservation_start_time.value = reservation_date.getTime() / 1000;

    if (form.persistent && form.persistent.checked) {
        form.duration_hour.value = Math.pow(2, 31) - 1;
    }
    return true;
}


// get string with timezone options
function tz_option_list() {
    var is_standard = is_standard_time();
    var options_str = '<select name="reservation_time_zone">';
    var local_offset = UTC_offset();
    // if standard (not daylight savings) time
    if (is_standard) {
        for (var tz_offset in standard_tz_name)  {
            options_str += tz_option(tz_offset, local_offset, standard_tz_name[tz_offset]) + "\n";
        }
    }
    else {
        for (var tz_offset in daylight_tz_name)  {
            options_str += tz_option(tz_offset, local_offset, standard_tz_name[tz_offset]) + "\n";
        }
    }
    options_str += "</select>\n";
    return options_str;
}


function is_standard_time()
{
    // TODO:  not every time zone has the same time period during which
    //        it is in effect; for now, U.S.
    var local_date = new Date();
    var offset = -(local_date.getTimezoneOffset());
    // unlikely that daylight savings in effect on January 1.
    var standard_time_date = new Date(local_date.getFullYear(), 0, 1, 0, 0, 0, 0);
    var standard_offset = -(standard_time_date.getTimezoneOffset()); 
    return (offset == standard_offset);
}


// Prints year, date, time, time zone, and duration for reservation example.
// NOTE:  For production use, start time should probably be in the future.
function time_settings_example() {
    var local_date = new Date();
    var dfields = new Array();

    dfields[0] = local_date.getFullYear();
    dfields[1] = local_date.getMonth() + 1;
    dfields[2] = local_date.getDate();
    dfields[3] = local_date.getHours();
    dfields[4] = local_date.getMinutes();
    dfields[5] = 'UTC' + UTC_offset() + ' (';
    if (is_standard_time()) {
        dfields[5] += standard_tz_name[UTC_offset()] + ')';
    }
    else { dfields[5] += daylight_tz_name[UTC_offset()] + ')'; }
    dfields[6] = 0.05;
    dfields[7] = ' ';
    var example_str = "";
    for (var i=0 ; i < 6; i++) {
        if (dfields[i] < 10) {
            dfields[i] = '0' + dfields[i];
        }
    }
    for (var i=0 ; i < 7; i++) {
        example_str += (' <td>' + dfields[i] + '</td>' );
    }
    return example_str;
}


// private functions

// format the time zone offset in MySQL [+/-]hhmm format (ex. +09:30, -05:00)
function UTC_offset()
{
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
function tz_option(tz, local_offset, tz_name)
{
    var option_str;

    option_str = '<option value="' + tz + '"';
    if (tz == local_offset) { option_str += " selected"; }
    option_str += '>' + tz + ' (' + tz_name + ')</option>';
    return option_str;
}


// check whether a year is a leap year
// Reference: http://javascript.internet.com/forms/val-date.html
function is_leap_year( intYear )
{
    if ( intYear % 100 == 0 ) {
        if ( intYear % 400 == 0 ) { return true; }
    }
    else {
        if ( intYear % 4 == 0 ) { return true; }
    }
    return false;
}
