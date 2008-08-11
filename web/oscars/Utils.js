/*
Utils.js:       Miscellaneous utilities for browser interface.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
isBlank(str)
*/

dojo.provide("oscars.Utils");

// From Javascript book, p. 264

// check to see if no parameter set
oscars.Utils.isBlank = function (str) {
    if (!str) {
        return true;
    }
    for (var i = 0; i < str.length; i++) {
        var c = str.charAt(i);
        if ((c != ' ') && (c != '\n') && c) { return false; }
    }
    return true;
};
