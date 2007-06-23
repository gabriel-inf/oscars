/*
Form.js:        Javascript form handling
Last modified:  May 28, 2007
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
submitForm()
handleReply(type, data, evt)
handleError(type, evt)
*/

dojo.provide("js.Form");

function submitForm() {
    var x = new dojo.io.FormBind({
    formNode: document.forms[0],
    load: handleReply,
    error: handleError });
}


function handleReply(type, data, http) {
    dojo.debug(http["responseText"]);
}


function handleError(type, evt) {
    var msg = evt["message"];
    // The message has non-useful information in it for the user.
    // The servlet inserts a series of *'s in the message so that
    // the non-helpful info can be parsed out.
    var lastIndex = msg.lastIndexOf("*");
    dojo.debug(msg.substring(lastIndex+2, msg.length));
}
