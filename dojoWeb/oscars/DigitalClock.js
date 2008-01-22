/*
DigitalClock.js:        Prints out current date and time.
Last modified:  December 18, 2007
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
initClock()
updateClock()
*/

dojo.provide("oscars.DigitalClock");

monthName = ['January', 'February', 'March', 'April', 'May',
   'June', 'July', 'August', 'September', 'October', 'November', 'December'];

// Outputs datetime with format: July 1, 2005 13:00.
oscars.DigitalClock.updateClock = function (clock) {
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
oscars.DigitalClock.initClock = function () {
    var clock = document.getElementById('clock');

    oscars.DigitalClock.updateClock(clock);
    setInterval(function() { oscars.DigitalClock.updateClock(clock); }, 60000);
}
