/*
Javascript functions for main pages
Last modified: June 13, 2005
Soo-yeon Hwang (dapi@umich.edu)
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
print_navigation_bar(user_level, activePage)
hasClass(obj)
stripe(id)
validate_integer( strValue )
validate_numeric( strValue )
*/


// ** prints HTML header
function print_html_header()
{
    document.write('<title>On-demand Secure Circuits and Advance Reservation System</title>');
    document.write('	<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"a');
    document.write('	<meta name="Author" content="Soo-yeon Hwang">');
    document.write('    <meta name="Keywords" content="">');
    document.write('	<meta name="Description" content="">');
//    document.write('	<meta http-equiv="Refresh" content="100;URL=./">');
}


// ** displays new page in given frame
function update_frame(target, uri)
{
    parent.frames[target].location = uri;
}


// ** prints navigation bar for appropriate form type, with active page
// ** highlighted

function print_navigation_bar(userLevel, activePage)
{
    document.write('<div>');
    document.write('<ul id="tabnav">');

    if (userLevel.indexOf("admin") != -1) {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/users/acctlist_form.pl" ');
        if (activePage == 'userlist') {
            document.write('class="active" ');
        }
        document.write('title="View list of all accounts">List All Accounts</a></li>');

        document.write('<li><a href="https://oscars.es.net/cgi-bin/users/add_form.pl" ');
        if (activePage == 'adduser') {
            document.write('class="active" ');
        }
        document.write('title="Add a new user account">Add a New User</a></li>');
    } 

    document.write('<li><a href="https://oscars.es.net/cgi-bin/users/profile_form.pl" ');
    if (activePage == 'profile') {
        document.write('class="active" ');
    }
    document.write('title="View and/or edit your information">User Profile</a></li>');

    document.write('<li><a href="https://oscars.es.net/cgi-bin/reservations/list_form.pl" ');
    if (activePage == 'reservationlist') {
        document.write('class="active" ');
    }
    document.write('title="View/Edit selected list of reservations">View/Edit Reservations</a></li>');

    document.write('<li><a href="https://oscars.es.net/cgi-bin/reservations/creation_form.pl" ');
    if (activePage == 'reservation') {
        document.write(' class="active" ');
    }
    document.write('title="Create a new reservation">Make a Reservation</a></li>');

    document.write('<li><a href="https://oscars.es.net/cgi-bin/lib/logout.pl" class="logout" title="Log out on click.">Log Out</a></li>');
    document.write("</ul>");
    document.write("</div>");
}


// ** prints new message in status frame
function update_status_message(target, msg)
{
    if (target != 'status_frame') {
        f = parent.frames['status_frame'].document;
        f.open();
        f.write('<html>');
        f.write('<head>');
        f.write('<link rel="stylesheet" type="text/css" href="https://oscars.es.net/styleSheets/layout.css">');
        f.write('<script language="javascript" type="text/javascript" src="https://oscars.es.net/main_common.js"></script>');
        f.write('</head>');

        f.write('<body>');
        f.write('<div>');
        f.write('<p class="topmessage">');
        print_current_date(f, '', 'local');
        f.write('</script> | ', msg, '</p>');
        f.write('</div>');
        f.write('</body>');
        f.write('</html>');
        f.close();
    }
    else {
        document.write('<div>');
        document.write('<p class="topmessage">');
        print_current_date('', '', 'local');
        document.write('</script> | ', msg, '</p>');
        document.write('</div>');
    }
}


// ** prints page footer
function print_footer()
{
    document.write('<br/>');
    document.write('<hr WIDTH = 80 % NOSHADE >');
    document.write('<br/>');
    document.write('<center><font face=arial,helvetica size=-1>');
    document.write('<a href="http://www.es.net/">ESnet</a> | ');
    document.write('<a href="http://www.lbl.gov/">Berkeley Lab</a> |');
    document.write('<a href="http://www.lbl.gov/Disclaimers.html">Notice to Users</a> |');
    document.write('<a href="http://www.es.net/OSCARS/">OSCARS</a>');
    document.write('</font></center>');

    document.write('<p>');
    document.write('<center><table Width="85%">');
    document.write('<tr><td>');
    document.write('<font face=arial,helvetica size=2 COLOR=GRAY>');
    document.write('<b>Contacts:</b> <a href="mailto:chin@es.net">Chin Guok</a>, ');
    document.write('<a href="mailto:DWRobertson@lbl.gov">David Robertson</a><br/>');

    document.write('</font>');
    document.write('</table>');
    document.write('</center>');
}


// ** apply zebra stripe to a table **
// Reference: http://www.alistapart.com/articles/zebratables/

// this function is need to work around
// a bug in IE related to element attributes
function hasClass(obj)
{
    var result = false;
    if ( obj.getAttributeNode("class") != null ) {
        result = obj.getAttributeNode("class").value;
    }
    return result;
}

function stripe(id)
{
    // the flag we'll use to keep track of 
    // whether the current row is odd or even
    var even = false;

    // if arguments are provided to specify the colours
    // of the even & odd rows, then use the them;
    // otherwise use the following defaults:
    var evenColor = arguments[1] ? arguments[1] : "#fff";
    var oddColor = arguments[2] ? arguments[2] : "#eee";

    // obtain a reference to the desired table
    // if no such table exists, abort
    var table = document.getElementById(id);
    if (! table) { return; }

    // by definition, tables can have more than one tbody
    // element, so we'll have to get the list of child
    // <tbody>s
    var tbodies = table.getElementsByTagName("tbody");

    // and iterate through them...
    for (var h = 0; h < tbodies.length; h++) {
        // find all the <tr> elements... 
        var trs = tbodies[h].getElementsByTagName("tr");
  
        // ... and iterate through them
        for (var i = 0; i < trs.length; i++) {
            // avoid rows that have a class attribute
            // or backgroundColor style
            if (!hasClass(trs[i]) && ! trs[i].style.backgroundColor) {
                // get all the cells in this row...
                var tds = trs[i].getElementsByTagName("td");

                // and iterate through them...
                for (var j = 0; j < tds.length; j++) {
                    var mytd = tds[j];
                    // avoid cells that have a class attribute
                    // or backgroundColor style
                    if (! hasClass(mytd) && ! mytd.style.backgroundColor) {
                        mytd.style.backgroundColor = even ? evenColor : oddColor;
                    }
                }
            }
            // flip from odd to even, or vice-versa
            even =  ! even;
        }
    }
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

// From Javascript book, p. 2654

function isblank(s) {
    for (var i = 0; i < s.length; i++) {
        var c = s.charAt(i);
        if ((c != ' ') && (c != '\n') && (c != '')) return false;
    }
    return true;
}
