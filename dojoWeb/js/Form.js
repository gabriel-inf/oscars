/*
Form.js:        Javascript form callback handling
Last modified:  December 13, 2007
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
handleReply(data, ioArgs)
handleError(data, ioArgs)
*/

dojo.provide("js.Form");

js.Form.handleReply = function (data, ioArgs) {
    console.log(data);
}

js.Form.handleError = function (data, ioArgs) {
    console.error("error");
}
