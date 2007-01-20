

        /**
        * OSCARSMessageReceiverInOut.java
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2 version: 1.1.1-SNAPSHOT Nov 29, 2006 (02:53:00 GMT+00:00)
        */
        package net.es.oscars.oscars;

        /**
        *  OSCARSMessageReceiverInOut message receiver
        */

        public class OSCARSMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        OSCARSSkeletonInterface skel = (OSCARSSkeletonInterface)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if(op.getName() != null & (methodName = op.getName().getLocalPart()) != null){

        

            if("cancelReservation".equals(methodName)){

            
            net.es.oscars.wsdlTypes.CancelReservationResponse param21 = null;
                    
                            //doc style
                            net.es.oscars.wsdlTypes.CancelReservation wrappedParam =
                                                 (net.es.oscars.wsdlTypes.CancelReservation)fromOM(
                        msgContext.getEnvelope().getBody().getFirstElement(),
                        net.es.oscars.wsdlTypes.CancelReservation.class,
                        getEnvelopeNamespaces(msgContext.getEnvelope()));
                                    
                                   param21 =
                                             skel.cancelReservation(wrappedParam) ;
                                        
                                    envelope = toEnvelope(getSOAPFactory(msgContext), param21, false);
                                

            }
        

            if("forward".equals(methodName)){

            
            net.es.oscars.wsdlTypes.ForwardResponse param23 = null;
                    
                            //doc style
                            net.es.oscars.wsdlTypes.Forward wrappedParam =
                                                 (net.es.oscars.wsdlTypes.Forward)fromOM(
                        msgContext.getEnvelope().getBody().getFirstElement(),
                        net.es.oscars.wsdlTypes.Forward.class,
                        getEnvelopeNamespaces(msgContext.getEnvelope()));
                                    
                                   param23 =
                                             skel.forward(wrappedParam) ;
                                        
                                    envelope = toEnvelope(getSOAPFactory(msgContext), param23, false);
                                

            }
        

            if("listReservations".equals(methodName)){

            
            net.es.oscars.wsdlTypes.ListReservationsResponse param25 = null;
                    
                            //doc style
                            net.es.oscars.wsdlTypes.ListReservations wrappedParam =
                                                 (net.es.oscars.wsdlTypes.ListReservations)fromOM(
                        msgContext.getEnvelope().getBody().getFirstElement(),
                        net.es.oscars.wsdlTypes.ListReservations.class,
                        getEnvelopeNamespaces(msgContext.getEnvelope()));
                                    
                                   param25 =
                                             skel.listReservations(wrappedParam) ;
                                        
                                    envelope = toEnvelope(getSOAPFactory(msgContext), param25, false);
                                

            }
        

            if("createReservation".equals(methodName)){

            
            net.es.oscars.wsdlTypes.CreateReservationResponse param27 = null;
                    
                            //doc style
                            net.es.oscars.wsdlTypes.CreateReservation wrappedParam =
                                                 (net.es.oscars.wsdlTypes.CreateReservation)fromOM(
                        msgContext.getEnvelope().getBody().getFirstElement(),
                        net.es.oscars.wsdlTypes.CreateReservation.class,
                        getEnvelopeNamespaces(msgContext.getEnvelope()));
                                    
                                   param27 =
                                             skel.createReservation(wrappedParam) ;
                                        
                                    envelope = toEnvelope(getSOAPFactory(msgContext), param27, false);
                                

            }
        

            if("queryReservation".equals(methodName)){

            
            net.es.oscars.wsdlTypes.QueryReservationResponse param29 = null;
                    
                            //doc style
                            net.es.oscars.wsdlTypes.QueryReservation wrappedParam =
                                                 (net.es.oscars.wsdlTypes.QueryReservation)fromOM(
                        msgContext.getEnvelope().getBody().getFirstElement(),
                        net.es.oscars.wsdlTypes.QueryReservation.class,
                        getEnvelopeNamespaces(msgContext.getEnvelope()));
                                    
                                   param29 =
                                             skel.queryReservation(wrappedParam) ;
                                        
                                    envelope = toEnvelope(getSOAPFactory(msgContext), param29, false);
                                

            }
        

        newMsgContext.setEnvelope(envelope);
        }
        }catch (BSSFaultMessageException e) {

            org.apache.axis2.AxisFault f = createAxisFault(e);

            f.setDetail(toOM(e.getFaultMessage(),false));

            throw f;
            }
        catch (AAAFaultMessageException e) {

            org.apache.axis2.AxisFault f = createAxisFault(e);

            f.setDetail(toOM(e.getFaultMessage(),false));

            throw f;
            }
        
        catch (Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }
        
        //
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.CancelReservation param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.CancelReservation.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.CancelReservationResponse param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.CancelReservationResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.BSSFault param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.BSSFault.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.AAAFault param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.AAAFault.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.Forward param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.Forward.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.ForwardResponse param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.ForwardResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.ListReservations param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.ListReservations.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.ListReservationsResponse param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.ListReservationsResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.CreateReservation param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.CreateReservation.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.CreateReservationResponse param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.CreateReservationResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.QueryReservation param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.QueryReservation.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.QueryReservationResponse param, boolean optimizeContent){
            
                     return param.getOMElement(net.es.oscars.wsdlTypes.QueryReservationResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.CancelReservationResponse param, boolean optimizeContent){
                      org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                       
                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.CancelReservationResponse.MY_QNAME,factory));
                            

                     return emptyEnvelope;
                    }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.ForwardResponse param, boolean optimizeContent){
                      org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                       
                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.ForwardResponse.MY_QNAME,factory));
                            

                     return emptyEnvelope;
                    }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.ListReservationsResponse param, boolean optimizeContent){
                      org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                       
                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.ListReservationsResponse.MY_QNAME,factory));
                            

                     return emptyEnvelope;
                    }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.CreateReservationResponse param, boolean optimizeContent){
                      org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                       
                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.CreateReservationResponse.MY_QNAME,factory));
                            

                     return emptyEnvelope;
                    }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.QueryReservationResponse param, boolean optimizeContent){
                      org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                       
                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.QueryReservationResponse.MY_QNAME,factory));
                            

                     return emptyEnvelope;
                    }
                    


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces){

        try {
        
                if (net.es.oscars.wsdlTypes.CancelReservation.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.CancelReservation.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.CancelReservationResponse.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.CancelReservationResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.BSSFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.BSSFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.AAAFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.AAAFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.Forward.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.Forward.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.ForwardResponse.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.ForwardResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.BSSFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.BSSFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.AAAFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.AAAFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.ListReservations.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.ListReservations.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.ListReservationsResponse.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.ListReservationsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.BSSFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.BSSFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.AAAFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.AAAFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.CreateReservation.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.CreateReservation.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.CreateReservationResponse.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.CreateReservationResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.BSSFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.BSSFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.AAAFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.AAAFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.QueryReservation.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.QueryReservation.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.QueryReservationResponse.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.QueryReservationResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.BSSFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.BSSFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.AAAFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.AAAFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (Exception e) {
        throw new RuntimeException(e);
        }
           return null;
        }



    

        /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
        org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
        returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
        return returnMap;
        }

        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

        }//end of class
    