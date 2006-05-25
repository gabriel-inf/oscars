When creating a JnxLSP object, the following parameters can be set:
 - name => string that uniquely identifies a reservation (required)
 - lsp_from => router that will initiate (start-point) LSP (required)
 - lsp_to => router that will terminate LSP (required for setup)
 - bandwidth => LSP bandwidth (e.g. 20, 20k, 20m) (required for setup)
 - lsp_class-of-service => LSP class-of-service (required for setup)
 - lsp_setup-priority => LSP setup priority (optional)
 - lsp_reservation-priority => LSP reservation priority (optional)
 - lsp_description => LSP description (optional)
 - policer_burst-size-limit => LSP burst size limit, typically 10% of
     bandwidth (required for setup)
 - source-address => IP/network of source (e.g. 10.0.0.1, 10.10.10.0/24)
     (required for setup)
 - destination-address => destination IP/network of sink
     (e.g. 10.0.0.1, 10.10.10.0/24) (required for setup)
 - dscp => DSCP value of traffic (optional)
 - protocol => protocol number of traffic (optional)
 - source-port => port number of source traffic (optional)
 - destination-port => port number of destination traffic (optional)
