/*
Form.js:        Javascript form handling
Last modified:  April 6, 2007
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
initLogin()
submitForm()
handleReply(type, data, evt)
startSession()
closeSession()
*/

dojo.provide("js.Form");

function submitForm() {
    var x = new dojo.io.FormBind({
    formNode: document.forms[0],
    load: handleReply,
    error: handleError });
}


function handleReply(type, data, http) {
    dojo.debug(http["responseText"]);
    startSession();
}


function startSession() {
    var tabContainer = dojo.widget.byId("mainTabContainer");

    var sessionPane = dojo.widget.byId("sessionPane");
    sessionPane.setUrl("forms/session.html");

    // create session tab widgets
    var listReservationsPane = dojo.widget.createWidget("ContentPane",
            {widgetId: "listReservationsPane",
             href:     "forms/listReservations.html", label: "Reservations"});
    var reservationPane = dojo.widget.createWidget("ContentPane",
            {widgetId: "reservationPane",
             href: "forms/reservation.html", label: "Reservation Details"});
    var createReservationPane = dojo.widget.createWidget("ContentPane",
            {widgetId: "createReservationPane", 
             href: "forms/createReservation.html",
             label: "Create Reservation"});
    var userListPane = dojo.widget.createWidget("ContentPane",
            {widgetId: "userListPane",
             href: "forms/userList.html", label: "Users"});
    var userPane = dojo.widget.createWidget("ContentPane",
            {widgetId: "userPane",
             href: "forms/user.html", label: "User Details"});
    // add tabs requiring authorization to container
    tabContainer.addChild(listReservationsPane);
    tabContainer.addChild(reservationPane);
    tabContainer.addChild(createReservationPane);
    tabContainer.addChild(userListPane);
    tabContainer.addChild(userPane);
}


function closeSession() {
    var tabContainer = dojo.widget.byId("mainTabContainer");
    var listReservationsPane = dojo.widget.byId("listReservationsPane");
    var reservationPane = dojo.widget.byId("reservationPane");
    var createReservationPane = dojo.widget.byId("createReservationPane");
    var userListPane = dojo.widget.byId("userListPane");
    var userPane = dojo.widget.byId("userPane");
    var sessionPane = dojo.widget.byId("sessionPane");
    sessionPane.setUrl("login.html");
    // remove protected tabs
    tabContainer.removeChild(listReservationsPane);
    tabContainer.removeChild(reservationPane);
    tabContainer.removeChild(createReservationPane);
    tabContainer.removeChild(userListPane);
    tabContainer.removeChild(userPane);
}


function handleError(type, evt) {
    var msg = evt["message"];
    // The message has non-useful information in it for the user.
    // The servlet inserts a series of *'s in the message so that
    // the non-helpful info can be parsed out.
    var lastIndex = msg.lastIndexOf("*");
    dojo.debug(msg.substring(lastIndex+2, msg.length));
}
