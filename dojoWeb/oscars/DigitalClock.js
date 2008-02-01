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

    var formattedTime = localDate.getHours() + ":";
    var minute = localDate.getMinutes();
    formattedTime += (minute > 9 ? '' : '0') + minute;
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
    formattedTime = endDate.getHours() + ":";
    minute = endDate.getMinutes();
    formattedTime += (minute > 9 ? '' : '0') + minute;
    endDateDefault.innerHTML = formattedDt;
    endTimeDefault.innerHTML = formattedTime;
}

// Init and updateClocks are adapted from the DHTML Utopia book.
oscars.DigitalClock.initClock = function () {
    var clock = dojo.byId('clock');

    oscars.DigitalClock.updateClocks(clock);
    setInterval(function() { oscars.DigitalClock.updateClocks(clock); }, 60000);
}

// Outputs datetime with format: 1/1/2007 13:00, given seconds since epoch.
oscars.DigitalClock.convertFromSeconds = function(seconds) {
    var jsDate = new Date(seconds*1000);
    var year = jsDate.getFullYear();
    var month = jsDate.getMonth() + 1;
    var day = jsDate.getDate();
    var hour = jsDate.getHours();
    var minute = jsDate.getMinutes();
    var formattedDt = month + "/" + day + "/" + year + " " + hour;
    formattedDt += (minute > 9 ? ':' : ':0') + minute;
    return formattedDt;
}

oscars.DigitalClock.convertDateTime = function(jsDate, dateId, timeId) {
    var year = null;
    var month = null;
    var day = null;
    var hour = null;
    var minute = null;
    var dateWidget = dijit.byId(dateId);
    var timeWidget = dijit.byId(timeId);
    if  ((dateWidget.getDisplayedValue() == null) ||
         (dateWidget.getDisplayedValue() == "")) {
        year = jsDate.getFullYear();
        month = jsDate.getMonth();
        day = jsDate.getDate();
        dateWidget.setValue(jsDate);
    } else {
        year = jsDate.getFullYear();
        var fields = dateWidget.getDisplayedValue().split("/");
        var fullYear = jsDate.getFullYear().toString();
        year = fullYear.substring(0,2) + fields[2];
        month = fields[0]-1;
        day = fields[1];
    }
    // if day of year filled in, but time isn't, use current time
    if ((timeWidget.getValue() == null) ||
        (timeWidget.getValue() == "")) {
        hour = jsDate.getHours();
        minute = jsDate.getMinutes();
        if (minute >= 10) {
            timeWidget.setValue(hour + ":" + minute);
        } else {
            timeWidget.setValue(hour + ":0" + minute);
        }
    } else {
        var fields = timeWidget.getValue().split(":");
        hour = fields[0];
        minute = fields[1];
    }
    //console.log("year: " + year + ", month: " + month + ", day: " + day + ", hour: " + hour + ", minute: " + minute);
    var finalDate = new Date(year, month, day, hour, minute, 0, 0);
    var seconds = finalDate.getTime()/1000;
    return seconds;
}
