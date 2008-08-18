/*
Authorizations.js:  Handles authorizations list functionality.
                    Note that it uses a grid.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
goToAdd()
handleReply(responseObject, ioArgs)
tabSelected(contentPane, oscarsStatus)
refreshAuthGrid()
onAuthRowSelect(evt)
*/

dojo.provide("oscars.Authorizations");

// Goes to the add version of the authorization details page.
// This is a client side only function.
oscars.Authorizations.goToAdd = function () {
    oscars.AuthorizationDetails.resetFields(false);
    var modifyAuthorizationNode = dojo.byId("modifyAuthorizationDisplay");
    modifyAuthorizationNode.style.display = "none";
    var addAuthorizationNode = dojo.byId("addAuthorizationDisplay");
    addAuthorizationNode.style.display = "";
    // set to authorization details tab
    var mainTabContainer = dijit.byId("mainTabContainer");
    var authDetailsPane = dijit.byId("authDetailsPane");
    mainTabContainer.selectChild(authDetailsPane);
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "success";
    oscarsStatus.innerHTML = "Adding authorization";
};

// handles all servlet replies
oscars.Authorizations.handleReply = function (responseObject, ioArgs) {
    if (responseObject.method == "AuthorizationList") {
        if (!oscars.Form.resetStatus(responseObject)) {
            return;
        }
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
        if (responseObject.rpcData) {
            oscarsState.authorizationState.setRpc(responseObject.rpcData);
        }
        var formNode = dijit.byId("authListForm").domNode;
        // ensure server won't send back rpcData again
        formNode.rpc = "set";
        var mainTabContainer = dijit.byId("mainTabContainer");
        var authGrid = dijit.byId("authGrid");
        var model = authGrid.model;
        model.setData(responseObject.authData);
        authGrid.setSortIndex(0, true);
        authGrid.sort();
        authGrid.update();
        authGrid.resize();
        authGrid.resize();
        oscarsState.authGridInitialized = true;
    }
};

// takes action based on this tab being clicked on
oscars.Authorizations.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {
    oscarsStatus.innerHTML = "Authorizations list";
    var authGrid = dijit.byId("authGrid");
    // Creation apparently needs to be programmatic, after the ContentPane
    // has been selected and its style no longer display:none
    if (authGrid && (!oscarsState.authGridInitialized)) {
        dojo.connect(authGrid, "onRowClick", oscars.Authorizations.onAuthRowSelect);
        oscars.Authorizations.refreshAuthGrid();
    }
};

// refresh authorizations list from servlet
oscars.Authorizations.refreshAuthGrid = function () {
    dojo.xhrPost({
        url: 'servlet/AuthorizationList',
        handleAs: "json-comment-filtered",
        load: oscars.Authorizations.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("authListForm").domNode
    });
};

// select authorization details based on row select in grid
oscars.Authorizations.onAuthRowSelect = function (/*Event*/ evt) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    var authDetailsPane = dijit.byId("authDetailsPane");
    // TODO:  disable None options in menus
    var modifyAuthorizationNode = dojo.byId("modifyAuthorizationDisplay");
    modifyAuthorizationNode.style.display = "";
    var addAuthorizationNode = dojo.byId("addAuthorizationDisplay");
    addAuthorizationNode.style.display = "none";
    var authGrid = dijit.byId("authGrid");
    // clear constraint value if any
    var formNode = dijit.byId("authDetailsForm").domNode;
    formNode.reset();
    // clear constraint value if any
    // set four parameters necessary to retrieve authorization
    // dijit.byId doesn't seem to work outside form and tab
    var menu = formNode.authAttributeName;
    menu.options[0].disabled = true;
    var attributeName = authGrid.model.getDatum(evt.rowIndex, 0);
    oscars.Form.setMenuSelected(menu, attributeName);
    menu = formNode.resourceName;
    menu.options[0].disabled = true;
    var resourceName = authGrid.model.getDatum(evt.rowIndex, 1);
    oscars.Form.setMenuSelected(menu, resourceName);
    menu = formNode.permissionName;
    menu.options[0].disabled = true;
    var permissionName = authGrid.model.getDatum(evt.rowIndex, 2);
    oscars.Form.setMenuSelected(menu, permissionName);
    menu = formNode.constraintName;
    var constraintName = authGrid.model.getDatum(evt.rowIndex, 3);
    // constraint can be blank
    if (constraintName) {
        oscars.Form.setMenuSelected(menu, constraintName);
    } else {
        oscars.Form.setMenuSelected(menu, "None");
    }
    formNode.constraintValue.value = authGrid.model.getDatum(evt.rowIndex, 4);
    oscarsState.authorizationState.saveAuthState(attributeName,
            resourceName, permissionName, constraintName,
            formNode.constraintValue.value);
    // uses current authorization state
    oscarsState.authorizationState.setConstraintType();
    // No need to query server; grid already contains all information.
    // Can't set up menus in grid; different fields may require different
    // subsets of values of other fields.
    // Set tab to authorization details, which allows authorization modification
    // and cloning.
    mainTabContainer.selectChild(authDetailsPane);
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "success";
    oscarsStatus.innerHTML = "Modifying authorization";
};
