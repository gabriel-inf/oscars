# general.pl
#
# library for general cgi script usage
# Last modified: April 15, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

##### Settings Begin (Global variables) #####
# contact info
$webmaster = 'dwrobertson@lbl.gov';

##### Settings End #####


##### List of sub routines #####
### parse CGI form input
# sub Parse_Form_Input_Data

### update status message, and replace main frame if necessary
# sub Update_Frames



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
      #print STDERR '** ' . $Key . ' ' . $Value . "\n";
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


##### sub Update_Frames
##    Updates status portion of display (form target is status frame)
##In:  URI of new frame if necessary, error or status msg
# Result: update status message, sets main frame to another page if success
#####
sub Update_Frames
{
  my ($uri, $msg) = @_;
  #print "Content-type: text/html\n\n";
  print "<html>\n";
  print "<head>\n";
  print "<link rel=\"stylesheet\" type=\"text/css\" ";
  print " href=\"https://oscars.es.net/styleSheets/layout.css\">\n";
  print "<script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>\n";
  print "</head>\n";
  print "<body>\n";
  print "<div>\n";
  print "<p class=\"topmessage\"><script language=\"javascript\">print_current_date(\"local\");</script>" . " | " . $msg . "</p>\n";
  print "</div>\n";
  if ($uri)
  {
      print "<script language=\"javascript\">update_main_frame(\"$uri\");</script>";
  }
  print "</body>\n";
  print "</html>\n";
  print "\n\n";
}



# Don't touch the line below
1;
