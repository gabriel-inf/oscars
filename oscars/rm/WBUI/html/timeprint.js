/*
Javascript functions for printing dates and times in various formats
Last modified: July 1, 2005
Soo-yeon Hwang (dapi@umich.edu)
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
print_current_date()
print_timezone_offset()
get_timezone_offset()
print_time_settings_example()
check_LeapYear( intYear )
*/


var monthMapping = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];


// ** print current date (format: July 1, 2005) **
function print_current_date(fr, epochSeconds)
{
    var currentDate;

    currentDate = new Date();
    if (epochSeconds) {
        currentDate.setTime(epochSeconds * 1000);
    }
    var currentMonth;

    if (!fr) { fr = document; }
    currentMonth = currentDate.getMonth();

    var monthName = monthMapping[currentMonth];

    currentMinutes = currentDate.getMinutes();
    fr.write( monthName + " " + currentDate.getDate() + ", " + currentDate.getFullYear() + " " + currentDate.getHours() + ":");

    if (currentMinutes < 10) { fr.write("0") } ;
    fr.write(currentMinutes);
}

// print local timezone offset
// Reference: "What's The Time?" at http://www.htmlgoodies.com/dateandtime/whattime.html
// Reference: http://msdn.microsoft.com/library/default.asp?url=/library/en-us/script56/html/js56jsmthgettimezone.asp
// getTimezoneOffset() number will be positive if you are behind UTC (e.g., Pacific Daylight Time), and negative if you are ahead of UTC (e.g., Japan).
function print_timezone_offset()
{
    document.write( '<option value="' + get_timezone_offset()  + '" selected>UTC ' + get_timezone_offset() + '</option>' );
}

function get_timezone_offset()
{
    var localDate = new Date();
    var Offset = -( localDate.getTimezoneOffset() / 60 );
    if ( Offset > 0 ) { Offset = "+" + Offset; }
    else { Offset = "" + Offset; }	// to string-ize

    // now start formatting the offset in the [+/-]hhmm format (ex. +0930, -0500)
    var formattedOffset_sign = Offset.substring( 0, 1 );
    var tempString = Offset.substring( 1, Offset.length );
    var formattedOffset_array = tempString.split( ".", 2 ); // split the "hour.minute" value

    if ( formattedOffset_array[1] > 0 ) {
        formattedOffset_array[1] = Number( "0." + formattedOffset_array[1] ) * 60; // change .5 to 30 minutes
    }
    else {
        formattedOffset_array[1] = 0;
    }
    formattedOffset_array[1] = "" + formattedOffset_array[1];	// to string-ize
    if ( formattedOffset_array[0].length < 2 ) {
        formattedOffset_array[0] = "0" + formattedOffset_array[0];
    }
    if ( formattedOffset_array[1].length < 2 ) {
        formattedOffset_array[1] = "0" + formattedOffset_array[1];
    }
    var formattedOffset_string = formattedOffset_sign + formattedOffset_array[0] + formattedOffset_array[1];

    return formattedOffset_string;
}

// Prints year, date, time, time zone, and duration for reservation example.
// NOTE:  For production use, start time should be in the future.
// Date calculation reference:
//     http://developer.netscape.com/viewsource/goodman_dateobject.html
function print_time_settings_example()
{
    /* var MINUTE = 60 * 1000; // in milliseconds
       var HOUR = MINUTE * 60;
       var DAY = HOUR * 24;
       var WEEK = DAY * 7;     */

    var nowDate = new Date();

    var printYear = nowDate.getFullYear();
    var printMonth = nowDate.getMonth() + 1;
    var printDate = nowDate.getDate();
    var printHour = nowDate.getHours();
    var printMinute = nowDate.getMinutes();

    document.write( '<td>' + printYear + '</td>' );
    document.write( '<td>' + printMonth + '</td>' );
    document.write( '<td>' + printDate + '</td>' );
    document.write( '<td>' + printHour + '</td>' );
    document.write( '<td>' + printMinute + '</td>' );
    document.write( '<td>UTC' + get_timezone_offset() + '</td>');
    document.write( '<td>0.05</td>' );
    document.write( '<td> </td>' );
}

// Reference: http://javascript.internet.com/forms/val-date.html
function check_date( form )
{
    currentDate = new Date();
    if ( isblank(form.start_year.value) ) {
        form.start_year.value = currentDate.getFullYear();
    }
    if ( isblank(form.start_month.value) ) {
        form.start_month.value = currentDate.getMonth() + 1;
    }
    if ( isblank(form.start_date.value) ) {
        form.start_date.value = currentDate.getDate();
    }
    if ( isblank(form.start_hour.value) ) {
        form.start_hour.value = currentDate.getHours();
    }
    if ( isblank(form.start_minute.value) ) {
        form.start_minute.value = currentDate.getMinutes();
    }
    if ( isblank(form.duration_hour.value) ) {
        form.duration_hour.value = 0.05;
    }

    if ( validate_integer(form.start_year.value) == false ) {
        alert( "The reservation start year is not a number. Please check again." );
        form.start_year.focus();
        return false;
    }
    if ( form.start_year.value.length != 4 ) {
        alert( "The reservation start year must be in four digits." );
        form.start_year.focus();
        return false;
    }

    var nowTime = currentDate.getTime();
    var boundaryTime = 60 * 1000 * 60 * 2 ; // 2 hours

    if ( validate_integer(form.start_month.value) == false ) {
        alert( "The reservation start month is not a number. Please check again." );
        form.start_month.focus();
        return false;
    }
    if ( form.start_month.value < 1 || form.start_month.value > 12 ) {
        alert( "The reservation start month is out of proper range. Please check again." );
        form.start_month.focus();
        return false;
    }
    if ( validate_integer(form.start_date.value) == false ) {
        alert( "The reservation start date is not a number. Please check again." );
        form.start_date.focus();
        return false;
    }

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
        if ( check_LeapYear(form.start_year.value) == true ) {
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

    if ( validate_integer(form.start_hour.value) == false ) {
        alert( "The reservation start hour is not a number. Please check again." );
        form.start_hour.focus();
        return false;
    }
    if ( form.start_hour.value < 0 || form.start_hour.value > 23 ) {
        alert( "The reservation start hour is out of proper range. Please check again." );
        form.start_hour.focus();
        return false;
    }
    if ( validate_numeric(form.duration_hour.value) == false ) {
        alert( "The reservation duration hour is not a number. Please check again." );
        form.duration_hour.focus();
        return false;
    }
    if (form.persistent && form.persistent.checked) {
        form.duration_hour.value = 'INF';
    }

    reserve_date = new Date(form.start_year.value, form.start_month.value - 1,
                            form.start_date.value, form.start_hour.value,
                            form.start_minute.value, 0, 0);
    form.reservation_start_time.value = reserve_date.getTime() / 1000;

    if ( (validate_integer(form.reservation_start_time.value) == false) ||
             (form.reservation_start_time.value == 0) ||
             isblank(form.reservation_start_time.value) ) {
        alert( "Problem with start time." );
        form.start_hour.focus();
        return false;
    }
    return true;
}

// check whether a year is a leap year
// Reference: http://javascript.internet.com/forms/val-date.html
function check_LeapYear( intYear )
{
    if ( intYear % 100 == 0 ) {
        if ( intYear % 400 == 0 ) { return true; }
    }
    else {
        if ( intYear % 4 == 0 ) { return true; }
    }
    return false;
}

