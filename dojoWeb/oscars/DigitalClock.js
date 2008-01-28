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
    oscarsState.ms = localDate.getTime();
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

// sets default times on create reservation form
oscars.DigitalClock.updateDefaultClocks =
  function(startDateDefault, startTimeDefault, endDateDefault, endTimeDefault) {
    // TODO:  FIX, won't be in sync with main clock part of the time
    var localDate = new Date(oscarsState.ms);
    var currentMonth = localDate.getMonth();
    var formattedDt = currentMonth + 1 + "/" + localDate.getDate() +
                   "/" + localDate.getFullYear();

    digits = localDate.getHours();
    formattedTime = (digits > 9 ? '' : '0') + digits + ':';
    digits = localDate.getMinutes();
    formattedTime += (digits > 9 ? '' : '0') + digits;
    startDateDefault.innerHTML = formattedDt;
    startTimeDefault.innerHTML = formattedTime;
    // get default end time
    var endDate = new Date(oscarsState.ms + 60*4*1000);
    currentMonth = endDate.getMonth();
    formattedDt = currentMonth + 1 + "/" + endDate.getDate() +
                   "/" + endDate.getFullYear();
    digits = endDate.getHours();
    formattedTime = (digits > 9 ? '' : '0') + digits + ':';
    digits = endDate.getMinutes();
    formattedTime += (digits > 9 ? '' : '0') + digits;
    endDateDefault.innerHTML = formattedDt;
    endTimeDefault.innerHTML = formattedTime;
}

oscars.DigitalClock.initDefaultClocks = function () {
    var startDateDefault = document.getElementById('startDateDefault');
    var startTimeDefault = document.getElementById('startTimeDefault');
    var endDateDefault = document.getElementById('endDateDefault');
    var endTimeDefault = document.getElementById('endTimeDefault');

    oscars.DigitalClock.updateDefaultClocks(
        startDateDefault, startTimeDefault, endDateDefault, endTimeDefault);
    setInterval(
        function() {
            oscars.DigitalClock.updateDefaultClocks(
                startDateDefault, startTimeDefault,
                endDateDefault, endTimeDefault); }, 60000);
}
