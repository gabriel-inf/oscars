/*
Institutions.js:     Handles form for institutions table.
Last modified:  July 15, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
manage(opName)
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget)
createInstitutionGrid()
*/

dojo.provide("oscars.Institutions");

oscars.Institutions.manage = function (opName) { 
    var institutionGrid = dijit.byId("institutionGrid");
    if (opName == "add") {
        // note there has to be at least one character, or revert fails
        institutionGrid.model.store.newItem({institutionName: " "});
    } else if (opName == "delete") {
        institutionGrid.removeSelectedRows();
    } else if (opName == "revert") {
        institutionGrid.model.store.revert();
    }
};

// handles reply from request to server to operate on Institutions table
oscars.Institutions.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject, true)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
};

// take action based on this tab being selected
oscars.Institutions.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* Boolean */ oscarsStatus,
        /* Boolean */ changeStatus) {
    if (changeStatus) {
        oscarsStatus.innerHTML = "Institutions Management";
    }
    var institutionGrid = dijit.byId("institutionGrid");
    if ((institutionGrid != null) && (!oscarsState.institutionGridInitialized)) {
        oscars.Institutions.createInstitutionGrid();
        oscarsState.institutionGridInitialized = true;
    }
};

// return initial institution list from servlet
oscars.Institutions.createInstitutionGrid = function () {
    var institutionGrid = dijit.byId("institutionGrid");
    var newStore = new dojo.data.ItemFileWriteStore(
                      {url: 'servlet/Institutions?op=list'});
    var newModel = new dojox.grid.data.DojoData(
                      null, newStore,
                      {query: {institutionName: '*'}, clientSort: true});
    institutionGrid.setModel(newModel);
    institutionGrid.refresh();
    institutionGrid.resize();
    institutionGrid.render();
};

