/*
Form.js:        Javascript form handling
Last modified:  February 9, 2007
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
initLogin()
submit()
handleReply()
handleError()
*/

dojo.provide("js.Form");

function initLogin() {
    var loginButton = dojo.widget.byId("loginButton");
    dojo.event.connect(loginButton, "onClick", "initSession");
}

function initLogout() {
    var logoutButton = dojo.widget.byId("logoutButton");
    alert(logoutButton);
    dojo.event.connect(logoutButton, "onClick", "closeSession");
}

function initSession() {
    var tabPane = dojo.widget.byId("mainTabContainer");

    var session = dojo.widget.byId("userSession");
    session.setUrl("forms/Session.html");

    // create session tab widgets
    var resvList = dojo.widget.createWidget("ContentPane",
            {widgetId: "reservationList",
             href:     "forms/ListReservations.html", label: "Reservations"});
    var resvDetails = dojo.widget.createWidget("ContentPane",
            {widgetId: "reservationDetails",
             href: "forms/Reservation.html", label: "Reservation Details"});
    var resvCreate = dojo.widget.createWidget("ContentPane",
            {widgetId: "createReservation", 
             href: "forms/CreateReservation.html",
             label: "Create Reservation"});
    var userList = dojo.widget.createWidget("ContentPane",
            {widgetId: "userList",
             href: "forms/UserList.html", label: "Users"});
    var userDetails = dojo.widget.createWidget("ContentPane",
            {widgetId: "user",
             href: "forms/User.html", label: "User Details"});
    // add tabs requiring authorization to container
    tabPane.addChild(resvList);
    tabPane.addChild(resvDetails);
    tabPane.addChild(resvCreate);
    tabPane.addChild(userList);
    tabPane.addChild(userDetails);
    
}

function closeSession() {
    var tabPane = dojo.widget.byId("mainTabContainer");
    var resvList = dojo.widget.byId("resvList");
    var resvDetails = dojo.widget.byId("resvDetails");
    var resvCreate = dojo.widget.byId("resvCreate");
    var userList = dojo.widget.byId("userList");
    var userDetails = dojo.widget.byId("userDetails");
    var session = dojo.widget.byId("userSession");
    session.setUrl("Login.html");
    // remove protected tabs
    tabPane.removeChild(resvList);
    tabPane.removeChild(resvDetails);
    tabPane.removeChild(resvCreate);
    tabPane.removeChild(userList);
    tabPane.removeChild(userDetails);
}

function handleError() {
    alert("error");
}
