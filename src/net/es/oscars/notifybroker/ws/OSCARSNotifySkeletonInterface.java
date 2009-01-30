
/**
 * OSCARSNotifySkeletonInterface.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */
    package net.es.oscars.notifybroker.ws;
    /**
     *  OSCARSNotifySkeletonInterface java skeleton interface for the axisService
     */
    public interface OSCARSNotifySkeletonInterface {
     
         
        /**
         * Auto generated method signature
         * 
                                    * @param subscribe
             * @throws InvalidTopicExpressionFault : 
             * @throws AAAFaultMessage : 
             * @throws InvalidMessageContentExpressionFault : 
             * @throws TopicExpressionDialectUnknownFault : 
             * @throws InvalidFilterFault : 
             * @throws ResourceUnknownFault : 
             * @throws NotifyMessageNotSupportedFault : 
             * @throws TopicNotSupportedFault : 
             * @throws UnsupportedPolicyRequestFault : 
             * @throws UnacceptableInitialTerminationTimeFault : 
             * @throws InvalidProducerPropertiesExpressionFault : 
             * @throws SubscribeCreationFailedFault : 
             * @throws UnrecognizedPolicyRequestFault : 
         */

        
                public org.oasis_open.docs.wsn.b_2.SubscribeResponse Subscribe
                (
                  org.oasis_open.docs.wsn.b_2.Subscribe subscribe
                 )
            throws InvalidTopicExpressionFault,AAAFaultMessage,InvalidMessageContentExpressionFault,TopicExpressionDialectUnknownFault,InvalidFilterFault,ResourceUnknownFault,NotifyMessageNotSupportedFault,TopicNotSupportedFault,UnsupportedPolicyRequestFault,UnacceptableInitialTerminationTimeFault,InvalidProducerPropertiesExpressionFault,SubscribeCreationFailedFault,UnrecognizedPolicyRequestFault;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param resumeSubscription
             * @throws AAAFaultMessage : 
             * @throws ResumeFailedFault : 
             * @throws ResourceUnknownFault : 
         */

        
                public org.oasis_open.docs.wsn.b_2.ResumeSubscriptionResponse ResumeSubscription
                (
                  org.oasis_open.docs.wsn.b_2.ResumeSubscription resumeSubscription
                 )
            throws AAAFaultMessage,ResumeFailedFault,ResourceUnknownFault;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param registerPublisher
             * @throws TopicNotSupportedFault : 
             * @throws InvalidTopicExpressionFault : 
             * @throws PublisherRegistrationFailedFault : 
             * @throws UnacceptableInitialTerminationTimeFault : 
             * @throws PublisherRegistrationRejectedFault : 
             * @throws ResourceUnknownFault : 
         */

        
                public org.oasis_open.docs.wsn.br_2.RegisterPublisherResponse RegisterPublisher
                (
                  org.oasis_open.docs.wsn.br_2.RegisterPublisher registerPublisher
                 )
            throws TopicNotSupportedFault,InvalidTopicExpressionFault,PublisherRegistrationFailedFault,UnacceptableInitialTerminationTimeFault,PublisherRegistrationRejectedFault,ResourceUnknownFault;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param unsubscribe
             * @throws AAAFaultMessage : 
             * @throws UnableToDestroySubscriptionFault : 
             * @throws ResourceUnknownFault : 
         */

        
                public org.oasis_open.docs.wsn.b_2.UnsubscribeResponse Unsubscribe
                (
                  org.oasis_open.docs.wsn.b_2.Unsubscribe unsubscribe
                 )
            throws AAAFaultMessage,UnableToDestroySubscriptionFault,ResourceUnknownFault;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param pauseSubscription
             * @throws AAAFaultMessage : 
             * @throws PauseFailedFault : 
             * @throws ResourceUnknownFault : 
         */

        
                public org.oasis_open.docs.wsn.b_2.PauseSubscriptionResponse PauseSubscription
                (
                  org.oasis_open.docs.wsn.b_2.PauseSubscription pauseSubscription
                 )
            throws AAAFaultMessage,PauseFailedFault,ResourceUnknownFault;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param renew
             * @throws UnacceptableTerminationTimeFault : 
             * @throws AAAFaultMessage : 
             * @throws ResourceUnknownFault : 
         */

        
                public org.oasis_open.docs.wsn.b_2.RenewResponse Renew
                (
                  org.oasis_open.docs.wsn.b_2.Renew renew
                 )
            throws UnacceptableTerminationTimeFault,AAAFaultMessage,ResourceUnknownFault;
        
         
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
                                    * @param destroyRegistration
             * @throws ResourceNotDestroyedFault : 
             * @throws AAAFaultMessage : 
             * @throws ResourceUnknownFault : 
         */

        
                public org.oasis_open.docs.wsn.br_2.DestroyRegistrationResponse DestroyRegistration
                (
                  org.oasis_open.docs.wsn.br_2.DestroyRegistration destroyRegistration
                 )
            throws ResourceNotDestroyedFault,AAAFaultMessage,ResourceUnknownFault;
        
         }
    