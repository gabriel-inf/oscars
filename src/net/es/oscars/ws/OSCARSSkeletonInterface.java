
/**
 * OSCARSSkeletonInterface.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */
    package net.es.oscars.ws;
    /**
     *  OSCARSSkeletonInterface java skeleton interface for the axisService
     */
    public interface OSCARSSkeletonInterface {
     
         
        /**
         * Auto generated method signature
         * 
                                    * @param cancelReservation
             * @throws BSSFaultMessage : 
             * @throws AAAFaultMessage : 
         */

        
                public net.es.oscars.wsdlTypes.CancelReservationResponse cancelReservation
                (
                  net.es.oscars.wsdlTypes.CancelReservation cancelReservation
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param createReservation
             * @throws BSSFaultMessage : 
             * @throws AAAFaultMessage : 
         */

        
                public net.es.oscars.wsdlTypes.CreateReservationResponse createReservation
                (
                  net.es.oscars.wsdlTypes.CreateReservation createReservation
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param queryReservation
             * @throws BSSFaultMessage : 
             * @throws AAAFaultMessage : 
         */

        
                public net.es.oscars.wsdlTypes.QueryReservationResponse queryReservation
                (
                  net.es.oscars.wsdlTypes.QueryReservation queryReservation
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param refreshPath
             * @throws BSSFaultMessage : 
             * @throws AAAFaultMessage : 
         */

        
                public net.es.oscars.wsdlTypes.RefreshPathResponse refreshPath
                (
                  net.es.oscars.wsdlTypes.RefreshPath refreshPath
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param teardownPath
             * @throws BSSFaultMessage : 
             * @throws AAAFaultMessage : 
         */

        
                public net.es.oscars.wsdlTypes.TeardownPathResponse teardownPath
                (
                  net.es.oscars.wsdlTypes.TeardownPath teardownPath
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param createPath
             * @throws BSSFaultMessage : 
             * @throws AAAFaultMessage : 
         */

        
                public net.es.oscars.wsdlTypes.CreatePathResponse createPath
                (
                  net.es.oscars.wsdlTypes.CreatePath createPath
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param getNetworkTopology
             * @throws BSSFaultMessage : 
             * @throws AAAFaultMessage : 
         */

        
                public net.es.oscars.wsdlTypes.GetNetworkTopologyResponse getNetworkTopology
                (
                  net.es.oscars.wsdlTypes.GetNetworkTopology getNetworkTopology
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param modifyReservation
             * @throws BSSFaultMessage : 
             * @throws AAAFaultMessage : 
         */

        
                public net.es.oscars.wsdlTypes.ModifyReservationResponse modifyReservation
                (
                  net.es.oscars.wsdlTypes.ModifyReservation modifyReservation
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param notify
         */

        
                public void Notify
                (
                  org.oasis_open.docs.wsn.b_2.Notify notify
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param forward
             * @throws BSSFaultMessage : 
             * @throws AAAFaultMessage : 
         */

        
                public net.es.oscars.wsdlTypes.ForwardResponse forward
                (
                  net.es.oscars.wsdlTypes.Forward forward
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param listReservations
             * @throws BSSFaultMessage : 
             * @throws AAAFaultMessage : 
         */

        
                public net.es.oscars.wsdlTypes.ListReservationsResponse listReservations
                (
                  net.es.oscars.wsdlTypes.ListReservations listReservations
                 )
            throws BSSFaultMessage,AAAFaultMessage;
        
         }
    