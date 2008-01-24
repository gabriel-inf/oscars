/*
Form.js:        Javascript form callback handling
Last modified:  January 23, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
handleReply(responseObject, ioArgs)
handleError(responseObject, ioArgs)
*/

dojo.provide("oscars.Form");

oscars.Form.handleReply = function (responseObject, ioArgs) {
    var status = responseObject.status;
    var mainTabContainer = dijit.byId("mainTabContainer");
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.innerHTML = responseObject.status;
    if (responseObject.success) {
        oscarsStatus.className = "success";
    } else {
        oscarsStatus.className = "failure";
    }
    if (responseObject.method == "AuthenticateUser") {
        var sessionPane = dijit.byId("sessionPane");
            if (responseObject.success) {
            sessionPane.setHref("forms/logout.html");
        }
        if (responseObject.authorizedTabs != null) {
            if (responseObject.authorizedTabs["usersPane"]) {
                var usersPaneTab = dijit.byId("usersPane");
                if (usersPaneTab == null) {
                    usersPaneTab = new dijit.layout.ContentPane(
                        {title:'User List', id: 'usersPane'},
                        dojo.doc.createElement('div'));
                        usersPaneTab.setHref("forms/users.html");
                }
                mainTabContainer.addChild(usersPaneTab, 2);
                usersPaneTab.startup();
            }
            if (responseObject.authorizedTabs["userAddPane"]) {
                var userAddPaneTab = dijit.byId("userAddPane");
                if (userAddPaneTab == null) {
                    userAddPaneTab = new dijit.layout.ContentPane(
                        {title:'Add User', id: 'userAddPane'},
                        dojo.doc.createElement('div'));
                    userAddPaneTab.setHref("forms/userAdd.html");
                }
                mainTabContainer.addChild(userAddPaneTab, 3);
                userAddPaneTab.startup();
            }
        }
    } else if (responseObject.method == "UserLogout") {
        var sessionPane = dijit.byId("sessionPane");
        sessionPane.setHref("forms/login.html");
        if (dijit.byId("usersPane") != null) {
            mainTabContainer.removeChild(dijit.byId("usersPane"));
        }
        if (dijit.byId("userAddPane") != null) {
            mainTabContainer.removeChild(dijit.byId("userAddPane"));
        }
    }
}

oscars.Form.handleError = function (responseObject, ioArgs) {
}

oscars.Form.initState = function() {
    var state = {
        back: function() { console.log("Back was clicked!"); },
        forward: function() { console.log("Forward was clicked!"); },
        changeUrl: "login",
    };
    dojo.back.setInitialState(state);
}

oscars.Form.selectedChanged = function(contentPane) {
    // start of back/forward button functionality
    var state = {
        back: function() {
            console.log("Back was clicked! ");
        },
        forward: function() {
            console.log("Forward was clicked! ");
        },
        changeUrl: contentPane.id,
    };
    dojo.back.addToHistory(state);
}

oscars.Form.hrefChanged = function(newUrl) {
    // start of back/forward button functionality
    var state = {
        back: function() { console.log("Back was clicked!"); },
        forward: function() { console.log("Forward was clicked!"); },
        changeUrl: newUrl,
    };
    dojo.back.addToHistory(state);
}

