/*
common.js:      Javascript functions for form submission
Last modified:  November 23, 2005
David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)
*/

/* List of functions:
submit_form(form, method_name, params_str)
new_section(method_name, params)
get_response(xmlhttp, method_name)
check_form(form, method_name);
check_for_required(form, required)
check_login(form)
check_reservation(form)
check_user_profile(form)
flash_status_bar(timer_id)
is_numeric(value)
is_blank(str)
has_class(obj)
stripe(id)
*/

var timer_id = null;

var login_required = {
    'user_dn': "Please enter your user name.",
    'user_password': "Please enter your password."
}

var reservation_required = {
    'source_host':  "Please enter starting host name, or its IP address, in the 'Source' field.",
    'destination_host':  "Please enter destination host name, or its IP address, in the 'Destination' field.",
    'reservation_bandwidth': "Please enter the amount of bandwidth you require in the 'Bandwidth' field.",
    'reservation_description': "Please describe the purpose of this reservation request."
}

var user_profile_required = {
    'user_password': "Please enter the current password.",
    'user_last_name': "Please enter the user's last name.",
    'user_first_name': "Please enter the user's first name.",
    'institution':  "Please enter the user's organization.",
    'user_email_primary': "Please enter the user's primary email address.",
    'user_phone_primary': "Please enter the user's primary phone number."
}
                    
// Checks validity of form settings, and uses Sarissa to post request
// and get back result.
function submit_form( form, method_name, params_str )
{
    var valid = check_form( form, method_name );
    if (!valid) { return false; }

    // adapted from http://www.devx.com/DevX/Tip/17500
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.open('POST', '/perl/adapt.pl', false);
    xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    if (!params_str) {
        params_str = 'method=' + method_name + ';';
        if (form.elements) {
            var form_elements = form.elements;
            var num_elements = form.elements.length;
            for (var e=0; e < num_elements; e++) {
                if (form_elements[e].value && form_elements[e].name) {
                    params_str += form_elements[e].name + '=' + form_elements[e].value + ';';
                }
            }
        } 
    }
    xmlhttp.send(params_str);
    get_response(xmlhttp, method_name);
    return false;
}

// Updates status and main portion of page (same as above, but without
// form submission).
function new_section( method_name, params ) {
    var xmlhttp = new XMLHttpRequest();
    var url = '/perl/adapt.pl?method=' + method_name;
    if (params) {
        url += ';' + params;
    }
    xmlhttp.open('GET', url, false);
    var empty_str = "";
    xmlhttp.send(empty_str);
    get_response(xmlhttp, method_name);
    return false;
}

// Gets back response from XMLHttpRequest.
function get_response(xmlhttp, method_name) {
    var response_dom = xmlhttp.responseXML;
    //alert(Sarissa.serialize(response_dom));
    //alert(xmlhttp.responseText);

    // Get div element within response, if any.  If none, an error has
    // occurred.
    var returned_divs = response_dom.getElementsByTagName('div');

    // Initialize navigation bar on login
    if ((method_name == 'login') && returned_divs.length) {
        var nav_node = document.getElementById('nav_div');
        if (nav_node) {
            var returned_nav_nodes =
                           response_dom.getElementsByTagName('navigation_bar');
            var nav_bar_str = '';
            if (returned_nav_nodes.length) {
                nav_bar_str = Sarissa.serialize(returned_nav_nodes[0]);
                nav_node.innerHTML = nav_bar_str;
            }
        }
    }

    // Update navigation bar
    var tab_node = document.getElementById(method_name);
    if (tab_node) {
        tab_node.className = 'active';
    }

    // get text of status message, if any
    var returned_status_nodes = response_dom.getElementsByTagName('msg');
    var status_msg = '';
    if (returned_status_nodes.length) {
        status_msg = returned_status_nodes[0].firstChild.data;
    }

    // update status bar
    var status_node = document.getElementById('status_div');
    if (status_msg) {
        status_node.innerHTML = date_str() + ' | ' + status_msg;
    }

    // update main portion (only present if there was no error)
    if (!returned_divs.length) {
        return;
    }
    var main_node = document.getElementById('main_div');
    main_node.innerHTML = Sarissa.serialize(returned_divs[0]);
    stripe('zebra');

    if (method_name == 'create_reservation_form') {
        var time_node = document.getElementById('tz_option_list');
        if (time_node) {
            time_node.innerHTML = tz_option_list();
        }
        time_node = document.getElementById('time_settings_example');
        if (time_node) {
            time_node.innerHTML = time_settings_example();
        }
    }
}

// Checks validity of particular form.
function check_form( form, method_name )
{
    if (!form) { return true; }
    if (method_name == 'login') { return check_login( form ); }
    else if (method_name == 'create_reservation') { 
        return check_reservation( form );
    }
    else if (method_name == 'set_profile') { return check_user_profile( form ); }
    return true;
}

// Checks to make sure all required fields are present.
function check_for_required( form, required )
{
    for (field in required) {
        if ( is_blank(form[field].value) ) {
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
    if ( (form.user_dn.value == 'dtyu@bnl.gov') ||
         (form.user_dn.value == 'wenji@fnal.gov'))
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
function check_user_profile( form )
{
    var valid = check_for_required( form, user_profile_required );
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


function flash_status_bar(timer_id) {
    var style_sheet = document.style_sheet[0];
    if (!timer_id) {
        timer_id = self.setTimeout("setup_flash", 1000);
        //style_sheet.p.topmessage.style.color = "#bb0000";
    }
    else {
        clearTimeout(timer_id);
        timer_id = null;
	//style_sheet.p.topmessage.style.color = "#0000bb";
    }
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

// ** apply zebra stripe to a table **
// Reference: http://www.alistapart.com/articles/zebratables/

// This function is needed to work around a bug in IE related to element 
// attributes.
function has_class(obj) {
    var result = false;
    if ( obj.getAttributeNode("class") != null ) {
        result = obj.getAttributeNode("class").value;
    }
    return result;
}

function stripe(id) {
    // Flag that keeps track of whether the current row is odd or even.
    var even = false

    // If arguments are provided to specify the colours of the even & odd rows,
    // then use the them.  Otherwise use the following defaults:
    var evenColor = arguments[1] ? arguments[1] : "#fff";
    var oddColor = arguments[2] ? arguments[2] : "#eee";

    // Obtain reference to the desired table.  If no such table exists, abort.
    var table = document.getElementById(id);
    if (! table) { return; }

    // By definition, tables can have more than one tbody element, so we'll 
    // have to get the list of children
    var tbodies = table.getElementsByTagName("tbody");

    // and iterate through them...
    for (var h = 0; h < tbodies.length; h++) {
        // Find all the <tr> elements... 
        var trs = tbodies[h].getElementsByTagName("tr");
        for (var i = 0; i < trs.length; i++) {
            // avoid rows that have a class attribute or backgroundColor style
            if (!has_class(trs[i]) && ! trs[i].style.backgroundColor) {
               // get all the cells in this row...
               var tds = trs[i].getElementsByTagName("td");

               // and iterate through them...
               for (var j = 0; j < tds.length; j++) {
                   var mytd = tds[j];
                   // avoid cells that have a class attribute or 
                   // backgroundColor style
                   if ( !(has_class(mytd)) && !(mytd.style.backgroundColor)) {
                       mytd.style.backgroundColor = even ? evenColor : oddColor;
                   }
               }
            }
            // flip from odd to even, or vice-versa
            even =  ! even;
        }
    }
}
