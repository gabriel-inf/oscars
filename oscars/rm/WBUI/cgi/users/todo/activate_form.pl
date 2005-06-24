##### sub process_user_account_activation
# In:  ref to form parameter hash
# Out: status, message
sub process_user_account_activation
{
    my( $form_params ) = @_;
    my( %results );

    # validate user input (just check for empty fields)
    if (!$form_params->{user_dn}) {
        $results{error_msg} = 'Please enter your login name.';
        return( 1, %results );
    }

    if (!$form_params->{activation_key}) {
        $results{error_msg} = 'Please enter the account activation key.';
        return( 1, %results );
    }

    if (!$form_params->{password}) {
        $results{error_msg} = 'Please enter the password.';
        return( 1, %results );
    }

    # start working with the database
    # TODO:  call AAAS, get return info and value

    # when everything has been processed successfully...
    $results{status_msg} = 'The user account <strong>' .
            $form_params->{user_dn} . '</strong> has been successfully' .
            ' activated. You will be redirected to the main service login' .
            ' page in 10 seconds.<br>Please change the password to your own' .
            ' once you sign in.';
    return( 0, %results );
}

1;
