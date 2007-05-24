/*
DigitalClock.js:        Prints out current date and time.
Last modified:  April 8, 2007
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
initClock()
updateClock()
*/

dojo.provide("js.DigitalClock");

monthName = ['January', 'February', 'March', 'April', 'May',
   'June', 'July', 'August', 'September', 'October', 'November', 'December'];

// Outputs datetime with format: July 1, 2005 13:00.
function updateClock(clock) {
    var localDate = new Date();
    var currentMonth = localDate.getMonth();
    var formattedDt = monthName[currentMonth] + " " + localDate.getDate() +
                   ", " + localDate.getFullYear() + " ";

    digits = localDate.getHours();
    formattedDt += (digits > 9 ? '' : '0') + digits + ':';
    digits = localDate.getMinutes();
    formattedDt += (digits > 9 ? '' : '0') + digits;
    clock.innerHTML = formattedDt;
}

// Init and updateClock are adapted from the DHTML Utopia book.
function initClock() {
    var clock = document.getElementById('clock');

    updateClock(clock);
    setInterval(function() { updateClock(clock); }, 60000);
}
