
/**
 * OSCARSMessageReceiverInOut.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */
        package net.es.oscars.oscars;

        /**
        *  OSCARSMessageReceiverInOut message receiver
        */

        public class OSCARSMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutMessageReceiver{


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
        if((op.getName() != null) && ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJava(op.getName().getLocalPart())) != null)){

        

            if("cancelReservation".equals(methodName)){
                
                net.es.oscars.wsdlTypes.CancelReservationResponse cancelReservationResponse48 = null;
	                        net.es.oscars.wsdlTypes.CancelReservation wrappedParam =
                                                             (net.es.oscars.wsdlTypes.CancelReservation)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.es.oscars.wsdlTypes.CancelReservation.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               cancelReservationResponse48 =
                                                   
                                                   
                                                         skel.cancelReservation(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), cancelReservationResponse48, false);
                                    } else 

            if("createReservation".equals(methodName)){
                
                net.es.oscars.wsdlTypes.CreateReservationResponse createReservationResponse50 = null;
	                        net.es.oscars.wsdlTypes.CreateReservation wrappedParam =
                                                             (net.es.oscars.wsdlTypes.CreateReservation)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.es.oscars.wsdlTypes.CreateReservation.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               createReservationResponse50 =
                                                   
                                                   
                                                         skel.createReservation(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), createReservationResponse50, false);
                                    } else 

            if("queryReservation".equals(methodName)){
                
                net.es.oscars.wsdlTypes.QueryReservationResponse queryReservationResponse52 = null;
	                        net.es.oscars.wsdlTypes.QueryReservation wrappedParam =
                                                             (net.es.oscars.wsdlTypes.QueryReservation)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.es.oscars.wsdlTypes.QueryReservation.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               queryReservationResponse52 =
                                                   
                                                   
                                                         skel.queryReservation(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), queryReservationResponse52, false);
                                    } else 

            if("refreshPath".equals(methodName)){
                
                net.es.oscars.wsdlTypes.RefreshPathResponse refreshPathResponse54 = null;
	                        net.es.oscars.wsdlTypes.RefreshPath wrappedParam =
                                                             (net.es.oscars.wsdlTypes.RefreshPath)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.es.oscars.wsdlTypes.RefreshPath.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               refreshPathResponse54 =
                                                   
                                                   
                                                         skel.refreshPath(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), refreshPathResponse54, false);
                                    } else 

            if("teardownPath".equals(methodName)){
                
                net.es.oscars.wsdlTypes.TeardownPathResponse teardownPathResponse56 = null;
	                        net.es.oscars.wsdlTypes.TeardownPath wrappedParam =
                                                             (net.es.oscars.wsdlTypes.TeardownPath)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.es.oscars.wsdlTypes.TeardownPath.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               teardownPathResponse56 =
                                                   
                                                   
                                                         skel.teardownPath(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), teardownPathResponse56, false);
                                    } else 

            if("createPath".equals(methodName)){
                
                net.es.oscars.wsdlTypes.CreatePathResponse createPathResponse58 = null;
	                        net.es.oscars.wsdlTypes.CreatePath wrappedParam =
                                                             (net.es.oscars.wsdlTypes.CreatePath)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.es.oscars.wsdlTypes.CreatePath.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               createPathResponse58 =
                                                   
                                                   
                                                         skel.createPath(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), createPathResponse58, false);
                                    } else 

            if("getNetworkTopology".equals(methodName)){
                
                net.es.oscars.wsdlTypes.GetNetworkTopologyResponse getNetworkTopologyResponse60 = null;
	                        net.es.oscars.wsdlTypes.GetNetworkTopology wrappedParam =
                                                             (net.es.oscars.wsdlTypes.GetNetworkTopology)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.es.oscars.wsdlTypes.GetNetworkTopology.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               getNetworkTopologyResponse60 =
                                                   
                                                   
                                                         skel.getNetworkTopology(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), getNetworkTopologyResponse60, false);
                                    } else 

            if("modifyReservation".equals(methodName)){
                
                net.es.oscars.wsdlTypes.ModifyReservationResponse modifyReservationResponse62 = null;
	                        net.es.oscars.wsdlTypes.ModifyReservation wrappedParam =
                                                             (net.es.oscars.wsdlTypes.ModifyReservation)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.es.oscars.wsdlTypes.ModifyReservation.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               modifyReservationResponse62 =
                                                   
                                                   
                                                         skel.modifyReservation(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), modifyReservationResponse62, false);
                                    } else 

            if("forward".equals(methodName)){
                
                net.es.oscars.wsdlTypes.ForwardResponse forwardResponse64 = null;
	                        net.es.oscars.wsdlTypes.Forward wrappedParam =
                                                             (net.es.oscars.wsdlTypes.Forward)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.es.oscars.wsdlTypes.Forward.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               forwardResponse64 =
                                                   
                                                   
                                                         skel.forward(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), forwardResponse64, false);
                                    } else 

            if("listReservations".equals(methodName)){
                
                net.es.oscars.wsdlTypes.ListReservationsResponse listReservationsResponse66 = null;
	                        net.es.oscars.wsdlTypes.ListReservations wrappedParam =
                                                             (net.es.oscars.wsdlTypes.ListReservations)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.es.oscars.wsdlTypes.ListReservations.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               listReservationsResponse66 =
                                                   
                                                   
                                                         skel.listReservations(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), listReservationsResponse66, false);
                                    } else 

            if("initiateTopologyPull".equals(methodName)){
                
                net.es.oscars.wsdlTypes.InitiateTopologyPullResponse initiateTopologyPullResponse68 = null;
	                        net.es.oscars.wsdlTypes.InitiateTopologyPull wrappedParam =
                                                             (net.es.oscars.wsdlTypes.InitiateTopologyPull)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.es.oscars.wsdlTypes.InitiateTopologyPull.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               initiateTopologyPullResponse68 =
                                                   
                                                   
                                                         skel.initiateTopologyPull(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), initiateTopologyPullResponse68, false);
                                    
            } else {
              throw new java.lang.RuntimeException("method not found");
            }
        

        newMsgContext.setEnvelope(envelope);
        }
        } catch (AAAFaultMessage e) {

            msgContext.setProperty(org.apache.axis2.Constants.FAULT_NAME,"AAAFault");
            org.apache.axis2.AxisFault f = createAxisFault(e);
            if (e.getFaultMessage() != null){
                f.setDetail(toOM(e.getFaultMessage(),false));
            }
            throw f;
            }
         catch (BSSFaultMessage e) {

            msgContext.setProperty(org.apache.axis2.Constants.FAULT_NAME,"BSSFault");
            org.apache.axis2.AxisFault f = createAxisFault(e);
            if (e.getFaultMessage() != null){
                f.setDetail(toOM(e.getFaultMessage(),false));
            }
            throw f;
            }
        
        catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }
        
        //
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.CancelReservation param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.CancelReservation.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.CancelReservationResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.CancelReservationResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.BSSFault param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.BSSFault.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.AAAFault param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.AAAFault.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.CreateReservation param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.CreateReservation.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.CreateReservationResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.CreateReservationResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.QueryReservation param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.QueryReservation.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.QueryReservationResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.QueryReservationResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.RefreshPath param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.RefreshPath.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.RefreshPathResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.RefreshPathResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.TeardownPath param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.TeardownPath.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.TeardownPathResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.TeardownPathResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.CreatePath param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.CreatePath.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.CreatePathResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.CreatePathResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.GetNetworkTopology param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.GetNetworkTopology.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.GetNetworkTopologyResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.GetNetworkTopologyResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.ModifyReservation param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.ModifyReservation.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.ModifyReservationResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.ModifyReservationResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.oasis_open.docs.wsn.b_2.Notify param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.oasis_open.docs.wsn.b_2.Notify.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.Forward param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.Forward.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.ForwardResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.ForwardResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.ListReservations param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.ListReservations.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.ListReservationsResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.ListReservationsResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.InitiateTopologyPull param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.InitiateTopologyPull.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.es.oscars.wsdlTypes.InitiateTopologyPullResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.es.oscars.wsdlTypes.InitiateTopologyPullResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.CancelReservationResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.CancelReservationResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.es.oscars.wsdlTypes.CancelReservationResponse wrapcancelReservation(){
                                net.es.oscars.wsdlTypes.CancelReservationResponse wrappedElement = new net.es.oscars.wsdlTypes.CancelReservationResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.CreateReservationResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.CreateReservationResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.es.oscars.wsdlTypes.CreateReservationResponse wrapcreateReservation(){
                                net.es.oscars.wsdlTypes.CreateReservationResponse wrappedElement = new net.es.oscars.wsdlTypes.CreateReservationResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.QueryReservationResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.QueryReservationResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.es.oscars.wsdlTypes.QueryReservationResponse wrapqueryReservation(){
                                net.es.oscars.wsdlTypes.QueryReservationResponse wrappedElement = new net.es.oscars.wsdlTypes.QueryReservationResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.RefreshPathResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.RefreshPathResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.es.oscars.wsdlTypes.RefreshPathResponse wraprefreshPath(){
                                net.es.oscars.wsdlTypes.RefreshPathResponse wrappedElement = new net.es.oscars.wsdlTypes.RefreshPathResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.TeardownPathResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.TeardownPathResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.es.oscars.wsdlTypes.TeardownPathResponse wrapteardownPath(){
                                net.es.oscars.wsdlTypes.TeardownPathResponse wrappedElement = new net.es.oscars.wsdlTypes.TeardownPathResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.CreatePathResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.CreatePathResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.es.oscars.wsdlTypes.CreatePathResponse wrapcreatePath(){
                                net.es.oscars.wsdlTypes.CreatePathResponse wrappedElement = new net.es.oscars.wsdlTypes.CreatePathResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.GetNetworkTopologyResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.GetNetworkTopologyResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.es.oscars.wsdlTypes.GetNetworkTopologyResponse wrapgetNetworkTopology(){
                                net.es.oscars.wsdlTypes.GetNetworkTopologyResponse wrappedElement = new net.es.oscars.wsdlTypes.GetNetworkTopologyResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.ModifyReservationResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.ModifyReservationResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.es.oscars.wsdlTypes.ModifyReservationResponse wrapmodifyReservation(){
                                net.es.oscars.wsdlTypes.ModifyReservationResponse wrappedElement = new net.es.oscars.wsdlTypes.ModifyReservationResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.ForwardResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.ForwardResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.es.oscars.wsdlTypes.ForwardResponse wrapforward(){
                                net.es.oscars.wsdlTypes.ForwardResponse wrappedElement = new net.es.oscars.wsdlTypes.ForwardResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.ListReservationsResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.ListReservationsResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.es.oscars.wsdlTypes.ListReservationsResponse wraplistReservations(){
                                net.es.oscars.wsdlTypes.ListReservationsResponse wrappedElement = new net.es.oscars.wsdlTypes.ListReservationsResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.InitiateTopologyPullResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.InitiateTopologyPullResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.es.oscars.wsdlTypes.InitiateTopologyPullResponse wrapinitiateTopologyPull(){
                                net.es.oscars.wsdlTypes.InitiateTopologyPullResponse wrappedElement = new net.es.oscars.wsdlTypes.InitiateTopologyPullResponse();
                                return wrappedElement;
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
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{

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
           
                if (net.es.oscars.wsdlTypes.RefreshPath.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.RefreshPath.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.RefreshPathResponse.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.RefreshPathResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.BSSFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.BSSFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.AAAFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.AAAFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.TeardownPath.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.TeardownPath.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.TeardownPathResponse.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.TeardownPathResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.BSSFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.BSSFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.AAAFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.AAAFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.CreatePath.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.CreatePath.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.CreatePathResponse.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.CreatePathResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.BSSFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.BSSFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.AAAFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.AAAFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.GetNetworkTopology.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.GetNetworkTopology.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.GetNetworkTopologyResponse.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.GetNetworkTopologyResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.BSSFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.BSSFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.AAAFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.AAAFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.ModifyReservation.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.ModifyReservation.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.ModifyReservationResponse.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.ModifyReservationResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.BSSFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.BSSFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.AAAFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.AAAFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.oasis_open.docs.wsn.b_2.Notify.class.equals(type)){
                
                           return org.oasis_open.docs.wsn.b_2.Notify.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

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
           
                if (net.es.oscars.wsdlTypes.InitiateTopologyPull.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.InitiateTopologyPull.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.InitiateTopologyPullResponse.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.InitiateTopologyPullResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.BSSFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.BSSFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.es.oscars.wsdlTypes.AAAFault.class.equals(type)){
                
                           return net.es.oscars.wsdlTypes.AAAFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
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
    