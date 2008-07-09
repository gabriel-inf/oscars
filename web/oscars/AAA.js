/*
AAA.js:     Handles form for basic AAA tables.
Last modified:  July 2, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
tableOp(tableName, opName)
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget)
createInstitutionGrid()
*/

dojo.provide("oscars.AAA");

oscars.AAA.tableOp = function (tableName, opName) { 
    if (tableName == "institution") {
        var institutionGrid = dijit.byId("institutionGrid");
        if (opName == "add") {
            // note there has to be at least one character, or revert fails
            institutionGrid.model.store.newItem({institutionName: " "});
        } else if (opName == "delete") {
            institutionGrid.removeSelectedRows();
        } else if (opName == "revert") {
            institutionGrid.model.store.revert();
        }
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

