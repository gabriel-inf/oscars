/*
Javascript functions for main pagessss
Last modified: June 6, 2005
Soo-yeon Hwang (dapi@umich.edu)
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
print_navigation_bar(activePage)
print_admin_bar(activePage)
print_current_date()
hasClass(obj)
stripe(id)
*/


var monthMapping = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];


// ** print current date (format: July 7, 2004) **
function print_current_date(fr, useLocal)
{
    var currentDate = new Date();
    var currentMonth;

    if (useLocal == 'local') {
        currentMonth = currentDate.getMonth();
    }
    else {
        currentMonth = currentDate.getUTCMonth();
    }

    var monthName = monthMapping[currentMonth];

    if (useLocal != 'local') {
        currentMinutes = currentDate.getUTCMinutes();
        fr.write( monthName + " " + currentDate.getUTCDate() + ", " + currentDate.getUTCFullYear() + " " + currentDate.getUTCHours() + ":");
    }
    else {
        currentMinutes = currentDate.getMinutes();
        fr.write( monthName + " " + currentDate.getDate() + ", " + currentDate.getFullYear() + " " + currentDate.getHours() + ":");
    }
    if (currentMinutes < 10) { fr.write("0") } ;
    fr.write(currentMinutes);
    if (useLocal != 'local') { fr.write(" (UTC)") } ;
}

// ** prints HTML header
function print_html_header(formType)
{
    if (formType == 'user') {
        document.write('<title>On-demand Secure Circuits and Advance Reservation System</title>');
    }
    else {
        document.write('<title>OSCARS Administration Tool</title>');
    }
    document.write('	<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"a');
    document.write('	<meta name="Author" content="Soo-yeon Hwang">');
    document.write('    <meta name="Keywords" content="">');
    document.write('	<meta name="Description" content="">');
//    document.write('	<meta http-equiv="Refresh" content="100;URL=./">');
}


// ** prints header
function print_header(formType)
{
    document.write('<div>');
    document.write('<table id="pagetitle">');
    document.write('<tr>');
    document.write('    <td><a href="http://www.es.net/" target="_blank"><img src="https://oscars.es.net/images/ESnetLogo.png" width="77" height="58" border="0" alt="ESnet Logo" title="ESnet"></a></td>');
    document.write('    <td>');
    if (formType == 'user') {
        document.write('	<h1>On-demand Secure Circuits and Advance Reservation System (OSCARS)</h1>');
    }
    else {
        document.write('	<h1>OSCARS Administration Tool</h1>');
    }
    document.write('    <p>In collaboration with the <a href="http://www.internet2.edu">Internet2</a> BRUW Project</p>');
    document.write('    </td>');
    document.write('</tr>');
    document.write('</table>');
    document.write('</div>');
}


// ** prints navigation bar, with active page highlighted
function print_navigation_bar(activePage)
{
    document.write('<div>');
    document.write('<ul id="tabnav">');
    if (activePage == 'reservation') {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/reservations/creation_form.pl" class="active" title="Create a new reservation">Make a Reservation</a></li>');
    }
    else {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/reservations/creation_form.pl" title="Create a new reservation">Make a Reservation</a></li>');
    }
    if (activePage == 'reservationlist') {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/reservations/list_form.pl" class="active" title="View/Edit selected list of reservations">View/Edit Reservations</a></li>');
    }
    else {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/reservations/list_form.pl" title="View/Edit selected list of reservations">View/Edit Reservations</a></li>');
    }
    if (activePage == 'userprofile') {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/users/profile_form.pl" class="active" title="View and/or edit your personal information">My Profile</a></li>');
    }
    else {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/users/profile_form.pl" title="View and/or edit your personal information">My Profile</a></li>');
    }
    document.write('<li><a href="https://oscars.es.net/cgi-bin/lib/logout.pl" class="logout" title="Log out on click.">Log Out</a></li>');
    document.write('</ul>');
    document.write('</div>');
}


// ** displays new page in given frame
function update_frame(target, uri)
{
    parent.frames[target].location = uri;
}


// ** prints admin navigation bar, with active page highlighted
// ** TODO:  Fix duplication with previous function later.

function print_admin_bar(activePage)
{
    document.write('<div>');
    document.write('<ul id="tabnav">');
    if (activePage == 'userlist') {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/users/acctlist_form.pl" class="active" title="View list of all accounts">List All Accounts</a></li>');
    }
    else {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/users/acctlist_form.pl" title="View list of all accounts">List All Accounts</a></li>');
    }
    if (activePage == 'adduser') {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/users/add_form.pl" class="active" title="Add a new user account">Add a New User</a></li>');
    }
    else {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/users/add_form.pl" title="Add a new user account">Add a New User</a></li>');
    
    }
    if (activePage == 'adminprofile') {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/users/profile_form.pl" class="active" title="View and/or edit your information">User Profile</a></li>');
    }
    else {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/users/profile_form.pl" title="View and/or edit your information">User Profile</a></li>');
    }
    if (activePage == 'reservationlist') {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/reservations/list_form.pl" class="active" title="View/Edit selected list of reservations">View/Edit Reservations</a></li>');
    }
    else {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/reservations/list_form.pl" title="View/Edit selected list of reservations">View/Edit Reservations</a></li>');
    }
    if (activePage == 'reservation') {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/reservations/creation_form.pl" class="active" title="Create a new reservation">Make a Reservation</a></li>');
    }
    else {
        document.write('<li><a href="https://oscars.es.net/cgi-bin/reservations/creation_form.pl" title="Create a new reservation">Make a Reservation</a></li>');
    }
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
        print_current_date(f, 'local');
        f.write('</script> | ', msg, '</p>');
        f.write('</div>');
        f.write('</body>');
        f.write('</html>');
        f.close();
    }
    else {
        document.write('<div>');
        document.write('<p class="topmessage">');
        print_current_date(document, 'local');
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

// From Javascript book, p. 2654

function isblank(s) {
    for (var i = 0; i < s.length; i++) {
        var c = s.charAt(i);
        if ((c != ' ') && (c != '\n') && (c != '')) return false;
    }
    return true;
}
