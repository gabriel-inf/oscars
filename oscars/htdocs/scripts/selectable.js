/*
selectable.js:      Javascript functions for selecting table cells, getting the 
                    inner text, and highlighting, and row addition and deletion.

At this point, just used in ManageResources and ManageAuthorizations pages.
Borrows heavily from sorttable.js in this directory.

Last modified:  January 27, 2005
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
tse_makeSelectable(table)
tse_addTable(table)
tse_processCell(cell)
*/

var selected_user;
var selected_role;
var selected_resource;
var selected_permission;

var HIGHLIGHT_STYLE = "1px solid rgb(0, 255, 0)";
var LIGHT_STRIPE = "#ffffff";
var DARK_STRIPE = "#eaf0f6";

function tse_zebraStripe(row, i, modnumber) {
    if ((i % 2) == modnumber) {
        row.style.backgroundColor = LIGHT_STRIPE; 
    }
    else {
        row.style.backgroundColor = DARK_STRIPE; 
    }
}


function tse_makeSelectable(table) {
    if (table.rows && table.rows.length > 0) {
        var tableRow = table.rows[0];
    }
    if (!tableRow) return;
    
    // make cells selectable (except for first row, which is used by sorttable)
    for (var i=1; i<table.rows.length; i++) {
        tableRow = table.rows[i];
        for (var j=0; j<tableRow.cells.length; j++) {
            var cell = tableRow.cells[j];
            var txt = ts_getInnerText(cell);
            if ((table.id == 'Authorizations.Users') ||
                (table.id == 'Authorizations.Roles')) {
                cell.innerHTML = '<td style="font-size: .8em" onclick="return tse_selectUser(this);">' + txt + '</td>';
            }
            else {
                cell.innerHTML = '<td style="font-size: .9em" onclick="return tse_processCell(this);">' + txt + '</td>';
            }
        }
    }
}


function tse_processCell(cell) {
    var removed = 0;

    if (!selected_user) { return false; }
    var txt = ts_getInnerText(cell);
    var table = getParent(cell, 'TABLE');
    if (cell.style.border == HIGHLIGHT_STYLE) {
        cell.style.border = "";
        removed = 1;
    }
    else {
        cell.style.border = HIGHLIGHT_STYLE;
    }
    if (table.id == 'Authorizations.Resources') {
         if (!removed) { selected_resource = txt; }
         else { selected_resource = ''; }
     }
    else if (table.id == 'Authorizations.Permissions') {
         if (!removed) { selected_permission = txt; }
         else { selected_permission = ''; }
    }
    return false;
}


function tse_addAuthorization(doc) {
    if (!selected_user) { return false; }

    var params = 'server=AAAS;method=ManageAuthorizations;' +
            'op=addAuthorization;' +
            'user_dn=' selected_user + ';resource_name=' + selected_resource +
            ';permission_name=' + selected_permission + ';';
    new_section(params);
    selected_resource = '';
    selected_permission = '';
    return false;
}


function tse_selectUser(cell) {
    selected_user = ts_getInnerText(cell);
    var params = 'server=AAAS;method=ManageAuthorizations;' +
           'op=selectUser;user_dn=' + selected_user + ';';
    return new_section(params);
}
