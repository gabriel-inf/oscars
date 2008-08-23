/*
Attributes.js:     Handles form for attributes table.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
manage(opName)
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget, oscarsStatus)
createAttributeGrid()
*/

dojo.provide("oscars.Attributes");

oscars.Attributes.manage = function (opName) { 
    var valid;
    var oscarsStatus = dojo.byId("oscarsStatus");
    var formNode = dijit.byId("attributesForm").domNode;
    var choiceType = dojo.byId("attributeOpChoice");
    var editWidget = dijit.byId("attributeEditName");
    var addButton = dijit.byId("attributeAddButton").domNode;
    var saveButton = dijit.byId("attributeSaveButton").domNode;
    var deleteButton = dijit.byId("attributeDeleteButton").domNode;
    if (opName == "add") {
        addButton.style.color = "#FF0000";
        saveButton.style.color = "#00FF00";
        deleteButton.style.color = "#FF0000";
        editWidget.required = false;
        valid = dijit.byId("attributesForm").validate();
        formNode.saveAttrName.value = "";
        formNode.attributeEditName.value = "";
        choiceType.innerHTML = "Adding";
    } else if (opName == "delete") {
        editWidget.required = true;
        valid = dijit.byId("attributesForm").validate();
        if (!valid) {
            return;
        } 
        dojo.xhrPost({
            url: 'servlet/Attributes?op=delete',
            handleAs: "json-comment-filtered",
            load: oscars.Attributes.handleReply,
            error: oscars.Form.handleError,
            form: formNode
        });
        formNode.saveAttrName.value = "";
        choiceType.innerHTML = "";
    } else if (opName == "save") {
        editWidget.required = true;
        valid = dijit.byId("attributesForm").validate();
        if (!valid) {
            return;
        } 
        if (!formNode.saveAttrName.value) {
            dojo.xhrPost({
                url: 'servlet/Attributes?op=add',
                handleAs: "json-comment-filtered",
                load: oscars.Attributes.handleReply,
                error: oscars.Form.handleError,
                form: formNode
            });
        } else {
            dojo.xhrPost({
                url: 'servlet/Attributes?op=modify',
                handleAs: "json-comment-filtered",
                load: oscars.Attributes.handleReply,
                error: oscars.Form.handleError,
                form: formNode
            });
        }
        formNode.saveAttrName.value = "";
        choiceType.innerHTML = "";
    }
};

// handles reply from request to server to operate on Attributes table
oscars.Attributes.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    var attributeGrid = dijit.byId("attributeGrid");
    var model = attributeGrid.model;
    model.setData(responseObject.attributeData);
    attributeGrid.setSortIndex(0, true);
    attributeGrid.sort();
    attributeGrid.update();
    attributeGrid.resize();
    attributeGrid.resize();
    oscarsState.attributeGridInitialized = true;
    var formNode = dijit.byId("attributesForm").domNode;
    formNode.attributeEditName.value = "";
    var addButton = dijit.byId("attributeAddButton").domNode;
    var saveButton = dijit.byId("attributeSaveButton").domNode;
    var deleteButton = dijit.byId("attributeDeleteButton").domNode;
    addButton.style.color = "#000000";
    saveButton.style.color = "#000000";
    deleteButton.style.color = "#000000";
    /*
    if (responseObject.method != "AttributeList") {
        formNode = dijit.byId("userProfileForm").domNode;
        formNode.userInstsUpdated.value = "changed";
        // doesn't exist until user add tab first clicked on
        if (dijit.byId("userAddForm")) {
            formNode = dijit.byId("userAddForm").domNode;
            formNode.userAddInstsUpdated.value = "changed";
        }
    }
    if (responseObject.method == "AttributeModify") {
        var listFormNode = dijit.byId("userListForm").domNode;
        listFormNode.userListInstsUpdated.value = "changed";
    }
    */
};

// take action based on this tab being selected
oscars.Attributes.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {
    oscarsStatus.innerHTML = "Attributes Management";
    var attributeGrid = dijit.byId("attributeGrid");
    if (attributeGrid && (!oscarsState.attributeGridInitialized)) {
        dojo.connect(attributeGrid, "onRowClick",
                oscars.Attributes.onRowSelect);
        oscars.Attributes.createAttributeGrid();
    }
};

// create initial attribute list from servlet
oscars.Attributes.createAttributeGrid = function () {
    dojo.xhrPost({
        url: 'servlet/Attributes?op=list',
        handleAs: "json-comment-filtered",
        load: oscars.Attributes.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("attributesForm").domNode
    });
};

// select name based on row select in grid
oscars.Attributes.onRowSelect = function (/*Event*/ evt) {
    var attributeGrid = dijit.byId("attributeGrid");
    // get attribute name
    var attributeName = attributeGrid.model.getDatum(evt.rowIndex, 0);
    var formNode = dijit.byId("attributesForm").domNode;
    formNode.attributeEditName.value = attributeName;
    formNode.saveAttrName.value = attributeName;
    var choiceType = dojo.byId("attributeOpChoice");
    choiceType.innerHTML = "Selected";
    var addButton = dijit.byId("attributeAddButton").domNode;
    var saveButton = dijit.byId("attributeSaveButton").domNode;
    var deleteButton = dijit.byId("attributeDeleteButton").domNode;
    addButton.style.color = "#FF0000";
    saveButton.style.color = "#00FF00";
    deleteButton.style.color = "#00FF00";
};
