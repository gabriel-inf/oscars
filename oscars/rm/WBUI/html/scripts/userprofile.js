/*
Javascript functions for main service: my profile interface
Last modified: July 27, 2004
Soo-yeon Hwang (dapi@umich.edu)
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
check_form( form )
*/

// check user input and validate it
function check_form( form ) {

    if ( isblank(form.user_password.value) ) {
        alert( "Please enter the current password." );
        form.user_password.focus();
        return false;
    }

    if ( !(isblank(form.password_new_once.value)) ) {
        if ( form.password_new_once.value != form.password_new_twice.value ) {
            alert( "Please enter the same new password twice for verification." );
            form.password_new_once.focus();
            return false;
        }
        form.update_password.value = 1;
    }

    if ( isblank(form.user_first_name.value) ) {
        alert( "Please enter the first name." );
        form.user_first_name.focus();
        return false;
    }

    if ( isblank(form.user_last_name.value) ) {
        alert( "Please enter the last name." );
        form.user_last_name.focus();
        return false;
    }

    if ( isblank(form.institution.value) ) {
        alert( "Please enter the organization." );
        form.institution.focus();
        return false;
    }

    if ( isblank(form.user_email_primary.value) ) {
        alert( "Please enter the primary e-mail address." );
        form.user_email_primary.focus();
        return false;
    }

    if ( isblank(form.user_phone_primary.value) ) {
        alert( "Please enter the primary phone number." );
        form.user_phone_primary.focus();
        return false;
    }
    return true;
}
