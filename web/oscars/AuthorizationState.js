/*
AuthorizationState.js:  Class handling state associated with authorizations
                        forms
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
testFunction()
*/

dojo.provide("oscars.AuthorizationState");

dojo.declare("oscars.AuthorizationState", null, {
    constructor: function(){
        this.rpcData = {};
    },

    setRpc: function(rpcGrid){
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
    }
});
