package BSS::Frontend::DBRequests;

# DBRequests.pm:   package for BSS database request handling
# Last modified:   November 12, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;
use Data::Dumper;
use Error qw(:try);
use Socket;

###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ( $self ) = @_;

    my ( %attr ) = (
        RaiseError => 0,
        PrintError => 0,
    );
    # I couldn't find a foolproof way to check for timeout; Apache::DBI
    # came closest, but it was too dependent on the driver handling the timeout
    # correctly.  So instead,
    # if a handle is left over from a previous session, attempts to disconnect.
    # If it was timed out, the error is ignored.
    # TODO:  FIX disconnect every time
    if ($self->{dbh}) {
        $self->{dbh}->disconnect();
    }
    $self->{dbh} = DBI->connect(
                 $self->{database}, 
                 $self->{dblogin}, 
                 $self->{password},
                 \%attr);
    if (!$self->{dbh}) {
        throw Error::Simple( "Unable to make database connection: $DBI::errstr");
    }
    return;
}
######

###############################################################################
#
sub do_query {
    my( $self, $statement, @args ) = @_;

    # TODO, FIX:  selectall_arrayref probably better
    my $sth = $self->{dbh}->prepare( $statement );
    if ($DBI::err) {
        throw Error::Simple("[DBERROR] Preparing $statement:  $DBI::errstr");
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        throw Error::Simple("[DBERROR] Executing $statement:  $DBI::errstr");
    }
    my $rows = $sth->fetchall_arrayref({});
    # TODO, FIX:  if check for err here, get fetch without execute if not
    # select
    return( $rows );
}
######

###############################################################################
# update_status: Updates reservation status.  Used to mark as active,
# finished, or cancelled.
#
sub update_status {
    my ( $self, $inref, $status ) = @_;

    my $statement = qq{ SELECT reservation_status from reservations
                 WHERE reservation_id = ?};
    my $rows = $self->do_query($statement, $inref->{reservation_id});

    # If the previous state was pending_cancel, mark it now as cancelled.
    # If the previous state was pending, and it is to be deleted, mark it
    # as cancelled instead of pending_cancel.  The latter is used by 
    # find_expired_reservations as one of the conditions to attempt to
    # tear down a circuit.
    my $prev_status = $rows->[0]->{reservation_status};
    if ( ($prev_status eq 'precancel') || ( ($prev_status eq 'pending') &&
            ($status eq 'precancel'))) { 
        $status = 'cancelled';
    }
    $statement = qq{ UPDATE reservations SET reservation_status = ?
                 WHERE reservation_id = ?};
    my $unused = $self->do_query($statement, $status, $inref->{reservation_id});
    return( $status );
}
######

##############################################################################
#
sub get_pss_configs {
    my( $self ) = @_;

        # use defaults for now
    my $statement = "SELECT " .
             "pss_conf_access, pss_conf_login, pss_conf_passwd, " .
             "pss_conf_firewall_marker, " .
             "pss_conf_setup_file, pss_conf_teardown_file, " .
             "pss_conf_ext_if_filter, pss_conf_CoS, " .
             "pss_conf_burst_limit, " .
             "pss_conf_setup_priority, pss_conf_resv_priority, " .
             "pss_conf_allow_lsp "  .
             "FROM pss_confs where pss_conf_id = 1";
    my $configs = $self->do_query($statement);
    return( $configs );
}
######

##############################################################################
# id_to_router_name:  get the router name given the interface primary key.
# In:  interface table key id
# Out: router name
#
sub id_to_router_name {
    my( $self, $interface_id ) = @_;

    my $statement = "SELECT router_name FROM routers
                 WHERE router_id = (SELECT router_id from interfaces
                                    WHERE interface_id = ?)";
    my $rows = $self->do_query($statement, $interface_id);
    # no match
    if ( !@$rows ) {
        # not considered an error
        return ("");
    }
    return ($rows->[0]->{router_name}, "");
}
######

##############################################################################
# hostaddrs_ip_to_id:  get the primary key in the hostaddrs table, given an
#     IP address.  A row is created if that address is not present.
# In:  hostaddr_ip
# Out: hostaddr_id
#
sub hostaddrs_ip_to_id {
    my( $self, $ipaddr ) = @_;
    my( $id );

    my $statement = 'SELECT hostaddr_id FROM hostaddrs WHERE hostaddr_ip = ?';
    my $rows = $self->do_query($statement, $ipaddr);
    # if no matches, insert a row in hostaddrs
    if ( !@$rows ) {
        $statement = "INSERT INTO hostaddrs VALUES ( NULL, '$ipaddr'  )";
        my $unused = $self->do_query($statement);
        $id = $self->{dbh}->{mysql_insertid};
    }
    else {
        $id = $rows->[0]->{hostaddr_id};
    }
    return( $id );
}
######

##############################################################################
#
sub get_host_info {
    my( $self, $resv ) = @_;
 
    my( $ipaddr, $hrows );

    my $statement = "SELECT hostaddr_ip FROM hostaddrs WHERE hostaddr_id = ?";
    my $hrows = $self->do_query($statement, $resv->{src_hostaddr_id});
    $resv->{source_ip} = $hrows->[0]->{hostaddr_ip};
    $ipaddr = inet_aton($resv->{source_ip});
    $resv->{source_host} = gethostbyaddr($ipaddr, AF_INET);
    if (!$resv->{source_host}) {
        $resv->{source_host} = $resv->{source_ip};
    }

    $hrows = $self->do_query($statement, $resv->{dst_hostaddr_id});
    $resv->{destination_ip} = $hrows->[0]->{hostaddr_ip};
    $ipaddr = inet_aton($resv->{destination_ip});
    $resv->{destination_host} = gethostbyaddr($ipaddr, AF_INET);
    if (!$resv->{destination_host}) {
        $resv->{destination_host} = $resv->{destination_ip};
    }
    return;
}
######

###############################################################################
# setup_times:  
#
sub setup_times {
    my( $self, $inref, $infinite_time) = @_;

    my( $duration_seconds );

    # Expects strings in second since epoch; converts to date in UTC time
    my $statement = "SELECT from_unixtime(?) AS start_time";
    my $rows = $self->do_query( $statement, $inref->{reservation_start_time});
    $inref->{reservation_start_time} = $rows->[0]->{start_time};
    if ($inref->{duration_hour} < (2**31 - 1)) {
        $duration_seconds = $inref->{duration_hour} * 3600;
        $statement = "SELECT DATE_ADD(?, INTERVAL ? SECOND) AS end_time";
        $rows = $self->do_query( $statement, $inref->{reservation_start_time},
                                $duration_seconds );
        $inref->{reservation_end_time} = $rows->[0]->{end_time};
    }
    else {
        $inref->{reservation_end_time} = $infinite_time;
    }
    $statement = "SELECT now() AS created_time";
    $rows = $self->do_query( $statement );
    $inref->{reservation_created_time} = $rows->[0]->{created_time};
}
######

##############################################################################
#
sub convert_times {
    my( $self, $resv ) = @_;
 
    # convert to time zone reservation was created in
    my $statement = "SELECT CONVERT_TZ(?, '+00:00', ?) AS new_time";
    my $rows = $self->do_query( $statement, $resv->{reservation_start_time},
                             $resv->{reservation_time_zone} );
    $resv->{reservation_start_time} = $rows->[0]->{new_time};
    $rows = $self->do_query( $statement, $resv->{reservation_end_time},
                             $resv->{reservation_time_zone} );
    $resv->{reservation_end_time} = $rows->[0]->{new_time};
    $rows = $self->do_query( $statement, $resv->{reservation_created_time},
                             $resv->{reservation_time_zone} );
    $resv->{reservation_created_time} = $rows->[0]->{new_time};
    return;
}
######

##############################################################################
#
sub get_engr_fields {
    my( $self, $resv ) = @_;
 
    my( $rows, @path_routers );

    my $statement = "SELECT router_name, router_loopback FROM routers" .
                " WHERE router_id =" .
                  " (SELECT router_id FROM interfaces" .
                  "  WHERE interface_id = ?)";

    $rows = $self->do_query($statement, $resv->{ingress_interface_id});
    $resv->{ingress_router} = $rows->[0]->{router_name}; 
    $resv->{ingress_ip} = $rows->[0]->{router_loopback}; 

    $rows = $self->do_query($statement, $resv->{egress_interface_id});
    $resv->{egress_router} = $rows->[0]->{router_name}; 
    $resv->{egress_ip} = $rows->[0]->{router_loopback}; 
    @path_routers = split(' ', $resv->{reservation_path});
    $resv->{reservation_path} = ();
    for $_ (@path_routers) {
        $rows = $self->do_query($statement, $_);
        push(@{$resv->{reservation_path}}, $rows->[0]->{router_name}); 
    }
    return;
}
######

sub get_primary_id {
    my( $self ) = @_;

    return $self->{dbh}->{mysql_insertid};
}
######
1;
# vim: et ts=4 sw=4
