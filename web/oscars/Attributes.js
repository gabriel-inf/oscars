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
    var editNameWidget = dijit.byId("attributeEditName");
    var editDescrWidget = dijit.byId("attributeEditDescription");
    var addButton = dijit.byId("attributeAddButton").domNode;
    var saveButton = dijit.byId("attributeSaveButton").domNode;
    var deleteButton = dijit.byId("attributeDeleteButton").domNode;
    if (opName == "add") {
        addButton.style.color = "#FF0000";
        saveButton.style.color = "#00FF00";
        deleteButton.style.color = "#FF0000";
        editNameWidget.required = false;
        editDescrWidget.required = false;
        valid = dijit.byId("attributesForm").validate();
        formNode.saveAttrName.value = "";
        formNode.saveAttrDescription.value = "";
        formNode.attributeEditName.value = "";
        formNode.attributeEditDescription.value = "";
        choiceType.innerHTML = "Adding";
    } else if (opName == "delete") {
        editNameWidget.required = true;
        editDescrWidget.required = true;
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
        formNode.saveAttrDescription.value = "";
        choiceType.innerHTML = "";
    } else if (opName == "save") {
        editNameWidget.required = true;
        editDescrWidget.required = true;
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
        formNode.saveAttrDescriptionValue = "";
        choiceType.innerHTML = "";
    }
};

// handles reply from request to server to operate on Attributes table
oscars.Attributes.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    var formNode;
    var attributeGrid = dijit.byId("attributeGrid");
    var model = attributeGrid.model;
    model.setData(responseObject.attributeData);
    attributeGrid.setSortIndex(0, true);
    attributeGrid.sort();
    attributeGrid.update();
    attributeGrid.resize();
    attributeGrid.resize();
    oscarsState.attributeGridInitialized = true;
    var attrFormNode = dijit.byId("attributesForm").domNode;
    var addButton = dijit.byId("attributeAddButton").domNode;
    var saveButton = dijit.byId("attributeSaveButton").domNode;
    var deleteButton = dijit.byId("attributeDeleteButton").domNode;
    addButton.style.color = "#000000";
    saveButton.style.color = "#000000";
    deleteButton.style.color = "#000000";
    // indicate to dependent pages that they need to be updated
    if (responseObject.method != "AttributeList") {
        formNode = dijit.byId("userProfileForm").domNode;
        formNode.userAttrsUpdated.value = "changed";
        // doesn't exist until user add tab first clicked on
        if (dijit.byId("userAddForm")) {
            formNode = dijit.byId("userAddForm").domNode;
            formNode.userAddAttrsUpdated.value = "changed";
        }
        if ((responseObject.method != "AttributeModify") ||
            (attrFormNode.attributeEditName.value !=
             attrFormNode.saveAttrName.value)) {
             if (dijit.byId("authDetailsForm")) {
                 formNode = dijit.byId("authDetailsForm").domNode;
                 formNode.authAttrsUpdatedValue = "changed";
             }
        }
    }
    attrFormNode.attributeEditName.value = "";
    attrFormNode.attributeEditDescription.value = "";
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
    // get attribute
    var attributeName = attributeGrid.model.getDatum(evt.rowIndex, 0);
    var attributeDescription = attributeGrid.model.getDatum(evt.rowIndex, 1);
    var formNode = dijit.byId("attributesForm").domNode;
    formNode.attributeEditName.value = attributeName;
    formNode.attributeEditDescription.value = attributeDescription;
    formNode.saveAttrName.value = attributeName;
    formNode.saveAttrDescription.value = attributeDescription;
    var choiceType = dojo.byId("attributeOpChoice");
    choiceType.innerHTML = "Selected";
    var addButton = dijit.byId("attributeAddButton").domNode;
    var saveButton = dijit.byId("attributeSaveButton").domNode;
    var deleteButton = dijit.byId("attributeDeleteButton").domNode;
    addButton.style.color = "#FF0000";
    saveButton.style.color = "#00FF00";
    deleteButton.style.color = "#00FF00";
};
