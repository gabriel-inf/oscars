
        /**
        * OSCARSStub.java
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2 version: 1.1.1 Jan 09, 2007 (06:20:51 LKT)
        */
        package net.es.oscars.oscars;

        

        /*
        *  OSCARSStub java implementation
        */

        
        public class OSCARSStub extends org.apache.axis2.client.Stub
        {
        protected org.apache.axis2.description.AxisOperation[] _operations;

        //hashmaps to keep the fault mapping
        private java.util.HashMap faultExeptionNameMap = new java.util.HashMap();
        private java.util.HashMap faultExeptionClassNameMap = new java.util.HashMap();
        private java.util.HashMap faultMessageMap = new java.util.HashMap();

    
    private void populateAxisService() throws org.apache.axis2.AxisFault {

     //creating the Service with a unique name
     _service = new org.apache.axis2.description.AxisService("OSCARS" + this.hashCode());
     
    

        //creating the operations
        org.apache.axis2.description.AxisOperation __operation;
    


        _operations = new org.apache.axis2.description.AxisOperation[5];
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "cancelReservation"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[0]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "forward"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[1]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "listReservations"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[2]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "createReservation"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[3]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "queryReservation"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[4]=__operation;
            
        
        }

    //populates the faults
    private void populateFaults(){
         
              faultExeptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessageException"
               );
              faultExeptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessageException");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );
           
              faultExeptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessageException"
               );
              faultExeptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessageException");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );
           
              faultExeptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessageException"
               );
              faultExeptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessageException");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );
           
              faultExeptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessageException"
               );
              faultExeptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessageException");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );
           
              faultExeptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessageException"
               );
              faultExeptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessageException");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );
           
              faultExeptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessageException"
               );
              faultExeptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessageException");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );
           
              faultExeptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessageException"
               );
              faultExeptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessageException");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );
           
              faultExeptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessageException"
               );
              faultExeptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessageException");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );
           
              faultExeptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessageException"
               );
              faultExeptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessageException");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );
           
              faultExeptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessageException"
               );
              faultExeptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessageException");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );
           


    }

   /**
    Constructor that takes in a configContext
    */
   public OSCARSStub(org.apache.axis2.context.ConfigurationContext configurationContext,
        java.lang.String targetEndpoint)
        throws org.apache.axis2.AxisFault {
         //To populate AxisService
         populateAxisService();
         populateFaults();

        _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext,_service);
        
	
        configurationContext = _serviceClient.getServiceContext().getConfigurationContext();

        _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(
                targetEndpoint));
        
            //Set the soap version
            _serviceClient.getOptions().setSoapVersionURI(org.apache.axiom.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        
    
    }

    /**
     * Default Constructor
     */
    public OSCARSStub() throws org.apache.axis2.AxisFault {
        
                    this("https://oscars-dev.es.net:9090/axis2/services/OSCARS" );
                
    }

    /**
     * Constructor taking the target endpoint
     */
    public OSCARSStub(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(null,targetEndpoint);
    }



        
                    /**
                    * Auto generated method signature
                    * @see net.es.oscars.oscars.OSCARS#cancelReservation
                        * @param param100
                    
                    */
                    public net.es.oscars.wsdlTypes.CancelReservationResponse cancelReservation(

                    net.es.oscars.wsdlTypes.CancelReservation param100)
                    throws java.rmi.RemoteException
                    
                    
                        ,net.es.oscars.oscars.BSSFaultMessageException
                        ,net.es.oscars.oscars.AAAFaultMessageException{
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/cancelReservation");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param100,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "cancelReservation")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        net.es.oscars.wsdlTypes.CancelReservationResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (net.es.oscars.wsdlTypes.CancelReservationResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessageException){
                          throw (net.es.oscars.oscars.BSSFaultMessageException)ex;
                        }
                        
                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessageException){
                          throw (net.es.oscars.oscars.AAAFaultMessageException)ex;
                        }
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see net.es.oscars.oscars.OSCARS#forward
                        * @param param102
                    
                    */
                    public net.es.oscars.wsdlTypes.ForwardResponse forward(

                    net.es.oscars.wsdlTypes.Forward param102)
                    throws java.rmi.RemoteException
                    
                    
                        ,net.es.oscars.oscars.BSSFaultMessageException
                        ,net.es.oscars.oscars.AAAFaultMessageException{
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/forward");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param102,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "forward")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        net.es.oscars.wsdlTypes.ForwardResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (net.es.oscars.wsdlTypes.ForwardResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessageException){
                          throw (net.es.oscars.oscars.BSSFaultMessageException)ex;
                        }
                        
                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessageException){
                          throw (net.es.oscars.oscars.AAAFaultMessageException)ex;
                        }
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see net.es.oscars.oscars.OSCARS#listReservations
                        * @param param104
                    
                    */
                    public net.es.oscars.wsdlTypes.ListReservationsResponse listReservations(

                    net.es.oscars.wsdlTypes.ListReservations param104)
                    throws java.rmi.RemoteException
                    
                    
                        ,net.es.oscars.oscars.BSSFaultMessageException
                        ,net.es.oscars.oscars.AAAFaultMessageException{
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/listReservations");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param104,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "listReservations")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        net.es.oscars.wsdlTypes.ListReservationsResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (net.es.oscars.wsdlTypes.ListReservationsResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessageException){
                          throw (net.es.oscars.oscars.BSSFaultMessageException)ex;
                        }
                        
                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessageException){
                          throw (net.es.oscars.oscars.AAAFaultMessageException)ex;
                        }
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see net.es.oscars.oscars.OSCARS#createReservation
                        * @param param106
                    
                    */
                    public net.es.oscars.wsdlTypes.CreateReservationResponse createReservation(

                    net.es.oscars.wsdlTypes.CreateReservation param106)
                    throws java.rmi.RemoteException
                    
                    
                        ,net.es.oscars.oscars.BSSFaultMessageException
                        ,net.es.oscars.oscars.AAAFaultMessageException{
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[3].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/createReservation");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param106,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "createReservation")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        net.es.oscars.wsdlTypes.CreateReservationResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (net.es.oscars.wsdlTypes.CreateReservationResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessageException){
                          throw (net.es.oscars.oscars.BSSFaultMessageException)ex;
                        }
                        
                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessageException){
                          throw (net.es.oscars.oscars.AAAFaultMessageException)ex;
                        }
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see net.es.oscars.oscars.OSCARS#queryReservation
                        * @param param108
                    
                    */
                    public net.es.oscars.wsdlTypes.QueryReservationResponse queryReservation(

                    net.es.oscars.wsdlTypes.QueryReservation param108)
                    throws java.rmi.RemoteException
                    
                    
                        ,net.es.oscars.oscars.BSSFaultMessageException
                        ,net.es.oscars.oscars.AAAFaultMessageException{
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[4].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/queryReservation");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param108,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "queryReservation")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        net.es.oscars.wsdlTypes.QueryReservationResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (net.es.oscars.wsdlTypes.QueryReservationResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        
                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessageException){
                          throw (net.es.oscars.oscars.BSSFaultMessageException)ex;
                        }
                        
                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessageException){
                          throw (net.es.oscars.oscars.AAAFaultMessageException)ex;
                        }
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
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

    
    
    private javax.xml.namespace.QName[] opNameArray = null;
    private boolean optimizeContent(javax.xml.namespace.QName opName) {
        

        if (opNameArray == null) {
            return false;
        }
        for (int i = 0; i < opNameArray.length; i++) {
            if (opName.equals(opNameArray[i])) {
                return true;   
            }
        }
        return false;
    }
     //https://oscars-dev.es.net:9090/axis2/services/OSCARS
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
        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.CancelReservation param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.CancelReservation.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.Forward param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.Forward.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.ListReservations param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.ListReservations.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.CreateReservation param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.CreateReservation.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.QueryReservation param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.QueryReservation.MY_QNAME,factory));
                                
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



    
            private void setOpNameArray(){
            opNameArray = null;
            }
           
   }
   