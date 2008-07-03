/*
AAA.js:     Handles form for basic AAA tables.
Last modified:  June 24, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
postInstitutionAdd()
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget)
*/

dojo.provide("oscars.AAA");

oscars.AAA.postTableOp = function () { 
    var valid = dijit.byId("aaaForm").validate();
    if (!valid) {
        return;
    }
};

// handles reply from request to server to operate on AAA table
oscars.AAA.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject, true)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
};

// take action based on this tab being selected
oscars.AAA.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* Boolean */ oscarsStatus,
        /* Boolean */ changeStatus) {
    if (changeStatus) {
        oscarsStatus.innerHTML = "Basic AAA Management";
    }
    var institutionGrid = dijit.byId("institutionGrid");
    if ((institutionGrid != null) && (!oscarsState.aaaGridsInitialized)) {
        oscars.AAA.createInstitutionGrid();
        oscarsState.aaaGridsInitialized = true;
    }
};

// return initial institution list from servlet
oscars.AAA.createInstitutionGrid = function () {
    var institutionGrid = dijit.byId("institutionGrid");
    var newStore = new dojo.data.ItemFileWriteStore(
                      {url: 'servlet/AAA?table=institution&op=list'});
    var newModel = new dojox.grid.data.DojoData(
                      null, newStore,
                      {query: {institutionName: '*'}, clientSort: true});
    institutionGrid.setModel(newModel);
    institutionGrid.refresh();
    institutionGrid.resize();
    institutionGrid.render();
};

