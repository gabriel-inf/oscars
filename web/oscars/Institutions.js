/*
Institutions.js:     Handles form for institutions table.
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
    var valid;
    var oscarsStatus = dojo.byId("oscarsStatus");
    var formNode = dijit.byId("institutionsForm").domNode;
    var choiceType = dojo.byId("institutionOpChoice");
    var editWidget = dijit.byId("institutionEditName");
    if (opName == "add") {
        editWidget.required = false;
        valid = dijit.byId("institutionsForm").validate();
        formNode.institutionEditName.value = "";
        formNode.saveType.value = "add";
        choiceType.innerHTML = "Adding";
    } else if (opName == "delete") {
        editWidget.required = true;
        valid = dijit.byId("institutionsForm").validate();
        if (!valid) {
            return;
        } 
        dojo.xhrPost({
            url: 'servlet/Institutions?op=delete',
            handleAs: "json-comment-filtered",
            load: oscars.Institutions.handleReply,
            error: oscars.Form.handleError,
            form: formNode
        });
        formNode.saveType.value = "";
        choiceType.innerHTML = "";
    } else if (opName == "save") {
        editWidget.required = true;
        valid = dijit.byId("institutionsForm").validate();
        if (!valid) {
            return;
        } 
        if (formNode.saveType.value == "add") {
            dojo.xhrPost({
                url: 'servlet/Institutions?op=add',
                handleAs: "json-comment-filtered",
                load: oscars.Institutions.handleReply,
                error: oscars.Form.handleError,
                form: formNode
            });
        } else {
            dojo.xhrPost({
                url: 'servlet/Institutions?op=modify',
                handleAs: "json-comment-filtered",
                load: oscars.Institutions.handleReply,
                error: oscars.Form.handleError,
                form: formNode
            });
        }
        formNode.saveType.value = "";
        choiceType.innerHTML = "";
    }
};

// handles reply from request to server to operate on Institutions table
oscars.Institutions.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject, true)) {
        return;
    }
    var institutionGrid = dijit.byId("institutionGrid");
    var model = institutionGrid.model;
    model.setData(responseObject.institutionData);
    institutionGrid.update();
    institutionGrid.resize();
    institutionGrid.resize();
    oscarsState.institutionGridInitialized = true;
    var formNode = dijit.byId("institutionsForm").domNode;
    formNode.institutionEditName.value = "";
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
    if (institutionGrid && (!oscarsState.institutionGridInitialized)) {
        dojo.connect(institutionGrid, "onRowClick",
                oscars.Institutions.onRowSelect);
        oscars.Institutions.createInstitutionGrid();
    }
};

// create initial institution list from servlet
oscars.Institutions.createInstitutionGrid = function () {
    dojo.xhrPost({
        url: 'servlet/Institutions?op=list',
        handleAs: "json-comment-filtered",
        load: oscars.Institutions.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("institutionsForm").domNode
    });
};

// select name based on row select in grid
oscars.Institutions.onRowSelect = function (/*Event*/ evt) {
    var institutionGrid = dijit.byId("institutionGrid");
    // get institution name
    var institutionName = institutionGrid.model.getDatum(evt.rowIndex, 0);
    var formNode = dijit.byId("institutionsForm").domNode;
    formNode.institutionEditName.value = institutionName;
    formNode.saveType.value = "";
    var choiceType = dojo.byId("institutionOpChoice");
    choiceType.innerHTML = "Selected";
};
