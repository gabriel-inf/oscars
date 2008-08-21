/*
AuthorizationState.js:  Class handling state associated with authorizations
                        forms
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
setRpc()
saveAuthState(attributeName, resourceName, permissionName, constraintName,
              constraintValue)
recoverAuthState(formNode)
clearAuthState()
constraintChoices(menuName)
setConstraintType(resourceName, permissionName, constraintName)
*/

dojo.provide("oscars.AuthorizationState");

dojo.declare("oscars.AuthorizationState", null, {
    constructor: function(){
        this.rpcData = {};
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
        var formNode = dijit.byId("authDetailsForm").domNode;
        formNode.oldAuthAttributeName = attributeName;
        formNode.oldResourceName = resourceName;
        formNode.oldPermissionName = permissionName;
        formNode.oldConstraintName = constraintName;
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
        this.setConstraintType(this.resourceName, this.permissionName,
                               this.constraintName);
    },

    clearAuthState: function() {
        var formNode = dijit.byId("authDetailsForm").domNode;
        var menu = formNode.authAttributeName;
        this.attributeName = menu.options[0].value;
        menu = formNode.resourceName;
        this.resourceName =  menu.options[0].value;
        menu = formNode.permissionName;
        this.permissionName = menu.options[0].value;
        menu = formNode.constraintName;
        this.constraintName = menu.options[0].value;
        formNode.constraintValue.value = "";
        var constraintTypeNode = dojo.byId("constraintType");
        constraintTypeNode.innerHTML = "";
        formNode.oldAuthAttributeName = "";
        formNode.oldResourceName = "";
        formNode.oldPermissionName = "";
        formNode.oldConstraintName = "";
    },

    constrainChoices: function(menuName) {
        var i;
        var j;
        var val;
        var illegalChoice;
        var formNode = dijit.byId("authDetailsForm").domNode;
        var resourceMenu = formNode.resourceName;
        var permissionMenu = formNode.permissionName;
        var constraintMenu = formNode.constraintName;
        var resourceName = resourceMenu.options[resourceMenu.selectedIndex].value;
        var permissionName =
            permissionMenu.options[permissionMenu.selectedIndex].value;
        var constraintName =
            constraintMenu.options[constraintMenu.selectedIndex].value;
        // constrain permissions menu
        if (menuName == "resourceName") {
            for (i=0; i < permissionMenu.options.length; i++) {
                val = permissionMenu.options[i].value;
                if (this.rpcData[resourceName][val]) {
                    permissionMenu.options[i].disabled = false;
                } else {
                    permissionMenu.options[i].disabled = true;
                }
            }
        }
        // constrain constraints menu
        else if (menuName == "permissionName") {
        // constraint constraints value and type
        } else if (menuName == "constraintName") {
            var constraintValueNode = formNode.constraintValue;
            var constraintTypeNode = dojo.byId("constraintType");
            if (constraintName == 'none') {
                constraintValueNode.disabled = true;
                constraintTypeNode.disabled = true;
                constraintTypeNode.innerHTML = "";
            } else {
                constraintValueNode.disabled = false;
                this.setConstraintType(resourceName, permissionName, constraintName);
            }
        }
    },

    setConstraintType: function(resourceName, permissionName, constraintName) {
        var constraintTypeNode = dojo.byId("constraintType");
        // consistency check
        if (!this.rpcData[resourceName] ||
            !this.rpcData[resourceName][permissionName] ||
            !this.rpcData[resourceName][permissionName].hasOwnProperty(constraintName)) {
            var oscarsStatus = dojo.byId("oscarsStatus");
            oscarsStatus.className = "failure";
            oscarsStatus.innerHTML = "Triplet resource: " +
                resourceName +
                ", permission: " + permissionName +
                ", constraint: " + constraintName + " not allowed." +
                "Contact an admin.";
            constraintTypeNode.innerHTML = "";
            return;
        } 
        var constraintType =
            this.rpcData[resourceName][permissionName][constraintName];
        if (constraintName != 'none') {
            constraintTypeNode.innerHTML = constraintType;
        } else {
            constraintTypeNode.innerHTML = "";
        }
    }
});
