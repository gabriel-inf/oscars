/*
Javascript functions for main service: my profile interface
Last modified: October 16, 2004
Soo-yeon Hwang (dapi@umich.edu)
*/

/* List of functions:
check_form( form )
*/

// check user input and validate it
function check_form( form )
{

	if ( form.password_current.value == "" )
	{
		alert( "Please enter the current password." );
		form.password_current.focus();
		return false;
	}

	if ( form.password_new_once.value != form.password_new_twice.value )
	{
		alert( "Please enter the same new password twice for verification." );
		form.password_new_once.focus();
		return false;
	}

	if ( form.firstname.value == "" )
	{
		alert( "Please enter the first name." );
		form.firstname.focus();
		return false;
	}

	if ( form.lastname.value == "" )
	{
		alert( "Please enter the last name." );
		form.lastname.focus();
		return false;
	}

	if ( form.organization.value == "" )
	{
		alert( "Please enter the organization." );
		form.organization.focus();
		return false;
	}

	if ( form.email_primary.value == "" )
	{
		alert( "Please enter the primary e-mail address." );
		form.email_primary.focus();
		return false;
	}

	if ( form.phone_primary.value == "" )
	{
		alert( "Please enter the primary phone number." );
		form.phone_primary.focus();
		return false;
	}

	// if every check passes...
	// change the submit button's lable, and disable the submit and reset buttons
	if ( document.all || document.getElementById )
	{
		for (i = 0; i < form.length; i++)
		{
			var tempObj = form.elements[i];

			if ( tempObj.type.toLowerCase() == "submit" )
			{
				tempObj.value = "  Processing...  ";
			}

			if ( tempObj.type.toLowerCase() == "submit" || tempObj.type.toLowerCase() == "reset" )
			{
				tempObj.disabled = true;
			}
		}
	}

	return true;
}
