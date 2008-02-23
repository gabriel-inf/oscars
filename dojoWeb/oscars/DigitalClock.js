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
    var month = localDate.getMonth();
    var year = localDate.getFullYear();
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
    formattedDt = month + 1 + "/" + localDate.getDate() + "/" + year;
    startDateDefault.innerHTML = formattedDt;
    startTimeDefault.innerHTML = formattedTime;
    var endDateDefault = dojo.byId('endDateDefault');
    var endTimeDefault = dojo.byId('endTimeDefault');
    // get default end time (4 minutes in future)
    var endDate = new Date(ms + 60*4*1000);
    month = endDate.getMonth();
    formattedDt = month + 1 + "/" + endDate.getDate() + "/" +
                  endDate.getFullYear();
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

oscars.DigitalClock.convertDateTime = function(jsDate, dateId, timeId,
                                               useCurrent) {
    // contains seconds, and any error message
    var dateFields = oscars.DigitalClock.convertDateWidget(jsDate, dateId);
    var timeFields = oscars.DigitalClock.convertTimeWidget(jsDate, timeId, 
                                                           useCurrent);
    var seconds = null;
    //console.log("year: " + dateFields.year + ", month: " + dateFields.month + ", day: " + dateFields.day + ", hour: " + timeFields.hour + ", minute: " + timeFields.minute);
    var finalDate =
        new Date(dateFields.year, dateFields.month, dateFields.day,
                 timeFields.hour, timeFields.minute, 0, 0);
    seconds = finalDate.getTime()/1000;
    return seconds;
}

oscars.DigitalClock.convertDateWidget = function(jsDate, dateId) {
    var dateFields = new Object();
    var dateWidget = dijit.byId(dateId);
    if  (oscars.Form.isBlank(dateWidget.getDisplayedValue())) {
        dateFields.year = jsDate.getFullYear();
        dateFields.month = jsDate.getMonth();
        dateFields.day = jsDate.getDate();
        dateWidget.setValue(jsDate);
    } else {
        var year = jsDate.getFullYear();
        var fields = dateWidget.getDisplayedValue().split("/");
        dateFields.month = fields[0]-1;
        dateFields.day = fields[1];
        dateFields.year = fields[2];
    }
    return dateFields;
}

oscars.DigitalClock.convertTimeWidget = function(jsDate, timeId, useCurrent) {
    var timeFields = new Object();
    var timeWidget = dijit.byId(timeId);
    // if day of year filled in, but time isn't, use current time
    if (oscars.Form.isBlank(timeWidget.getValue())) {
        if (useCurrent) {
            timeFields.hour = jsDate.getHours();
            timeFields.minute = jsDate.getMinutes();
            if (timeFields.minute >= 10) {
                timeWidget.setValue(timeFields.hour + ":" + timeFields.minute);
            } else {
                timeWidget.setValue(timeFields.hour + ":0" + timeFields.minute);
            }
        } else {
            timeFields.hour = 0;
            timeFields.minute = 0;
            timeWidget.setValue("00:00");
        }
    } else {
        var fields = timeWidget.getValue().split(":");
        timeFields.hour = fields[0];
        timeFields.minute = fields[1];
    }
    return timeFields;
}
