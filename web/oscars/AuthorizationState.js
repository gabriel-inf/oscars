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
            var constraintType = rpcGrid[i][3];
            if (!this.rpcData[resource]) {
                this.rpcData[resource] = {};
                this.rpcData[resource][permission] = {};
                this.rpcData[resource][permission][constraint] = constraintType;
            } else if (!this.rpcData[resource][permission]) {
                this.rpcData[resource][permission] = {};
                this.rpcData[resource][permission][constraint] = constraintType;
            } else if (!this.rpcData[resource][permission][constraint]) {
                this.rpcData[resource][permission][constraint] = 
                    constraintType;
            }
        }
        //console.dir(this.rpcData);
    },

    saveAuthState: function(attributeName, resourceName, permissionName,
                           constraintName, constraintValue) {
        this.attributeName = attributeName;
        this.resourceName = resourceName;
        this.permissionName = permissionName;
        this.constraintName = constraintName;
        this.constraintValue = constraintValue;
    }, 

    recoverAuthState: function(formNode) {
        var menu = formNode.authAttributeName;
        oscars.Form.setMenuSelected(menu, this.attributeName);
        menu = formNode.resourceName;
        oscars.Form.setMenuSelected(menu, this.resourceName);
        menu = formNode.permissionName;
        oscars.Form.setMenuSelected(menu, this.permissionName);
        menu = formNode.constraintName;
        oscars.Form.setMenuSelected(menu, this.constraintName);
        formNode.constraintValue.value = this.constraintValue;
        this.setConstraintType();
    },

    clearAuthState: function() {
        this.attributeName = 'None';
        this.resourceName = 'None';
        this.permissionName = 'none';
        this.constraintName = 'none';
        this.constraintValue = '';
    },

    constrainChoices: function(menuName) {
        var formNode = dijit.byId("authDetailsForm").domNode;
        //console.log(formNode[menuName].selectedIndex);
    },

    setConstraintType: function() {
        var constraintTypeNode = dojo.byId("constraintType");
        // necessary at this time
        if (!this.rpcData[this.resourceName] ||
            !this.rpcData[this.resourceName][this.permissionName] ||
            !this.rpcData[this.resourceName][this.permissionName][this.constraintName]) {
            constraintTypeNode.innerHTML = "";
            return;
        } 
        var constraintType =
            this.rpcData[this.resourceName][this.permissionName][this.constraintName];
        if (this.constraintName != 'none') {
            constraintTypeNode.innerHTML = constraintType;
        } else {
            constraintTypeNode.innerHTML = "";
        }
    }
});
