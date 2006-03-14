/*
common.js:      Javascript functions for form submission
Last modified:  February 13, 2005
David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)
*/

/* List of functions:
submit_form(form, params)
new_section(params)
get_response(xmlhttp)
check_for_required(form, required)
check_login(form)
check_reservation(form)
check_profile_modification(form)
check_add_user(form)
is_numeric(value)
is_blank(str)
*/

// TODO:  objects
var login_required = {
    'user_login': "Please enter your user name.",
    'user_password': "Please enter your password."
}

var reservation_required = {
    'source_host': "Please enter starting host name, or its IP address, in the 'Source' field.",
    'destination_host':  "Please enter destination host name, or its IP address, in the 'Destination' field.",
    'reservation_bandwidth': "Please enter the amount of bandwidth you require in the 'Bandwidth' field.",
    'reservation_description': "Please describe the purpose of this reservation request."
}

var profile_modification_required = {
    'user_last_name': "Please enter the user's last name.",
    'user_first_name': "Please enter the user's first name.",
    'institution':  "Please enter the user's organization.",
    'user_email_primary': "Please enter the user's primary email address.",
    'user_phone_primary': "Please enter the user's primary phone number."
}

var add_user_required = {
    'selected_user': "Please enter the new user's distinguished name.",
    'password_new_once': "Please enter the new user's password.",
    'user_last_name': "Please enter the new user's last name.",
    'user_first_name': "Please enter the new user's first name.",
    'institution':  "Please enter the new user's organization.",
    'user_email_primary': "Please enter the new user's primary email address.",
    'user_phone_primary': "Please enter the new user's primary phone number."
}

// Checks validity of form settings, and uses Sarissa to post request
// and get back result.
function submit_form( form, params, check_function ) {
    if ( check_function ) {
        var valid = check_function( form );
        if (!valid) { return false; }
    }

    // adapted from http://www.devx.com/DevX/Tip/17500
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.open('POST', '/perl/adapt.pl', false);
    xmlhttp.setRequestHeader('Content-Type',
                             'application/x-www-form-urlencoded');
    if (form.elements) {
        var form_elements = form.elements;
        var num_elements = form.elements.length;
        for (var e=0; e < num_elements; e++) {
            if (form_elements[e].value && form_elements[e].name) {
                params +=
                   form_elements[e].name + '=' + form_elements[e].value + ';';
            }
        }
    }
    xmlhttp.send(params.substring(0, params.length-1));
    get_response(xmlhttp);
    return false;
}

// Updates status and main portion of page (same as above, but without
// form submission).
function new_section( params ) {
    var empty_str = "";
    var xmlhttp = new XMLHttpRequest();
    var url = '/perl/adapt.pl?' + params;
    xmlhttp.open('GET', url, false);
    xmlhttp.send(empty_str);
    get_response(xmlhttp);
    return false;
}

// Gets back response from XMLHttpRequest.
function get_response(xmlhttp) {
    var response_dom = xmlhttp.responseXML;
    if (!response_dom) {
        var status_node = document.getElementById('status-div');
        status_node.innerHTML = date_str() + ' Please contact dwrobertson@lbl.gov to make sure that the OSCARS server is running.';
        return;
    }

    //alert(Sarissa.serialize(response_dom));
    //alert(xmlhttp.responseText);

    // Get div element within response, if any.  If none, an error has
    // occurred.
    var returned_divs = response_dom.getElementsByTagName('div');

    // Rewrite navigation bar
    var returned_nav_nodes =
                           response_dom.getElementsByTagName('navigation_bar');
    var nav_bar_str = '';
    var nav_node = document.getElementById('nav-div');
    if (returned_nav_nodes.length) {
        nav_bar_str = Sarissa.serialize(returned_nav_nodes[0]);
        nav_node.innerHTML = nav_bar_str;
    }

    // get text of status message, if any
    var returned_status_nodes = response_dom.getElementsByTagName('msg');
    var status_msg = '';
    if (returned_status_nodes.length) {
        status_msg = returned_status_nodes[0].firstChild.data;
    }

    // update status bar
    var status_node = document.getElementById('status-div');
    if (status_msg) {
        status_node.innerHTML = date_str() + ' | ' + status_msg;
    }

    // update main portion (only present if there was no error)
    if (!returned_divs.length) {
        return;
    }
    var main_node = document.getElementById('main-div');
    main_node.innerHTML = Sarissa.serialize(returned_divs[0]);

    // only used with time zones in CreateReservationForm:  TODO:  FIX
    var time_node = document.getElementById('time-zone-options');
    if (time_node) {
        time_node.innerHTML = time_zone_options();
    }
    time_node = document.getElementById('local-time-zone');
    if (time_node) {
        time_node.innerHTML = local_time_zone();
    }

    sortables_init();
}

// Checks to make sure all required fields are present.
function check_for_required( form, required )
{
    for (field in required) {
        if ( form[field] && is_blank(form[field].value) ) {
            alert( required[field] );
            form[field].focus();
            return false;
        }
    }
    return true;
}

// Checks validity of login form.
function check_login( form )
{
    return check_for_required( form, login_required );
}

// Checks validity of create reservation form.
function check_reservation( form )
{
    var valid = check_for_required( form, reservation_required );
    if (!valid) { return false; }

    // Temporary hack: (TODO:  FIX)
    if ( (form.user_login.value == 'dtyu@bnl.gov') ||
         (form.user_login.value == 'wenji@fnal.gov'))
    {
        if (form.ingress_router.value && (form.ingress_router.value != 'chi-sl-sdn1'))
        {
             alert( "Only 'chi-sl-sdn1', or a blank value, is permissible in the 'Ingress loopback' field." );
             form.ingress_router.focus();
             return false;
        }
        if (form.egress_router.value && (form.egress_router.value != 'chi-sl-sdn1'))
        {
             alert( "Only 'chi-sl-sdn1', or a blank value, is permissible in the 'Egress loopback' field." );
             form.egress_router.focus();
             return false;
        }
    }
    if (!(is_numeric(form.reservation_bandwidth.value))) {
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

    if ( form.source_host.value == form.destination_host.value ) {
        alert( "Please provide different host names or IP addresses for the source and destination." );
        form.source_host.focus();
        return false;
    }
    // TODO:  needs more work
    var sections = form.source_host.value.split('/');
    if ((sections.length > 1) && (sections[1] < 24)) {
        alert( "Only CIDR blocks >= 24 (class C) are accepted." );
        form.source_host.focus();
        return false;
    }
    var sections = form.destination_host.value.split('/');
    if ((sections.length > 1) && (sections[1] < 24)) {
        alert( "Only CIDR blocks >= 24 (class C) are accepted." );
        form.destination_host.focus();
        return false;
    }

    // check non-required fields if a value has been entered
    if ( !is_blank(form.reservation_src_port.value) ) {
        if (!(is_numeric(form.reservation_src_port.value))) {
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
    if ( !is_blank(form.reservation_dst_port.value) ) {
        if (!(is_numeric(form.reservation_dst_port.value))) {
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
    if ( !is_blank(form.reservation_dscp.value) ) {
        if (!(is_numeric(form.reservation_dscp.value))) {
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
    return check_date_fields(form);
}

// Checks validity of user profile form.
function check_profile_modification( form )
{
    var valid = check_for_required( form, profile_modification_required );
    if (!valid) { return false; }
    return true;
}

// Checks validity of add user form.
function check_add_user( form )
{
    var valid = check_for_required( form, add_user_required );
    if (!valid) { return false; }

    if ( !(is_blank(form.password_new_once.value)) ) {
        if ( form.password_new_once.value != form.password_new_twice.value ) {
            alert( "Please enter the same new password twice for verification." );
            form.password_new_once.focus();
            return false;
        }
    }
    return true;
}

function is_numeric(s) {
   return( s.match(/(\d)+/) );
}

// From Javascript book, p. 264

function is_blank(s) {
    for (var i = 0; i < s.length; i++) {
        var c = s.charAt(i);
        if ((c != ' ') && (c != '\n') && (c != '')) return false;
    }
    return true;
}
