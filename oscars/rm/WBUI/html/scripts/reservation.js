/*
Javascript functions for reservation CGI interface
Last modified: June 29, 2005
Soo-yeon Hwang (dapi@umich.edu)
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
check_form( form )
*/

function check_form( form, user_dn )
{
    // Temporary hack: (TODO:  FIX)
    if ( (user_dn == 'dtyu@bnl.gov') || (user_dn == 'wenji@fnal.gov'))
    {
        if (form.lsp_from.value && (form.lsp_from.value != 'chi-sl-sdn1'))
        {
             alert( "Only 'chi-sl-sdn1', or a blank value, is permissible in the 'Ingress loopback' field." );
             form.lsp_from.focus();
             return false;
        }
        if (form.lsp_to.value && (form.lsp_to.value != 'chi-sl-sdn1'))
        {
             alert( "Only 'chi-sl-sdn1', or a blank value, is permissible in the 'Egress loopback' field." );
             form.lsp_to.focus();
             return false;
        }
    }
    // Check for empty required fields, and whether non-blank fields have
    // proper values for each field, in the order of: check whether its value
    // is a valid number/integer, check whether it's in a proper range, and
    // then check other conditions 
    if ( isblank(form.src_address.value) )
    {
        alert( "Please enter starting host name, or its IP address, in the 'Source' field." );
        form.src_address.focus();
        return false;
    }
    if ( isblank(form.dst_address.value) )
    {
        alert( "Please enter destination host name, or its IP address, in the 'Destination' field." );
        form.dst_address.focus();
        return false;
    }
    if ( isblank(form.reservation_bandwidth.value) ) {
        alert( "Please enter the amount of bandwidth that you want to reserve in the 'Bandwidth' field." );
        form.reservation_bandwidth.focus();
        return false;
    }
    else if ( validate_numeric(form.reservation_bandwidth.value) == false ) {
        alert( "The bandwidth must be a positive integer." );
        form.reservation_bandwidth.focus();
        return false;
    }
    else if ( (form.reservation_bandwidth.value < 1 ) ||
             (form.reservation_bandwidth.value > 10000)) {
        alert( "The amount of bandwidth must be in the range 1-10000 Mbps." );
        form.reservation_bandwidth.focus();
        return false;
    }

    if ( isblank(form.reservation_description.value) ) {
        alert( "Please describe the purpose of this reservation request." );
        form.reservation_description.focus();
        return false;
    }

    if ( form.src_address.value == form.dst_address.value ) {
        alert( "Please provide different IP addresses for the source and destination." );
        form.src_address.focus();
        return false;
    }
    // TODO:  needs more work
    var sections = form.src_address.value.split('/');
    if ((sections.length > 1) && (sections[1] < 24)) {
        alert( "Only CIDR blocks >= 24 (class C) are accepted." );
        form.src_address.focus();
        return false;
    }
    var sections = form.dst_address.value.split('/');
    if ((sections.length > 1) && (sections[1] < 24)) {
        alert( "Only CIDR blocks >= 24 (class C) are accepted." );
        form.dst_address.focus();
        return false;
    }

    // check non-required fields if a value has been entered
    if ( !isblank(form.reservation_src_port.value) ) {
        if ( validate_numeric(form.reservation_src_port.value) == false ) {
            alert( "The source port must be a positive integer." );
            form.reservation_src_port.focus();
            return false;
        }
        else if ( (form.reservation_src_port.value < 1024) ||
                (form.reservation_src_port.value > 65535) ) {
            alert( "The source port, if given, must be in the range 1024-65535." );
            form.reservation_src_port.focus();
            return false;
        }
    }
    if ( !isblank(form.reservation_dst_port.value) ) {
        if ( validate_numeric(form.reservation_dst_port.value) == false ) {
            alert( "The destination port must be a positive integer." );
            form.reservation_dst_port.focus();
            return false;
        }
        else if ( (form.reservation_dst_port.value < 1024) ||
                (form.reservation_dst_port.value > 65535) ) {
            alert( "The destination port, if given, must be in the range 1024-65535." );
            form.reservation_dst_port.focus();
            return false;
        }
    }
    if ( !isblank(form.reservation_dscp.value) ) {
        if ( validate_numeric(form.reservation_dscp.value) == false ) {
            alert( "The DSCP must be a positive integer." );
            form.reservation_dscp.focus();
            return false;
        }
        else if ( (form.reservation_dscp.value < 0) ||
                (form.reservation_dscp.value > 63) ) {
            alert( "The DSCP, if given, must be in the range 0-63." );
            form.reservation_dscp.focus();
            return false;
        }
    }
    // at this point success only depends on a correct date
    return check_date(form);
}
