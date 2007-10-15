
/**
 * OSCARSSkeletonInterface.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3-RC2  Built on : Jul 20, 2007 (04:21:49 LKT)
 */
    package net.es.oscars.oscars;
    /**
     *  OSCARSSkeletonInterface java skeleton interface for the axisService
     */
    public interface OSCARSSkeletonInterface {
     
         
        /**
         * Auto generated method signature
         
                                    * @param createReservation
         */

        
                public net.es.oscars.wsdlTypes.CreateReservationResponse createReservation
                (
                  net.es.oscars.wsdlTypes.CreateReservation createReservation
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param cancelReservation
         */

        
                public net.es.oscars.wsdlTypes.CancelReservationResponse cancelReservation
                (
                  net.es.oscars.wsdlTypes.CancelReservation cancelReservation
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param queryReservation
         */

        
                public net.es.oscars.wsdlTypes.QueryReservationResponse queryReservation
                (
                  net.es.oscars.wsdlTypes.QueryReservation queryReservation
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param listReservations
         */

        
                public net.es.oscars.wsdlTypes.ListReservationsResponse listReservations
                (
                  net.es.oscars.wsdlTypes.ListReservations listReservations
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param getNetworkTopology
         */

        
                public net.es.oscars.wsdlTypes.GetNetworkTopologyResponse getNetworkTopology
                (
                  net.es.oscars.wsdlTypes.GetNetworkTopology getNetworkTopology
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param initiateTopologyPull
         */

        
                public net.es.oscars.wsdlTypes.InitiateTopologyPullResponse initiateTopologyPull
                (
                  net.es.oscars.wsdlTypes.InitiateTopologyPull initiateTopologyPull
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param createPath
         */

        
                public net.es.oscars.wsdlTypes.CreatePathResponse createPath
                (
                  net.es.oscars.wsdlTypes.CreatePath createPath
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param refreshPath
         */

        
                public net.es.oscars.wsdlTypes.RefreshPathResponse refreshPath
                (
                  net.es.oscars.wsdlTypes.RefreshPath refreshPath
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param teardownPath
         */

        
                public net.es.oscars.wsdlTypes.TeardownPathResponse teardownPath
                (
                  net.es.oscars.wsdlTypes.TeardownPath teardownPath
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param forward
         */

        
                public net.es.oscars.wsdlTypes.ForwardResponse forward
                (
                  net.es.oscars.wsdlTypes.Forward forward
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         }
    