/*
Javascript functions for new user registration CGI interface
Last modified: July 13, 2004
Soo-yeon Hwang (dapi@umich.edu)
*/

/* List of functions:
login_name_overlap_check( formObject )
check_form( form )
*/

// check overlapping login name
function login_name_overlap_check( formObject )
{
	var loginnameRegexp = /\W|\s/;

	if ( formObject.value == "" )
	{
		alert( "Please enter your desired login name." );
		formObject.focus();
		return false;
	}
	else if ( loginnameRegexp.test( formObject.value ) )
	{
		alert( "Please use only alphanumeric characters or _ for login name." );
		formObject.focus();
		return false;
	}
	else
	{
		var url = "form_user_idcheck.cgi?mode=idcheck&id=" + formObject.value;
 		window.open( url,"useridcheck","height=200,width=450,scrollbars=no" );
	}
}

// check user input and validate it
function check_form( form )
{
	if ( form.loginname.value == "" )
	{
		alert( "Please enter your desired login name." );
		form.loginname.focus();
		return false;
	}

	var loginnameRegexp = /\W|\s/;

	if ( loginnameRegexp.test( form.loginname.value ) )
	{
		alert( "Please use only alphanumeric characters or _ for login name." );
		form.loginname.focus();
		return false;
	}

	if ( form.password_once.value == "" )
	{
		alert( "Please enter the password." );
		form.password_once.focus();
		return false;
	}

	if ( form.password_twice.value == "" )
	{
		alert( "Please enter the password." );
		form.password_twice.focus();
		return false;
	}

	if ( form.password_once.value != form.password_twice.value )
	{
		alert( "Please enter the same password twice for verification." );
		form.password_once.focus();
		return false;
	}

	if ( form.firstname.value == "" )
	{
		alert( "Please enter your first name." );
		form.firstname.focus();
		return false;
	}

	if ( form.lastname.value == "" )
	{
		alert( "Please enter your last name." );
		form.lastname.focus();
		return false;
	}

	if ( form.organization.value == "" )
	{
		alert( "Please enter your organization." );
		form.organization.focus();
		return false;
	}

	if ( form.email_primary.value == "" )
	{
		alert( "Please enter your primary e-mail address." );
		form.email_primary.focus();
		return false;
	}

	if ( form.phone_primary.value == "" )
	{
		alert( "Please enter your primary phone number." );
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
