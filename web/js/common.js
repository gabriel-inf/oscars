/*
common.js:      Javascript functions for form submission
Last modified:  Februrary 27, 2007
David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)
*/

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
        // remove unchecked roles
        if (form.roles) {
        	for (i=0; i<form.roles.length; i++){
	        	if (form.roles[i].checked != true) {
	      	    	form.roles[i].value='';
	      	    }
	      	}
		}

        for (var e=0; e < numElements; e++) {
   
            if (formElements[e].value && formElements[e].name) {
                if (formElements[e].className == 'SOAP' || formElements[e].className == 'required') {
                        params +=  formElements[e].name + '=' + formElements[e].value + '&';
                }
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
        if (id == 'odate') {
            var userMonth = localDate.getMonth() + 1;
            node.data = localDate.getFullYear() + "-" + 
                        userMonth + "-" +
                        localDate.getDate();
        }
        else if (id == 'otime') {
            var minutes = localDate.getMinutes();
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            node.data = localDate.getHours() + ":" + minutes;
        }
    }
}
