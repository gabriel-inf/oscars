#

# general.pl
#
# library for general cgi script usage
# Last modified: April 15, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

##### Settings Begin (Global variables) #####
# contact info
$webmaster = 'dwrobertson@lbl.gov';
# password salt
$psalt = 'oscars';

##### Settings End #####


##### List of sub routines #####
### parse CGI form input
# sub Parse_Form_Input_Data

### format/obtain time string 
# sub Create_Time_String
# sub Create_Time_Sprintf_String
# sub Get_Offset_Adjusted_Time
# sub Calc_Local_Timezone_Offset
# sub Format_Offset_from_Seconds

### password encryption & random string generation (activation key, password reset (future), ...)
# sub Encode_Passwd
# sub Generate_Random_String

### update status message, and replace main frame if necessary
# sub Update_Frames

# sub Error_Code_To_Error_Message



##### sub Parse_Form_Input_Data
# In: $Form_Method [all/post/get]
# Out: %Form_Data (name-value pairs)
#####
sub Parse_Form_Input_Data
{

  my $Form_Method = $_[0];
	
  my( $Form_Info, @Key_Value_Pairs, $Key, $Value, %Form_Data );

  if ( $Form_Method eq 'all' ) {
      if ( $ENV{'REQUEST_METHOD'} =~ /^POST$/i ) {
          read( STDIN, $Form_Info, $ENV{'CONTENT_LENGTH'} );
      }
      elsif ( $ENV{'REQUEST_METHOD'} =~ /^GET$/i ) {
          $Form_Info = $ENV{'QUERY_STRING'}; 
      }
  }
  elsif ( $Form_Method eq 'post' ) {
      read( STDIN, $Form_Info, $ENV{'CONTENT_LENGTH'} );
  }
  elsif ( $Form_Method eq 'get' ) {
      $Form_Info = $ENV{'QUERY_STRING'}; 
  }

  @Key_Value_Pairs = split( /&/, $Form_Info );

  foreach $_ ( @Key_Value_Pairs ) {
      ( $Key, $Value ) = split( /=/, $_ );
      $Value =~ tr/+/ /;
      $Value =~ s/%([\dA-Fa-f][\dA-Fa-f])/pack( "C", hex( $1 ) )/eg;

      foreach ( $Value ) {
          s/\r|\n//g;	# removes line feeds
          s/^\s+|\s+$//g;	# removes leading/trailing spaces
          s/&/&amp;/g;
          s/&amp;\#([0-9]+);/&\#$1;/g;	# takes care of non-latin characters
          s/</&lt;/g;
          s/>/&gt;/g;
          s/"/&quot;/g;
      }

      $Form_Data{$Key} = $Value;
  }

  return %Form_Data;
} 


##### sub Encode_Passwd
# In: $Raw_Password (plain text)
# Out: $Crypted_Password
#####
sub Encode_Passwd
{

  my $Raw_Pwd = $_[0];
  my( $Crypted_Pwd );
 
  $Crypted_Pwd = crypt( $Raw_Pwd, $psalt );
  return $Crypted_Pwd;
}


##### sub Generate_Random_String
# In: $String_Length
# Out: $Random_String
# This sub routine takes care of generating a random string for all functions
#####
sub Generate_Random_String
{

  my $String_Length = $_[0] + 0;	# make it a numeric value

  my @Alphanumeric = ('a'..'z', 'A'..'Z', 0..9);
  my $Random_String = join( '', map( $Alphanumeric[ rand @Alphanumeric ], 0 .. $String_Length ) );

  return $Random_String;
}


##### sub Update_Frames
##    Updates status portion of display (form target is status frame)
##    and replaces main_frame if necessary
# In: $uri, "$Err_Message (to be referenced by &Error_Code_To_Error_Message)\n$Errno (optional; $! in usual)"
# Result: update status message, sets main frame to another page if success
#####
sub Update_Frames
{
  my ($uri, $Err_Message) = @_;
  print "Content-type: text/html\n\n";
  print "<html>";
  print "<head>";
  print "<link rel=stylesheet type=\"text/css\" ";
  print " href=\"https://oscars.es.net/styleSheets/layout.css\">";
  print "<script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>";
  print "</head>";
  print "<body>";
  print "<div>";
  print "<p class=\"topmessage\"><script language=\"javascript\">print_current_date(\"local\");</script>" . " | " . $Err_Message . "</p>";
  print "</div>";
  if ($uri)
  {
      print "<?php session_start(); $_SESSION['valid_user'] = 'foo'; ?>";
      print "<script language=\"javascript\">update_main_frame(\"$uri\");</script>";
  }
  print "</body>";
  print "</html>";
  print "\n\n";
}



##### sub Error_Code_To_Error_Message
# Internal to this script.
# In: $Error_Code
# Out: $Err_Message
#####
sub Error_Code_To_Error_Message
{

#  my $errmsglang = 'en';

  my %Error_Msg_Table_EN = (
      FileOpen => "Cannot open file. Please check if the file location is right, the file permission is properly set, or the file is not corrupted.",
      Locked => "The process is locked by other preceding processes. Please try again later. If you keep seeing this message, there might be an error in the lock setting, or the file used for locking might be corrupted; please inform the webmaster of the problem.",
      CantLock => "Cannot perform file lock. If you keep seeing this message, there might be an error in the lock setting or on the server space; please inform the webmaster of the problem.",
      CantConnectDB => "Cannot connect to the database.",
      CantPrepareStatement => "Cannot prepare database statement.",
      CantExecuteQuery => "Cannot execute query in the database.",
      CantOpenSendmail => "Cannot run Sendmail.",
  );

#  my %Error_Msg_Set = (
#      en => { %Error_Msg_Table_EN }
#  );

#  return $Error_Msg_Set{$errmsglang}{$_[0]};

  return $Error_Msg_Table_EN{$_[0]};
} 


# Don't touch the line below
1;
