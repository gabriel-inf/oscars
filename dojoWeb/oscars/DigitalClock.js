/*
DigitalClock.js:        Prints out current date and time in various places.
Last modified:  January 31, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
initClock()
updateClocks()
*/

dojo.provide("oscars.DigitalClock");

monthName = ['January', 'February', 'March', 'April', 'May',
   'June', 'July', 'August', 'September', 'October', 'November', 'December'];

// Outputs datetime with format: July 1, 2005 13:00 in main clock.
// Updates default times on create reservation form page
oscars.DigitalClock.updateClocks = function (clock) {
    var localDate = new Date();
    var ms = localDate.getTime();
    var year = localDate.getFullYear().toString();
    var month = localDate.getMonth();
    var formattedDt = monthName[month] + " " + localDate.getDate() +
                   ", " + year + " ";

    digits = localDate.getHours();
    var formattedTime = (digits > 9 ? '' : '0') + digits + ':';
    digits = localDate.getMinutes();
    formattedTime += (digits > 9 ? '' : '0') + digits;
    clock.innerHTML = formattedDt + formattedTime;
    // update default times on create reservation form
    var startDateDefault = dojo.byId('startDateDefault');
    // page not loaded yet
    if (startDateDefault == null) {
        return;
    }
    var startTimeDefault = dojo.byId('startTimeDefault');
    formattedDt = month + 1 + "/" + localDate.getDate() +
                   "/" + year.substring(2);
    startDateDefault.innerHTML = formattedDt;
    startTimeDefault.innerHTML = formattedTime;
    var endDateDefault = dojo.byId('endDateDefault');
    var endTimeDefault = dojo.byId('endTimeDefault');
    // get default end time (4 minutes in future)
    var endDate = new Date(ms + 60*4*1000);
    year = endDate.getFullYear().toString();
    month = endDate.getMonth();
    formattedDt = month + 1 + "/" + endDate.getDate() + "/" + year.substring(2);
    digits = endDate.getHours();
    formattedTime = (digits > 9 ? '' : '0') + digits + ':';
    digits = endDate.getMinutes();
    formattedTime += (digits > 9 ? '' : '0') + digits;
    endDateDefault.innerHTML = formattedDt;
    endTimeDefault.innerHTML = formattedTime;
}

// Init and updateClocks are adapted from the DHTML Utopia book.
oscars.DigitalClock.initClock = function () {
    var clock = dojo.byId('clock');

    oscars.DigitalClock.updateClocks(clock);
    setInterval(function() { oscars.DigitalClock.updateClocks(clock); }, 60000);
}

