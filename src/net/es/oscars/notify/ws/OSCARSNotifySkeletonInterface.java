
/**
 * OSCARSNotifySkeletonInterface.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */
    package net.es.oscars.notify.ws;
    /**
     *  OSCARSNotifySkeletonInterface java skeleton interface for the axisService
     */
    public interface OSCARSNotifySkeletonInterface {
     
         
        /**
         * Auto generated method signature
         
                                    * @param notify
         */

        
                public void Notify
                (
                  org.oasis_open.docs.wsn.b_2.Notify notify
                 )
            ;
        
         
        /**
         * Auto generated method signature
         
                                    * @param subscribe
         */

        
                public org.oasis_open.docs.wsn.b_2.SubscribeResponse Subscribe
                (
                  org.oasis_open.docs.wsn.b_2.Subscribe subscribe
                 )
            throws TopicNotSupportedFault,InvalidTopicExpressionFault,AAAFaultMessage,UnsupportedPolicyRequestFault,UnacceptableInitialTerminationTimeFault,InvalidMessageContentExpressionFault,InvalidProducerPropertiesExpressionFault,SubscribeCreationFailedFault,TopicExpressionDialectUnknownFault,InvalidFilterFault,NotifyMessageNotSupportedFault,UnrecognizedPolicyRequestFault;
        
         
        /**
         * Auto generated method signature
         
                                    * @param renew
         */

        
                public org.oasis_open.docs.wsn.b_2.RenewResponse Renew
                (
                  org.oasis_open.docs.wsn.b_2.Renew renew
                 )
            throws UnacceptableTerminationTimeFault,AAAFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param unsubscribe
         */

        
                public org.oasis_open.docs.wsn.b_2.UnsubscribeResponse Unsubscribe
                (
                  org.oasis_open.docs.wsn.b_2.Unsubscribe unsubscribe
                 )
            throws AAAFaultMessage,UnableToDestroySubscriptionFault;
        
         
        /**
         * Auto generated method signature
         
                                    * @param pauseSubscription
         */

        
                public org.oasis_open.docs.wsn.b_2.PauseSubscriptionResponse PauseSubscription
                (
                  org.oasis_open.docs.wsn.b_2.PauseSubscription pauseSubscription
                 )
            throws AAAFaultMessage,PauseFailedFault;
        
         
        /**
         * Auto generated method signature
         
                                    * @param resumeSubscription
         */

        
                public org.oasis_open.docs.wsn.b_2.ResumeSubscriptionResponse ResumeSubscription
                (
                  org.oasis_open.docs.wsn.b_2.ResumeSubscription resumeSubscription
                 )
            throws AAAFaultMessage,ResumeFailedFault;
        
         
        /**
         * Auto generated method signature
         
                                    * @param registerPublisher
         */

        
                public org.oasis_open.docs.wsn.br_2.RegisterPublisherResponse RegisterPublisher
                (
                  org.oasis_open.docs.wsn.br_2.RegisterPublisher registerPublisher
                 )
            throws TopicNotSupportedFault,InvalidTopicExpressionFault,PublisherRegistrationFailedFault,UnacceptableInitialTerminationTimeFault,PublisherRegistrationRejectedFault;
        
         
        /**
         * Auto generated method signature
         
                                    * @param destroyRegistration
         */

        
                public org.oasis_open.docs.wsn.br_2.DestroyRegistrationResponse DestroyRegistration
                (
                  org.oasis_open.docs.wsn.br_2.DestroyRegistration destroyRegistration
                 )
            throws ResourceNotDestroyedFault;
        
         }
    