/*
Javascript functions for admin tool: user list/update CGI interface
Last modified: August 23, 2004
Soo-yeon Hwang (dapi@umich.edu)
*/

/* List of functions:
check_update_form( form )
confirm_account_delete( form )
confirm_password_reset( form )
*/

// check user input and validate it
function check_update_form( form )
{
	if ( form.firstname.value == "" )
	{
		alert( "Please enter the user's first name." );
		form.firstname.focus();
		return false;
	}

	if ( form.lastname.value == "" )
	{
		alert( "Please enter the user's last name." );
		form.lastname.focus();
		return false;
	}

	if ( form.organization.value == "" )
	{
		alert( "Please enter the user's organization." );
		form.organization.focus();
		return false;
	}

	if ( form.email_primary.value == "" )
	{
		alert( "Please enter the user's primary e-mail address." );
		form.email_primary.focus();
		return false;
	}

	if ( form.phone_primary.value == "" )
	{
		alert( "Please enter the user's primary phone number." );
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

// confirm account deletion
function confirm_account_delete( form )
{
	if ( confirm( 'Do you really want to delete this user account? The deletion cannot be undone.' ) )
	{
		// if confirmed, change the submit button's lable and disable the button
		if ( document.all || document.getElementById )
		{
			for (i = 0; i < form.length; i++)
			{
				var tempObj = form.elements[i];

				if ( tempObj.type.toLowerCase() == "submit" )
				{
					tempObj.value = "  Processing...  ";
				}

				if ( tempObj.type.toLowerCase() == "submit" )
				{
					tempObj.disabled = true;
				}
			}
		}
	}
	else
	{
		// if "no" is clicked
		return false;
	}

	return true;
}

// confirm password reset
function confirm_password_reset( form )
{
	if ( form.password_new.value == "" )
	{
		alert( "Please enter the password to reset to." );
		form.password_new.focus();
		return false;
	}

	if ( confirm( 'Do you really want to reset this user\'s password to "' + form.password_new.value + '"?' ) )
	{
		// if confirmed, change the submit button's lable and disable the button
		if ( document.all || document.getElementById )
		{
			for (i = 0; i < form.length; i++)
			{
				var tempObj = form.elements[i];

				if ( tempObj.type.toLowerCase() == "submit" )
				{
					tempObj.value = "  Processing...  ";
				}

				if ( tempObj.type.toLowerCase() == "submit" )
				{
					tempObj.disabled = true;
				}
			}
		}
	}
	else
	{
		// if "no" is clicked
		return false;
	}

	return true;
}
