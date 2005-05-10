/*
Javascript functions for admin tool: user list/update CGI interface
Last modified: August 23, 2004
Soo-yeon Hwang (dapi@umich.edu)
*/

/* List of functions:
confirm_account_delete( form )
confirm_password_reset( form )
*/

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
