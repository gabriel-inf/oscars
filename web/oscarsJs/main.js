/*
main.js:        Javascript functions not handled by Dojo
Last modified:  January 28, 2007
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
zebraStripe(row, i, modnumber)
initStatusTime()
updateStatusTime()
*/

monthName = ['January', 'February', 'March', 'April', 'May',
   'June', 'July', 'August', 'September', 'October', 'November', 'December'];

var LIGHT_STRIPE = "#ffffff";
var DARK_STRIPE = "#eaf0f6";

dojo.provide("oscarsJs.main");

function zebraStripe(row, i, modnumber) {
    if ((i % 2) == modnumber) { row.style.backgroundColor = LIGHT_STRIPE; }
    else { row.style.backgroundColor = DARK_STRIPE; }
}

// Outputs datetime with format: 2005-07-01 13:00.
function dateTimeStr(localDate) {
    var formattedDt = localDate.getFullYear() + "-";
    var digits = localDate.getMonth() + 1;
    formattedDt += (digits > 9 ? '' : '0') + digits + '-';
    digits = localDate.getDate();
    formattedDt += (digits > 9 ? '' : '0') + digits + ' ';
    digits = localDate.getHours();
    formattedDt += (digits > 9 ? '' : '0') + digits + ':';
    digits = localDate.getMinutes();
    formattedDt += (digits > 9 ? '' : '0') + digits;
    return formattedDt;
}

// Outputs datetime with format: July 1, 2005 13:00.
function updateStatusTime(statusTime) {
    var localDate = new Date();
    var currentMonth = localDate.getMonth();
    var formattedDt = monthName[currentMonth] + " " + localDate.getDate() +
                   ", " + localDate.getFullYear() + " ";

    digits = localDate.getHours();
    formattedDt += (digits > 9 ? '' : '0') + digits + ':';
    digits = localDate.getMinutes();
    formattedDt += (digits > 9 ? '' : '0') + digits;
    statusTime.value = formattedDt;
}

function initStatusTime() {
    var statusTime = dojo.byId('statusTime');
    updateStatusTime(statusTime);
    setInterval(function() { updateStatusTime(statusTime); }, 60000);
}
