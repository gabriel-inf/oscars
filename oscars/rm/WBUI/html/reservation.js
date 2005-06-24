/*
Javascript functions for reservation CGI interface
Last modified: June 23, 2005
Soo-yeon Hwang (dapi@umich.edu)
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
check_form( form )
*/

function check_form( form )
{
    /*
    if ( form.user_dn.value == "" ) {
        alert( "Please enter your distinguished name." );
        form.user_dn.focus();
        return false;
    }
    */

    if ( isblank(form.src_address.value) )
    {
        alert( "Please enter starting host name, or its IP address ." );
        form.src_address.focus();
        return false;
    }
    if ( isblank(form.dst_address.value) )
    {
        alert( "Please enter destination host name, or its IP address." );
        form.dst_address.focus();
        return false;
    }
    if ( isblank(form.reservation_bandwidth.value) )
    {
        alert( "Please enter the amount of bandwidth that you want to reserve." );
        form.reservation_bandwidth.focus();
        return false;
    }

    if ( isblank(form.reservation_description.value) )
    {
        alert( "Please describe the purpose of this reservation request." );
        form.reservation_description.focus();
        return false;
    }

    // finished checking empty fields
    // now starting to check whether some fields have proper values
    // for each field, in the order of: check whether its value is a valid number/integer, check whether it's in a proper range, and then check other conditions 
    if ( form.src_address.value == form.dst_address.value ) {
        alert( "Please provide different IP adresses for origin and destination." );
        form.src_address.focus();
        return false;
    }
    if ( validate_numeric(form.reservation_bandwidth.value) == false ) {
        alert( "The bandwidth is not a number. Please check again." );
        form.reservation_bandwidth.focus();
        return false;
    }
    if ( form.reservation_bandwidth.value <= 0 ) {
        alert( "The amount of bandwidth should be greater than 0. Please check again." );
        form.reservation_bandwidth.focus();
        return false;
    }
    // at this point success only depends on a correct date
    return check_date(form);
}
