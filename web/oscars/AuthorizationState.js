/*
AuthorizationState.js:  Class handling state associated with authorizations
                        forms
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
setRpc()
*/

dojo.provide("oscars.AuthorizationState");

dojo.declare("oscars.AuthorizationState", null, {
    constructor: function(){
        this.rpcData = {};
        this.clearAuthState();
    },

    // Not all combinations of these triplets are permissible.  For example,
    // choose a resource may disable some options in the permissions menu.
    setRpc: function(rpcGrid) {
        for (var i=0; i < rpcGrid.length; i++) {
            var resource = rpcGrid[i][0];
            var permission = rpcGrid[i][1];
            var constraint = rpcGrid[i][2];
            if (!this.rpcData[resource]) {
                this.rpcData[resource] = {};
                this.rpcData[resource][permission] = {};
                this.rpcData[resource][permission][constraint] = 1;
            } else if (!this.rpcData[resource][permission]) {
                this.rpcData[resource][permission] = {};
                this.rpcData[resource][permission][constraint] = 1;
            } else if (!this.rpcData[resource][permission][constraint]) {
                this.rpcData[resource][permission][constraint] = 1;
            }
        }
    },

    saveAuthState: function(attributeName, resourceName, permissionName,
                           constraintName, constraintValue) {
        this.attributeName = attributeName;
        this.resourceName = resourceName;
        this.permissionName = permissionName;
        this.constraintName = constraintName;
        this.constraintValue = constraintValue;
    }, 

    recoverAuthState: function(formParam) {
        var menu = formParam.authAttributeName;
        oscars.Form.setMenuSelected(menu, this.attributeName);
        menu = formParam.resourceName;
        oscars.Form.setMenuSelected(menu, this.resourceName);
        menu = formParam.permissionName;
        oscars.Form.setMenuSelected(menu, this.permissionName);
        menu = formParam.constraintName;
        oscars.Form.setMenuSelected(menu, this.constraintName);
        formParam.constraintValue.value = this.constraintValue;
    },

    clearAuthState: function() {
        this.attributeName = null;
        this.resourceName = null;
        this.permissionName = null;
        this.constraintName = null;
        this.constraintValue = null;
    },

    constrainChoices: function(menuName) {
        var formNode = dijit.byId("authDetailsForm").domNode;
        //console.log(formNode[menuName].selectedIndex);
    }
});
