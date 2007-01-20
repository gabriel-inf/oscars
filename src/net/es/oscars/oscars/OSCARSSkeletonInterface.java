
    /**
     * OSCARSSkeletonInterface.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: 1.1.1-SNAPSHOT Nov 29, 2006 (02:53:00 GMT+00:00)
     */
    package net.es.oscars.oscars;
    /**
     *  OSCARSSkeletonInterface java skeleton interface for the axisService
     */
    public interface OSCARSSkeletonInterface {
     
         
        /**
         * Auto generated method signature
         */
        public  net.es.oscars.wsdlTypes.CancelReservationResponse cancelReservation
        (
          net.es.oscars.wsdlTypes.CancelReservation param0     
         )
         
           throws BSSFaultMessageException,AAAFaultMessageException;
     
         
        /**
         * Auto generated method signature
         */
        public  net.es.oscars.wsdlTypes.ForwardResponse forward
        (
          net.es.oscars.wsdlTypes.Forward param2     
         )
         
           throws BSSFaultMessageException,AAAFaultMessageException;
     
         
        /**
         * Auto generated method signature
         */
        public  net.es.oscars.wsdlTypes.ListReservationsResponse listReservations
        (
          net.es.oscars.wsdlTypes.ListReservations param4     
         )
         
           throws BSSFaultMessageException,AAAFaultMessageException;
     
         
        /**
         * Auto generated method signature
         */
        public  net.es.oscars.wsdlTypes.CreateReservationResponse createReservation
        (
          net.es.oscars.wsdlTypes.CreateReservation param6     
         )
         
           throws BSSFaultMessageException,AAAFaultMessageException;
     
         
        /**
         * Auto generated method signature
         */
        public  net.es.oscars.wsdlTypes.QueryReservationResponse queryReservation
        (
          net.es.oscars.wsdlTypes.QueryReservation param8     
         )
         
           throws BSSFaultMessageException,AAAFaultMessageException;
     

         }
    