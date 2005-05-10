/*
Javascript functions for reservation CGI interface
Last modified: April 09, 2005
Soo-yeon Hwang (dapi@umich.edu)
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
print_timezone_offset()
get_timezone_offset()
print_start_datetime_example()
check_form( form )
check_LeapYear( intYear )
validate_integer( strValue )
validate_numeric( strValue )
*/

// print local timezone offset
// Reference: "What's The Time?" at http://www.htmlgoodies.com/dateandtime/whattime.html
// Reference: http://msdn.microsoft.com/library/default.asp?url=/library/en-us/script56/html/js56jsmthgettimezone.asp
// getTimezoneOffset() number will be positive if you are behind UTC (e.g., Pacific Daylight Time), and negative if you are ahead of UTC (e.g., Japan).
function print_timezone_offset()
{
//	document.write( '<input type="input" name="local_offset" value="' + get_timezone_offset() + '" size="3" style="text-align: left">' );

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

	if ( formattedOffset_array[1] > 0 )
	{
		formattedOffset_array[1] = Number( "0." + formattedOffset_array[1] ) * 60; // change .5 to 30 minutes
	}
	else
	{
		formattedOffset_array[1] = 0;
	}

	formattedOffset_array[1] = "" + formattedOffset_array[1];	// to string-ize

	if ( formattedOffset_array[0].length < 2 ) { formattedOffset_array[0] = "0" + formattedOffset_array[0]; }
	if ( formattedOffset_array[1].length < 2 ) { formattedOffset_array[1] = "0" + formattedOffset_array[1]; }

	var formattedOffset_string = formattedOffset_sign + formattedOffset_array[0] + formattedOffset_array[1];

	return formattedOffset_string;
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

	if ( formattedOffset_array[1] > 0 )
	{
		formattedOffset_array[1] = Number( "0." + formattedOffset_array[1] ) * 60; // change .5 to 30 minutes
	}
	else
	{
		formattedOffset_array[1] = 0;
	}

	formattedOffset_array[1] = "" + formattedOffset_array[1];	// to string-ize

	if ( formattedOffset_array[0].length < 2 ) { formattedOffset_array[0] = "0" + formattedOffset_array[0]; }
	if ( formattedOffset_array[1].length < 2 ) { formattedOffset_array[1] = "0" + formattedOffset_array[1]; }

	var formattedOffset_string = formattedOffset_sign + formattedOffset_array[0] + formattedOffset_array[1];

	return formattedOffset_string;
}

// print year, date, and time for reservation start time ui example
// date calculation reference: http://developer.netscape.com/viewsource/goodman_dateobject.html
function print_start_datetime_example()
{
	/*
	var MINUTE = 60 * 1000; // in milliseconds
	var HOUR = MINUTE * 60;
	var DAY = HOUR * 24;
	var WEEK = DAY * 7;
	*/

	var nowTime = ( new Date() ).getTime();
	var boundaryDate = new Date( nowTime + ( 60 * 1000 * 60 * 2 ) ); // 2 hours from now

	var printYear = boundaryDate.getUTCFullYear();
	var printMonth = boundaryDate.getUTCMonth() + 1;
	var printDate = boundaryDate.getUTCDate();
	var printHour = boundaryDate.getUTCHours();
	var printMinute = boundaryDate.getUTCMinutes();


	document.write( '<td>' + printYear + '</td>' + '<td>' + printMonth + '</td>' + '<td>' + printDate + '</td>' );

	document.write( '<td>' + printHour + '</td>' + '<td>' + printMinute + '</td>' + '<td>UTC</td>' );
}

// Reference: http://javascript.internet.com/forms/val-date.html
function check_form( form )
{
	/*
	if ( form.username.value == "" )
	{
		alert( "Please enter your distinguished name." );
		form.username.focus();
		return false;
	}
	*/

	if ( form.origin.value == "" )
	{
		alert( "Please enter starting host name, or its IP address ." );
		form.origin.focus();
		return false;
	}

	if ( form.destination.value == "" )
	{
		alert( "Please enter destination host name, or its IP address." );
		form.destination.focus();
		return false;
	}

	if ( form.bandwidth.value == "" )
	{
		alert( "Please enter the amount of bandwidth that you want to reserve." );
		form.bandwidth.focus();
		return false;
	}

        currentDate = new Date();
	if ( form.start_year.value == "" )
	{
                form.start_year.value = currentDate.getUTCFullYear();
	}

	if ( form.start_month.value == "" )
	{
                form.start_month.value = currentDate.getUTCMonth();
	}

	if ( form.start_date.value == "" )
	{
                form.start_date.value = currentDate.getUTCDate();
	}

	if ( form.start_hour.value == "" )
	{
                form.start_hour.value = currentDate.getUTCHour();
	}

	if ( form.start_minute.value == "" )
	{
                form.start_minute.value = currentDate.getUTCMinute();
	}

	if ( form.duration_hour.value == "" )
	{
		alert( "Please enter the reservation duration hour." );
		form.duration_hour.focus();
		return false;
	}

	if ( form.description.value == "" )
	{
		alert( "Please describe the purpose of this reservation request." );
		form.description.focus();
		return false;
	}

	// finished checking empty fields
	// now starting to check whether some fields have proper values
	// for each field, in the order of: check whether its value is a valid number/integer, check whether it's in a proper range, and then check other conditions 

	if ( form.origin.value == form.destination.value )
	{
		alert( "Please provide different IP adresses for origin and destination." );
		form.origin.focus();
		return false;
	}

	if ( validate_numeric(form.bandwidth.value) == false )
	{
		alert( "The bandwidth is not a number. Please check again." );
		form.bandwidth.focus();
		return false;
	}

	if ( form.bandwidth.value <= 0 )
	{
		alert( "The amount of bandwidth should be greater than 0. Please check again." );
		form.bandwidth.focus();
		return false;
	}

	if ( validate_integer(form.start_year.value) == false )
	{
		alert( "The reservation start year is not a number. Please check again." );
		form.start_year.focus();
		return false;
	}

	if ( form.start_year.value.length != 4 )
	{
		alert( "The reservation start year must be in four digits." );
		form.start_year.focus();
		return false;
	}

	var nowTime = currentDate.getTime();
	var boundaryTime = 60 * 1000 * 60 * 2 ; // 2 hours

	if ( validate_integer(form.start_month.value) == false )
	{
		alert( "The reservation start month is not a number. Please check again." );
		form.start_month.focus();
		return false;
	}

	if ( form.start_month.value < 1 || form.start_month.value > 12 )
	{
		alert( "The reservation start month is out of proper range. Please check again." );
		form.start_month.focus();
		return false;
	}

	if ( validate_integer(form.start_date.value) == false )
	{
		alert( "The reservation start date is not a number. Please check again." );
		form.start_date.focus();
		return false;
	}

	if ( ( form.start_month.value == 1 || form.start_month.value == 3 || form.start_month.value == 5 || form.start_month.value == 7 || form.start_month.value == 8 || form.start_month.value == 10 || form.start_month.value == 12 ) && ( form.start_date.value > 31 || form.start_date.value < 1 ) )
	{
		alert( "The reservation start date is out of proper range. Please check again." );
		form.start_date.focus();
		return false;
	}

	if ( ( form.start_month.value == 4 || form.start_month.value == 6 || form.start_month.value == 9 || form.start_month.value == 11 ) && ( form.start_date.value > 30 || form.start_date.value < 1 ) )
	{
		alert( "The reservation start date is out of proper range. Please check again." );
		form.start_date.focus();
		return false;
	}

	if ( form.start_month.value == 2 )
	{
		if ( form.start_date.value < 1 )
		{
			alert( "The reservation start date is out of proper range. Please check again." );
			form.start_date.focus();
			return false;
		}

		if ( check_LeapYear(form.start_year.value) == true )
		{
			if ( form.start_date.value > 29 )
			{
				alert( "The reservation start date is out of proper range. Please check again." );
				form.start_date.focus();
				return false;
			}
		}
		else
		{
			if ( form.start_date.value > 28 )
			{
				alert( "The reservation start date is out of proper range. Please check again." );
				form.start_date.focus();
				return false;
			}
		}
	}


	if ( validate_integer(form.start_hour.value) == false )
	{
		alert( "The reservation start hour is not a number. Please check again." );
		form.start_hour.focus();
		return false;
	}


	if ( form.start_hour.value < 0 || form.start_hour.value > 23 )
	{
		alert( "The reservation start hour is out of proper range. Please check again." );
		form.start_hour.focus();
		return false;
	}

	if ( validate_integer(form.duration_hour.value) == false )
	{
		alert( "The reservation duration hour is not a number. Please check again." );
		form.duration_hour.focus();
		return false;
	}

	if ( form.duration_hour.value < 2 )
	{
		alert( "The duration of reservation should be equal to or greater than two hours. Please check again." );
		form.duration_hour.focus();
		return false;
	}
        reserve_date = new Date(form.start_year.value, form.start_month.value, form.start_date.value, form.start_hour.value, form.start_minute.value, 0, 0);
        form.start_time.value = reserve_date.getTime() / 1000;
	if ( form.start_time.value == 0 )
	{
		alert( "Problem with start time." );
		return false;
	}

	return true;
}

// check whether a year is a leap year
// Reference: http://javascript.internet.com/forms/val-date.html
function check_LeapYear( intYear )
{
	if ( intYear % 100 == 0 )
	{
		if ( intYear % 400 == 0 ) { return true; }
	}
	else
	{
		if ( intYear % 4 == 0 ) { return true; }
	}

	return false;
}

// validates that a string contains only valid integer number
// returns true if valid, otherwise false
// Reference: http://www.rgagnon.com/jsdetails/js-0063.html
function validate_integer( strValue )
{
	var objRegExp = /(^-?\d\d*$)/;

	//check for integer characters
	return objRegExp.test(strValue);
}

// validates that a string contains only valid numbers
// returns true if valid, otherwise false
// Reference: http://www.rgagnon.com/jsdetails/js-0063.html
function validate_numeric( strValue )
{
  var objRegExp = /(^-?\d\d*\.\d*$)|(^-?\d\d*$)|(^-?\.\d\d*$)/;

  //check for numeric characters
  return objRegExp.test(strValue);
}
