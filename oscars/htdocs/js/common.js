/*
common.js:      Javascript functions for form submission
Last modified:  July 19, 2006
David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)
*/

/* List of functions:
submitForm(form, params, checkFunction)
newSection(params)
handleResponse(xmlhttp)
setInnerHTML(responseDom, serializer, tagName)
setNavigationTab(responseDom)
zebraStripe(row, i, modnumber)
*/

var dateHandler;

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
    dateHandler = new DateHandler();
    dateHandler.init();
}

addEvent(window, 'load', init, false);

// Get sections of login page (done this way to make it customizable)
function initPage() {
    var emptyStr = "";
    var xmlhttp = new XMLHttpRequest();
    var serializer = new XMLSerializer();
    xmlhttp.open('GET', '/xml/init.xml', false);
    xmlhttp.send(emptyStr);
    var responseDom = xmlhttp.responseXML;
    setInnerHTML(responseDom, serializer, 'titles');    // set titles section
    setInnerHTML(responseDom, serializer, 'content');   // set main content
    setInnerHTML(responseDom, serializer, 'info');      // set info section
    setInnerHTML(responseDom, serializer, 'notices');   // set notices section
}

// Checks validity of form settings, and uses Sarissa to post request
// and get back result.
function submitForm( form, params, checkFunction ) {
    if (checkFunction) {
        var valid = checkFunction( form );
        if (!valid) { return false; }
    } 

    // adapted from http://www.devx.com/DevX/Tip/17500
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.open('POST', '/perl/adapt.pl', false);
    xmlhttp.setRequestHeader('Content-Type',
                             'application/x-www-form-urlencoded');
    if (form.elements) {
        var formElements = form.elements;
        var numElements = form.elements.length;
        for (var e=0; e < numElements; e++) {
            if (formElements[e].value && formElements[e].name && (formElements[e].className == 'SOAP' || formElements[e].className == 'required')) {
                params +=
                   formElements[e].name + '=' + formElements[e].value + ';';
            }
        }
    }
    xmlhttp.send(params.substring(0, params.length-1));
    handleResponse(xmlhttp);
    return false;
}

// Updates status and main portion of page (same as above, but without
// form submission).
function newSection( params ) {
    var emptyStr = "";
    var xmlhttp = new XMLHttpRequest();
    var url = '/perl/adapt.pl?' + params;
    xmlhttp.open('GET', url, false);
    xmlhttp.send(emptyStr);
    handleResponse(xmlhttp);
    return false;
}

// Handle response from XMLHttpRequest.
function handleResponse(xmlhttp) {
    //alert(xmlhttp.responseText);
    var responseDom = xmlhttp.responseXML;
    var serializer = new XMLSerializer();
    //alert(serializer.serializeToString(responseDom));

    setInnerHTML(responseDom, serializer, 'status');        // update status
    setInnerHTML(responseDom, serializer, 'content');       // get main content
    // unset previous page tab and set new one
    setNavigationTab(responseDom);
    // TODO:  FIX, should not call these except upon login
    setInnerHTML(responseDom, serializer, 'navigation');    // reset active tab
    // clear information section; for now, only happens immediately after login
    setInnerHTML(responseDom, serializer, 'info');

    // only used with time zones in ReservationCreateForm
    // TODO:  FIX with some sort of pattern
    var timeNode = document.getElementById('time-zone-options');
    if (timeNode) { timeNode.innerHTML = dateHandler.timeZoneOptions(); }
    timeNode = document.getElementById('local-time-zone');
    if (timeNode) { timeNode.innerHTML = dateHandler.localTimeZone(); }

    sortables_init();
}

// Sets innerHTML for document element if corresponding tag exists in response.
function setInnerHTML( responseDom, serializer, tagName ) {
    var nodes = responseDom.getElementsByTagName(tagName);
    if ( !nodes.length ) { return; }
    // tag name in response and id in document must be identical
    var elem = document.getElementById(tagName);
    elem.innerHTML = serializer.serializeToString(nodes[0]);
}

// Sets innerHTML for document element if corresponding tag exists in response.
function setNavigationTab( responseDom ) {
    var nodes = responseDom.getElementsByTagName('active');
    if (!nodes.length) { return; }
    var activeTab = nodes[0].childNodes[0].data;
    nodes = responseDom.getElementsByTagName('previous');
    if (nodes.length) {
        var previousTab = nodes[0].childNodes[0].data;
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
    if ((i % 2) == modnumber) {
        row.style.backgroundColor = LIGHT_STRIPE; 
    }
    else {
        row.style.backgroundColor = DARK_STRIPE; 
    }
}

