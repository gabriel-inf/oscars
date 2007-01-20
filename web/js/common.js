/*
common.js:      Javascript functions for form submission
Last modified:  November 16, 2006
David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)
*/

/*
Copyright (c) 2006, The Regents of the University of California, through
Lawrence Berkeley National Laboratory (subject to receipt of any required
approvals from the U.S. Dept. of Energy). All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

(1) Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

(2) Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

(3) Neither the name of the University of California, Lawrence Berkeley
    National Laboratory, U.S. Dept. of Energy nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

You are under no obligation whatsoever to provide any bug fixes, patches,
or upgrades to the features, functionality or performance of the source
code ("Enhancements") to anyone; however, if you choose to make your
Enhancements available either publicly, or directly to Lawrence Berkeley
National Laboratory, without imposing a separate written license agreement
for such Enhancements, then you hereby grant the following license: a
non-exclusive, royalty-free perpetual license to install, use, modify,
prepare derivative works, incorporate into other computer software,
distribute, and sublicense such enhancements or derivative works thereof,
in binary and source code form. */

/* List of functions:
submitForm(form, methodName, params)
newSection(methodName, params)
handleResponse(xmlhttp, methodName)
setInnerHTML(responseDom, serializer, tagName)
setNavigationTab(responseDom)
zebraStripe(row, i, modnumber)
initClock()
updateClock()
convertTimes(responseDom)
*/

monthName = ['January', 'February', 'March', 'April', 'May',
   'June', 'July', 'August', 'September', 'October', 'November', 'December'];

// Next two functions adapted from DHTML Utopia book.
function addEvent(elm, evType, fn, useCapture) {
    if (elm.addEventListener) {
        elm.addEventListener(evType, fn, useCapture);
        return true;
    } else if (elm.attachEvent) {
        var r = elm.attachEvent('on' + evType, fn);
        return r;
    } else {
        elm['on' + evType] = fn;
    }
}

function init() {
    if (!document.getElementById)
        return;
    initPage();
    initClock();
}

addEvent(window, 'load', init, false);

// Get sections of login page (done this way to make it customizable)
function initPage() {
    var emptyStr = "";
    var xmlhttp = new XMLHttpRequest();
    var serializer = new XMLSerializer();
    xmlhttp.open('GET', 'xml/init.xml', false);
    xmlhttp.send(emptyStr);
    var responseDom = xmlhttp.responseXML;
    setInnerHTML(responseDom, serializer, 'titles');    // set titles section
    setInnerHTML(responseDom, serializer, 'content');   // set main content
    setInnerHTML(responseDom, serializer, 'info');      // set info section
    setInnerHTML(responseDom, serializer, 'notices');   // set notices section
}

// Checks validity of form settings, and uses Sarissa to post request
// and get back result.
function submitForm( form, methodName, params ) {
    if (validateParams[methodName]) {
        var valid = validateParams[methodName]( form );
        if (!valid) { return false; }
    } 
    if (!params) { params = ''; }
    if (form.elements) {
        var formElements = form.elements;
        var numElements = form.elements.length;
        for (var e=0; e < numElements; e++) {
            if (formElements[e].value && formElements[e].name &&
                    (formElements[e].className == 'SOAP' ||
                    formElements[e].className == 'required')) {
                params +=
                   formElements[e].name + '=' + formElements[e].value + '&';
            }
        }
    }

    // adapted from http://www.devx.com/DevX/Tip/17500
    var xmlhttp = new XMLHttpRequest();
    var url = 'servlet/' + methodName;

    if (params) { url += '?' + params; }
    xmlhttp.open('POST', url, false);
    xmlhttp.setRequestHeader('Content-Type',
                             'application/x-www-form-urlencoded');
    xmlhttp.send(params.substring(0, params.length-1));
    handleResponse(xmlhttp, methodName);
    return false;
}

// Updates status and main portion of page (same as above, but without
// form submission).
function newSection( methodName, params ) {
    var emptyStr = "";
    var xmlhttp = new XMLHttpRequest();
    var url = 'servlet/' + methodName;
    if (params) { url += '?' + params; }
    xmlhttp.open('GET', url, false);
    xmlhttp.send(emptyStr);
    handleResponse(xmlhttp, methodName);
    return false;
}

// Handle response from XMLHttpRequest.
function handleResponse(xmlhttp, methodName) {
    //alert(xmlhttp.responseText);
    var responseDom = xmlhttp.responseXML;
    var serializer = new XMLSerializer();
    //alert(serializer.serializeToString(responseDom));

    // reset active tab
    if (methodName == 'AuthenticateUser') {
        setInnerHTML(responseDom, serializer, 'navigation');
        // clear information section
        setInnerHTML(responseDom, serializer, 'info');
        convertTimes(responseDom);
    }
    else if ((methodName == 'ListReservations') ||
             (methodName == 'CreateReservation') ||
             (methodName == 'CancelReservation') ||
             (methodName == 'QueryReservation')) {
        convertTimes(responseDom);
    }
    else if (methodName == 'CreateReservationForm') {
        outputDefaults(responseDom);
    }
    setInnerHTML(responseDom, serializer, 'status');      // update status
    setInnerHTML(responseDom, serializer, 'content');     // get main content

    // unset previous page tab and set new one
    setNavigationTab(responseDom);

    sortables_init();
}

// Sets innerHTML for document element if corresponding tag is in response.
function setInnerHTML( responseDom, serializer, tagName ) {
    var elems = responseDom.getElementsByTagName(tagName);
    if ( !elems.length ) { return; }
    // tag name in response and id in document must be identical
    var elem = document.getElementById(tagName);
    elem.innerHTML = serializer.serializeToString(elems[0]);
}

// Sets innerHTML for document element if corresponding tag is in response.
function setNavigationTab( responseDom ) {
    var elems = responseDom.getElementsByTagName('active');
    if (!elems.length) { return; }
    var activeTab = elems[0].childNodes[0].data;
    elems = responseDom.getElementsByTagName('previous');
    if (elems.length) {
        var previousTab = elems[0].childNodes[0].data;
        if (previousTab && !isBlank(previousTab)) {
            var elem = document.getElementById(previousTab);
            elem.className = '';
        }
    }
    elem = document.getElementById(activeTab);
    elem.className = 'active';
}

var LIGHT_STRIPE = "#ffffff";
var DARK_STRIPE = "#eaf0f6";

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

// Takes epoch milliseconds and formats using dateTimeStr
function convertTimes(responseDom) {
    var elems = responseDom.getElementsByTagName('td');

    var numElements = elems.length;
    for (var e=0; e < numElements; e++) {
        node = elems[e].childNodes[0];
        if (elems[e].hasAttribute('class') && 
            elems[e].getAttribute('class') == 'dt') {
            var intDt = node.data / 1;
            var dt = new Date(intDt);
            var formattedStr = dateTimeStr(dt);
            node.data = formattedStr;
        }
    }
}

// Print defaults for time settings, and valid ranges
function outputDefaults(responseDom) {
    var localDate = new Date();
    var elems = responseDom.getElementsByTagName('td');

    var numElements = elems.length;
    for (var e=0; e < numElements; e++) {
        if (!elems[e].hasAttribute('id')) { continue; }
        node = elems[e].childNodes[0];
        var id = elems[e].getAttribute('id');
        if (id == 'oyear') {
            node.data = localDate.getYear() + 1900;  // Javascript weirdness
        }
        else if (id == 'omonth') {
            node.data = (localDate.getMonth()+1) + " (1-12)";
        }
        else if (id == 'odate') {
            node.data = localDate.getDate() + " (1-31)";
        }
        else if (id == 'ohour') {
            node.data = localDate.getHours() + " (0-23)";
        }
        else if (id == 'ominute') {
            node.data = localDate.getMinutes() + " (0-59)";
        }
    }
}
