/*
common.js:  Common Javascript functions
Last modified: August 17, 2005
David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)
*/

/* List of functions:
submit_form(form, which)
check_form(form, which);
init_navigation_bar(user_level)
update_status_frame(status, msg)
isblank (strValue)
*/

var timer_id = null;
var starting_page = 'https://oscars.es.net/';

var login_required = {
    'user_dn': "Please enter your user name.",
    'user_password': "Please enter your password."
}

var reservation_required = {
    'src_address':  "Please enter starting host name, or its IP address, in the 'Source' field.",
    'dst_address':  "Please enter destination host name, or its IP address, in the 'Destination' field.",
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
                    
// checks validity of form settings, and uses Sarissa to post request
// and get back result
function submit_form( form, form_name, form_url )
{
    var valid = check_form( form, form_name );
    if (!valid) { return false; }

    // adapted from http://www.devx.com/DevX/Tip/17500
    var xmlhttp, send_str, resultStr;
    xmlhttp = new XMLHttpRequest();
    xmlhttp.open('POST', form_url, false);
    xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    send_str = "";
    if (form.elements) {
        var form_elements = form.elements;
        var num_elements = form.elements.length;
        for (var e=0; e < num_elements; e++) {
            if (form_elements[e].value && form_elements[e].name) {
                send_str += form_elements[e].name + '=' + form_elements[e].value + '&';
            }
        }
    } 
    // remove trailing &, and send
    xmlhttp.send(send_str.substring(0, send_str.length-1));
    get_response(xmlhttp, form_name);
    return false;
}

// updates status and main portion of page (same as above, but not with
// form submission
function new_page(form_name, url) {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.open('GET', url, false);
    var send_str = "";
    xmlhttp.send(send_str);
    get_response(xmlhttp, form_name);
    return false;
}

// gets back response from XMLHttpRequest
function get_response(xmlhttp, form_name) {
    var response_dom = xmlhttp.responseXML;
    //alert(Sarissa.serialize(response_dom));
    //alert(xmlhttp.responseText);

    // Get div element within response, if any.  If none, an error has
    // occurred.
    var returned_divs = response_dom.getElementsByTagName('div');

    // print initial navigation bar
    if ((form_name == 'login') && returned_divs.length) {
        var nav_node = document.getElementById('nav_div');
        nav_node.innerHTML = init_navigation_bar('admin');
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
        status_node.innerHTML = get_date_str() + ' | ' + status_msg;
    }

    // update main portion (only present if there was no error)
    if (returned_divs.length) {
        var main_node = document.getElementById('main_div');
        main_node.innerHTML = Sarissa.serialize(returned_divs[0]);
    }
}

// checks validity of particular form
function check_form( form, form_name )
{
    if (!form) { return true; }
    if (form_name == 'login') { return check_login( form ); }
    else if (form_name == 'creation_form') { return check_reservation( form ); }
    else if (form_name == 'profile_form') { return check_user_profile( form ); }
    return true;
}

// checks to make sure all required fields are present
function check_for_required( form, required )
{
    for (field in required) {
        if ( isblank(form[field].value) ) {
            alert( required[field] );
            form[field].focus();
            return false;
        }
    }
    return true;
}

// checks validity of login form
function check_login( form )
{
    return check_for_required( form, login_required );
}

// checks validity of create reservation form
function check_reservation( form )
{
    var valid = check_for_required( form, reservation_required );
    if (!valid) { return false; }

    // Temporary hack: (TODO:  FIX)
    if ( (form.user_dn.value == 'dtyu@bnl.gov') ||
         (form.user_dn.value == 'wenji@fnal.gov'))
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
    if (typeof(form.reservation_bandwidth.value) != 'number') {
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
        if (typeof(form.reservation_src_port.value) != 'number') {
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
        if (typeof(form.reservation_dst_port.value) != 'number' ) {
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
        if (typeof(form.reservation_dscp.value) != 'number' ) {
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

// checks validity of user profile form
function check_user_profile( form )
{
	var valid = check_for_required( form, user_profile_required );
	if (!valid) { return false; }

	if ( !(isblank(form.password_new_once.value)) ) {
		if ( form.password_new_once.value != form.password_new_twice.value ) {
			alert( "Please enter the same new password twice for verification." );
			form.password_new_once.focus();
			return false;
		}
		form.update_password.value = 1;
	}
	return true;
}

// initializes string representing navigation bar, with info page initially
// highlighted
function init_navigation_bar(user_level) {
    var navBar = '<div>' +
        '<ul id="tabnav">' +
        init_tab("info_form", "lib", "Information page", "Information");

    if (user_level.indexOf("admin") != -1) {
        navBar += init_tab("acctlist_form", "users",
                           "List user accounts", "List Accounts"); 
        navBar += init_tab("add_form", "users",
                           "Add a new user account", "Add User");
    } 
    navBar += init_tab("profile_form", "users",
                       "View and/or edit your information", "User Profile") +
              init_tab("list_form", "reservations",
                       "View/Edit selected list of reservations",
                       "View/Edit Reservations") +
              init_tab("creation_form", "reservations",
                       "Create a new reservation", "Make Reservation") +
              init_tab("logout", "lib", "Log out on click", "Log out");
    navBar += '</ul></div>';
    return navBar;
}


// Initializes string for one tab.  All the dynamic HTML generation kruft
// is concentrated here.  Clicking on tab results in call to CGI script.
function init_tab(form_name, script_type, tab_title, tab_name) {
    var tab_str = '<li id="' + form_name + '">' +
         '<a style="' + starting_page + 'test/styleSheets/layout.css"' +
	 ' title="' + tab_title + '"';
    if (form_name != 'logout') {
         tab_str += ' href="#"' +
         ' onclick="new_page(' + "'" + form_name + "', '" + 
         starting_page + 'cgi-bin/test/' + script_type + '/' + form_name +
         ".pl'" +
         ');return false;">';
    }
    else {
        tab_str += ' href="' + starting_page +
         'cgi-bin/test/' + script_type + '/' + form_name + '.pl">';
    }
    tab_str += tab_name + '</a></li>';
    return tab_str;
}

function setup_flash(errorFlag) {
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


// From Javascript book, p. 264

function isblank(s) {
    for (var i = 0; i < s.length; i++) {
        var c = s.charAt(i);
        if ((c != ' ') && (c != '\n') && (c != '')) return false;
    }
    return true;
}
