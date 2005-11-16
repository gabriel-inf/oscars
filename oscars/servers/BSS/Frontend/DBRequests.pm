package BSS::Frontend::DBRequests;

# General BSS database requests.  Traceroute and scheduler-specific requests
# are in Traceroute::DBRequests and Scheduler::DBRequests.
# Last modified:   November 15, 2005
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

# TODO:  FIX duplication

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
    return $rows;
}
######

###############################################################################
#
sub get_row {
    my( $self, $statement, @args ) = @_;

    my $sth = $self->{dbh}->prepare( $statement );
    if ($DBI::err) {
        throw Error::Simple("[DBERROR] Preparing $statement:  $DBI::errstr");
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        throw Error::Simple("[DBERROR] Executing $statement:  $DBI::errstr");
    }
    my $rows = $sth->fetchall_arrayref({});
    if ( !@$rows ) { return undef; }
    # TODO:  error checking if more than one row
    return $rows->[0];
}
######

###############################################################################
# update_status: Updates reservation status.  Used to mark as active,
# finished, or cancelled.
#
sub update_status {
    my ( $self, $params, $status ) = @_;

    my $statement = qq{ SELECT reservation_status from reservations
                 WHERE reservation_id = ?};
    my $row = $self->get_row($statement, $params->{reservation_id});

    # If the previous state was pending_cancel, mark it now as cancelled.
    # If the previous state was pending, and it is to be deleted, mark it
    # as cancelled instead of pending_cancel.  The latter is used by 
    # find_expired_reservations as one of the conditions to attempt to
    # tear down a circuit.
    my $prev_status = $row->{reservation_status};
    if ( ($prev_status eq 'precancel') || ( ($prev_status eq 'pending') &&
            ($status eq 'precancel'))) { 
        $status = 'cancelled';
    }
    $statement = qq{ UPDATE reservations SET reservation_status = ?
                 WHERE reservation_id = ?};
    my $unused = $self->do_query($statement, $status, $params->{reservation_id});
    return $status;
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
    my $configs = $self->get_row($statement);
    return $configs;
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
    my $row = $self->get_row($statement, $interface_id);
    # no match
    if ( !$row ) {
        # not considered an error
        return "";
    }
    return $row->{router_name};
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

    # TODO:  fix schema, possible hostaddr_ip would not be unique
    my $statement = 'SELECT hostaddr_id FROM hostaddrs WHERE hostaddr_ip = ?';
    my $row = $self->get_row($statement, $ipaddr);
    # if no matches, insert a row in hostaddrs
    if ( !$row ) {
        $statement = "INSERT INTO hostaddrs VALUES ( NULL, '$ipaddr'  )";
        my $unused = $self->do_query($statement);
        return $self->{dbh}->{mysql_insertid};
    }
    else { return $row->{hostaddr_id}; }
}
######

##############################################################################
#
sub get_host_info {
    my( $self, $resv ) = @_;
 
    my $statement = "SELECT hostaddr_ip FROM hostaddrs WHERE hostaddr_id = ?";
    my $hrow = $self->get_row($statement, $resv->{src_hostaddr_id});
    $resv->{source_ip} = $hrow->{hostaddr_ip};
    my $ipaddr = inet_aton($resv->{source_ip});
    $resv->{source_host} = gethostbyaddr($ipaddr, AF_INET);
    if (!$resv->{source_host}) {
        $resv->{source_host} = $resv->{source_ip};
    }

    $hrow = $self->get_row($statement, $resv->{dst_hostaddr_id});
    # TODO:  FIX, hrow might be empty
    $resv->{destination_ip} = $hrow->{hostaddr_ip};
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
    my( $self, $params, $infinite_time) = @_;

    my( $duration_seconds );

    # Expects strings in second since epoch; converts to date in UTC time
    my $statement = "SELECT from_unixtime(?) AS start_time";
    my $row = $self->get_row( $statement, $params->{reservation_start_time});
    $params->{reservation_start_time} = $row->{start_time};
    if ($params->{duration_hour} < (2**31 - 1)) {
        $duration_seconds = $params->{duration_hour} * 3600;
        $statement = "SELECT DATE_ADD(?, INTERVAL ? SECOND) AS end_time";
        $row = $self->get_row( $statement, $params->{reservation_start_time},
                                $duration_seconds );
        $params->{reservation_end_time} = $row->{end_time};
    }
    else {
        $params->{reservation_end_time} = $infinite_time;
    }
    $statement = "SELECT now() AS created_time";
    $row = $self->get_row( $statement );
    $params->{reservation_created_time} = $row->{created_time};
}
######

##############################################################################
#
sub convert_times {
    my( $self, $resv ) = @_;
 
    # convert to time zone reservation was created in
    my $statement = "SELECT CONVERT_TZ(?, '+00:00', ?) AS new_time";
    my $row = $self->get_row( $statement, $resv->{reservation_start_time},
                             $resv->{reservation_time_zone} );
    $resv->{reservation_start_time} = $row->{new_time};
    $row = $self->get_row( $statement, $resv->{reservation_end_time},
                             $resv->{reservation_time_zone} );
    $resv->{reservation_end_time} = $row->{new_time};
    $row = $self->get_row( $statement, $resv->{reservation_created_time},
                             $resv->{reservation_time_zone} );
    $resv->{reservation_created_time} = $row->{new_time};
    return;
}
######

##############################################################################
#
sub get_engr_fields {
    my( $self, $resv ) = @_;
 
    my $statement = "SELECT router_name, router_loopback FROM routers" .
                " WHERE router_id =" .
                  " (SELECT router_id FROM interfaces" .
                  "  WHERE interface_id = ?)";

    # TODO:  FIX row might be empty
    my $row = $self->get_row($statement, $resv->{ingress_interface_id});
    $resv->{ingress_router} = $row->{router_name}; 
    $resv->{ingress_ip} = $row->{router_loopback}; 

    $row = $self->get_row($statement, $resv->{egress_interface_id});
    $resv->{egress_router} = $row->{router_name}; 
    $resv->{egress_ip} = $row->{router_loopback}; 
    my @path_routers = split(' ', $resv->{reservation_path});
    $resv->{reservation_path} = ();
    for $_ (@path_routers) {
        $row = $self->get_row($statement, $_);
        push(@{$resv->{reservation_path}}, $row->{router_name}); 
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
