/*
Form.js:        Javascript form callback handling
Last modified:  January 18, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
handleReply(responseObject, ioArgs)
handleError(responseObject, ioArgs)
*/

dojo.provide("oscars.Form");

oscars.Form = function() {
    this.placeholder = false;
}

oscars.Form.prototype.handleReply = function (responseObject, ioArgs) {
    var status = responseObject.status;
    var mainTabContainer = dijit.byId("mainTabContainer");
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.innerHTML = responseObject.status;
    if (responseObject.success) {
        oscarsStatus.className = "success";
    } else {
        oscarsStatus.className = "warning";
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
        //mainTabContainer.selectChild(sessionPane);
    }
}

oscars.Form.prototype.handleError = function (responseObject, ioArgs) {
}

