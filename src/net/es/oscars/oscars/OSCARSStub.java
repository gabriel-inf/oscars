
/**
 * OSCARSStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */
        package net.es.oscars.oscars;



        /*
        *  OSCARSStub java implementation
        */


        public class OSCARSStub extends org.apache.axis2.client.Stub {
        protected org.apache.axis2.description.AxisOperation[] _operations;

        //hashmaps to keep the fault mapping
        private java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
        private java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
        private java.util.HashMap faultMessageMap = new java.util.HashMap();


    private void populateAxisService() throws org.apache.axis2.AxisFault {

     //creating the Service with a unique name
     _service = new org.apache.axis2.description.AxisService("OSCARS" + this.hashCode());

        //creating the operations
        org.apache.axis2.description.AxisOperation __operation;

        _operations = new org.apache.axis2.description.AxisOperation[11];

                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS", "createReservation"));
        _service.addOperation(__operation);




            _operations[0]=__operation;


                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS", "cancelReservation"));
        _service.addOperation(__operation);




            _operations[1]=__operation;


                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS", "queryReservation"));
        _service.addOperation(__operation);




            _operations[2]=__operation;


                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS", "modifyReservation"));
        _service.addOperation(__operation);




            _operations[3]=__operation;


                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS", "listReservations"));
        _service.addOperation(__operation);




            _operations[4]=__operation;


                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS", "getNetworkTopology"));
        _service.addOperation(__operation);




            _operations[5]=__operation;


                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS", "initiateTopologyPull"));
        _service.addOperation(__operation);




            _operations[6]=__operation;


                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS", "createPath"));
        _service.addOperation(__operation);




            _operations[7]=__operation;


                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS", "refreshPath"));
        _service.addOperation(__operation);




            _operations[8]=__operation;


                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS", "teardownPath"));
        _service.addOperation(__operation);




            _operations[9]=__operation;


                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS", "forward"));
        _service.addOperation(__operation);




            _operations[10]=__operation;


        }

    //populates the faults
    private void populateFaults(){

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.oscars.BSSFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "BSSFault"),
                "net.es.oscars.oscars.BSSFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "BSSFault"),
                 "net.es.oscars.wsdlTypes.BSSFault"
               );

              faultExceptionNameMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.oscars.AAAFaultMessage"
               );
              faultExceptionClassNameMap.put(new javax.xml.namespace.QName(
                "http://oscars.es.net/OSCARS",
                "AAAFault"),
                "net.es.oscars.oscars.AAAFaultMessage");
               faultMessageMap.put( new javax.xml.namespace.QName(
                 "http://oscars.es.net/OSCARS",
                 "AAAFault"),
                 "net.es.oscars.wsdlTypes.AAAFault"
               );



    }

    /**
      *Constructor that takes in a configContext
      */

    public OSCARSStub(org.apache.axis2.context.ConfigurationContext configurationContext,
       java.lang.String targetEndpoint)
       throws org.apache.axis2.AxisFault {
         this(configurationContext,targetEndpoint,false);
   }


   /**
     * Constructor that takes in a configContext  and useseperate listner
     */
   public OSCARSStub(org.apache.axis2.context.ConfigurationContext configurationContext,
        java.lang.String targetEndpoint, boolean useSeparateListener)
        throws org.apache.axis2.AxisFault {
         //To populate AxisService
         populateAxisService();
         populateFaults();

        _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext,_service);


        configurationContext = _serviceClient.getServiceContext().getConfigurationContext();

        _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(
                targetEndpoint));
        _serviceClient.getOptions().setUseSeparateListener(useSeparateListener);

            //Set the soap version
            _serviceClient.getOptions().setSoapVersionURI(org.apache.axiom.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);


    }

    /**
     * Default Constructor
     */
    public OSCARSStub(org.apache.axis2.context.ConfigurationContext configurationContext) throws org.apache.axis2.AxisFault {

                    this(configurationContext,"https://oscars-dev.es.net:9090/axis2/services/OSCARS" );

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
                     * @see net.es.oscars.oscars.OSCARS#createReservation
                     * @param createReservation0

                     */


                            public  net.es.oscars.wsdlTypes.CreateReservationResponse createReservation(

                            net.es.oscars.wsdlTypes.CreateReservation createReservation0)


                    throws java.rmi.RemoteException


                        ,net.es.oscars.oscars.BSSFaultMessage
                        ,net.es.oscars.oscars.AAAFaultMessage{

              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/createReservation");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    createReservation0,
                                                    optimizeContent(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                    "createReservation")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
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
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
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

                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessage){
                          throw (net.es.oscars.oscars.BSSFaultMessage)ex;
                        }

                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessage){
                          throw (net.es.oscars.oscars.AAAFaultMessage)ex;
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
                     * @see net.es.oscars.oscars.OSCARS#cancelReservation
                     * @param cancelReservation2

                     */


                            public  net.es.oscars.wsdlTypes.CancelReservationResponse cancelReservation(

                            net.es.oscars.wsdlTypes.CancelReservation cancelReservation2)


                    throws java.rmi.RemoteException


                        ,net.es.oscars.oscars.BSSFaultMessage
                        ,net.es.oscars.oscars.AAAFaultMessage{

              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/cancelReservation");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    cancelReservation2,
                                                    optimizeContent(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                    "cancelReservation")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
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
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
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

                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessage){
                          throw (net.es.oscars.oscars.BSSFaultMessage)ex;
                        }

                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessage){
                          throw (net.es.oscars.oscars.AAAFaultMessage)ex;
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
                     * @param queryReservation4

                     */


                            public  net.es.oscars.wsdlTypes.QueryReservationResponse queryReservation(

                            net.es.oscars.wsdlTypes.QueryReservation queryReservation4)


                    throws java.rmi.RemoteException


                        ,net.es.oscars.oscars.BSSFaultMessage
                        ,net.es.oscars.oscars.AAAFaultMessage{

              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/queryReservation");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    queryReservation4,
                                                    optimizeContent(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                    "queryReservation")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
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
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
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

                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessage){
                          throw (net.es.oscars.oscars.BSSFaultMessage)ex;
                        }

                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessage){
                          throw (net.es.oscars.oscars.AAAFaultMessage)ex;
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
                     * @see net.es.oscars.oscars.OSCARS#modifyReservation
                     * @param modifyReservation6

                     */


                            public  net.es.oscars.wsdlTypes.ModifyReservationResponse modifyReservation(

                            net.es.oscars.wsdlTypes.ModifyReservation modifyReservation6)


                    throws java.rmi.RemoteException


                        ,net.es.oscars.oscars.BSSFaultMessage
                        ,net.es.oscars.oscars.AAAFaultMessage{

              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[3].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/modifyReservation");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    modifyReservation6,
                                                    optimizeContent(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                    "modifyReservation")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
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
                                             net.es.oscars.wsdlTypes.ModifyReservationResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));
                                _messageContext.getTransportOut().getSender().cleanup(_messageContext);

                                        return (net.es.oscars.wsdlTypes.ModifyReservationResponse)object;

         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
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

                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessage){
                          throw (net.es.oscars.oscars.BSSFaultMessage)ex;
                        }

                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessage){
                          throw (net.es.oscars.oscars.AAAFaultMessage)ex;
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
                     * @param listReservations8

                     */


                            public  net.es.oscars.wsdlTypes.ListReservationsResponse listReservations(

                            net.es.oscars.wsdlTypes.ListReservations listReservations8)


                    throws java.rmi.RemoteException


                        ,net.es.oscars.oscars.BSSFaultMessage
                        ,net.es.oscars.oscars.AAAFaultMessage{

              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[4].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/listReservations");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    listReservations8,
                                                    optimizeContent(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                    "listReservations")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
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
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
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

                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessage){
                          throw (net.es.oscars.oscars.BSSFaultMessage)ex;
                        }

                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessage){
                          throw (net.es.oscars.oscars.AAAFaultMessage)ex;
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
                     * @see net.es.oscars.oscars.OSCARS#getNetworkTopology
                     * @param getNetworkTopology10

                     */


                            public  net.es.oscars.wsdlTypes.GetNetworkTopologyResponse getNetworkTopology(

                            net.es.oscars.wsdlTypes.GetNetworkTopology getNetworkTopology10)


                    throws java.rmi.RemoteException


                        ,net.es.oscars.oscars.BSSFaultMessage
                        ,net.es.oscars.oscars.AAAFaultMessage{

              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[5].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/getNetworkTopology");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    getNetworkTopology10,
                                                    optimizeContent(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                    "getNetworkTopology")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
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
                                             net.es.oscars.wsdlTypes.GetNetworkTopologyResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));
                                _messageContext.getTransportOut().getSender().cleanup(_messageContext);

                                        return (net.es.oscars.wsdlTypes.GetNetworkTopologyResponse)object;

         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
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

                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessage){
                          throw (net.es.oscars.oscars.BSSFaultMessage)ex;
                        }

                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessage){
                          throw (net.es.oscars.oscars.AAAFaultMessage)ex;
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
                     * @see net.es.oscars.oscars.OSCARS#initiateTopologyPull
                     * @param initiateTopologyPull12

                     */


                            public  net.es.oscars.wsdlTypes.InitiateTopologyPullResponse initiateTopologyPull(

                            net.es.oscars.wsdlTypes.InitiateTopologyPull initiateTopologyPull12)


                    throws java.rmi.RemoteException


                        ,net.es.oscars.oscars.BSSFaultMessage
                        ,net.es.oscars.oscars.AAAFaultMessage{

              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[6].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/initiateTopologyPull");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    initiateTopologyPull12,
                                                    optimizeContent(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                    "initiateTopologyPull")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
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
                                             net.es.oscars.wsdlTypes.InitiateTopologyPullResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));
                                _messageContext.getTransportOut().getSender().cleanup(_messageContext);

                                        return (net.es.oscars.wsdlTypes.InitiateTopologyPullResponse)object;

         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
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

                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessage){
                          throw (net.es.oscars.oscars.BSSFaultMessage)ex;
                        }

                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessage){
                          throw (net.es.oscars.oscars.AAAFaultMessage)ex;
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
                     * @see net.es.oscars.oscars.OSCARS#createPath
                     * @param createPath14

                     */


                            public  net.es.oscars.wsdlTypes.CreatePathResponse createPath(

                            net.es.oscars.wsdlTypes.CreatePath createPath14)


                    throws java.rmi.RemoteException


                        ,net.es.oscars.oscars.BSSFaultMessage
                        ,net.es.oscars.oscars.AAAFaultMessage{

              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[7].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/createPath");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    createPath14,
                                                    optimizeContent(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                    "createPath")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
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
                                             net.es.oscars.wsdlTypes.CreatePathResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));
                                _messageContext.getTransportOut().getSender().cleanup(_messageContext);

                                        return (net.es.oscars.wsdlTypes.CreatePathResponse)object;

         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
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

                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessage){
                          throw (net.es.oscars.oscars.BSSFaultMessage)ex;
                        }

                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessage){
                          throw (net.es.oscars.oscars.AAAFaultMessage)ex;
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
                     * @see net.es.oscars.oscars.OSCARS#refreshPath
                     * @param refreshPath16

                     */


                            public  net.es.oscars.wsdlTypes.RefreshPathResponse refreshPath(

                            net.es.oscars.wsdlTypes.RefreshPath refreshPath16)


                    throws java.rmi.RemoteException


                        ,net.es.oscars.oscars.BSSFaultMessage
                        ,net.es.oscars.oscars.AAAFaultMessage{

              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[8].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/refreshPath");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    refreshPath16,
                                                    optimizeContent(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                    "refreshPath")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
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
                                             net.es.oscars.wsdlTypes.RefreshPathResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));
                                _messageContext.getTransportOut().getSender().cleanup(_messageContext);

                                        return (net.es.oscars.wsdlTypes.RefreshPathResponse)object;

         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
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

                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessage){
                          throw (net.es.oscars.oscars.BSSFaultMessage)ex;
                        }

                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessage){
                          throw (net.es.oscars.oscars.AAAFaultMessage)ex;
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
                     * @see net.es.oscars.oscars.OSCARS#teardownPath
                     * @param teardownPath18

                     */


                            public  net.es.oscars.wsdlTypes.TeardownPathResponse teardownPath(

                            net.es.oscars.wsdlTypes.TeardownPath teardownPath18)


                    throws java.rmi.RemoteException


                        ,net.es.oscars.oscars.BSSFaultMessage
                        ,net.es.oscars.oscars.AAAFaultMessage{

              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[9].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/teardownPath");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    teardownPath18,
                                                    optimizeContent(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                    "teardownPath")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
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
                                             net.es.oscars.wsdlTypes.TeardownPathResponse.class,
                                              getEnvelopeNamespaces(_returnEnv));
                                _messageContext.getTransportOut().getSender().cleanup(_messageContext);

                                        return (net.es.oscars.wsdlTypes.TeardownPathResponse)object;

         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
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

                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessage){
                          throw (net.es.oscars.oscars.BSSFaultMessage)ex;
                        }

                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessage){
                          throw (net.es.oscars.oscars.AAAFaultMessage)ex;
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
                     * @param forward20

                     */


                            public  net.es.oscars.wsdlTypes.ForwardResponse forward(

                            net.es.oscars.wsdlTypes.Forward forward20)


                    throws java.rmi.RemoteException


                        ,net.es.oscars.oscars.BSSFaultMessage
                        ,net.es.oscars.oscars.AAAFaultMessage{

              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[10].getName());
              _operationClient.getOptions().setAction("http://oscars.es.net/OSCARS/forward");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    forward20,
                                                    optimizeContent(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                    "forward")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
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
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
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

                        if (ex instanceof net.es.oscars.oscars.BSSFaultMessage){
                          throw (net.es.oscars.oscars.BSSFaultMessage)ex;
                        }

                        if (ex instanceof net.es.oscars.oscars.AAAFaultMessage){
                          throw (net.es.oscars.oscars.AAAFaultMessage)ex;
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


                            private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.QueryReservation param, boolean optimizeContent)
                            throws org.apache.axis2.AxisFault{


                                        try{

                                                org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.QueryReservation.MY_QNAME,factory));
                                                return emptyEnvelope;
                                            } catch(org.apache.axis2.databinding.ADBException e){
                                                throw org.apache.axis2.AxisFault.makeFault(e);
                                            }


                            }


                             /* methods to provide back word compatibility */



                            private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.RefreshPath param, boolean optimizeContent)
                            throws org.apache.axis2.AxisFault{


                                        try{

                                                org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.RefreshPath.MY_QNAME,factory));
                                                return emptyEnvelope;
                                            } catch(org.apache.axis2.databinding.ADBException e){
                                                throw org.apache.axis2.AxisFault.makeFault(e);
                                            }


                            }


                             /* methods to provide back word compatibility */



                            private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.TeardownPath param, boolean optimizeContent)
                            throws org.apache.axis2.AxisFault{


                                        try{

                                                org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.TeardownPath.MY_QNAME,factory));
                                                return emptyEnvelope;
                                            } catch(org.apache.axis2.databinding.ADBException e){
                                                throw org.apache.axis2.AxisFault.makeFault(e);
                                            }


                            }


                             /* methods to provide back word compatibility */



                            private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.CreateReservation param, boolean optimizeContent)
                            throws org.apache.axis2.AxisFault{


                                        try{

                                                org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.CreateReservation.MY_QNAME,factory));
                                                return emptyEnvelope;
                                            } catch(org.apache.axis2.databinding.ADBException e){
                                                throw org.apache.axis2.AxisFault.makeFault(e);
                                            }


                            }


                             /* methods to provide back word compatibility */



                            private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.GetNetworkTopology param, boolean optimizeContent)
                            throws org.apache.axis2.AxisFault{


                                        try{

                                                org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.GetNetworkTopology.MY_QNAME,factory));
                                                return emptyEnvelope;
                                            } catch(org.apache.axis2.databinding.ADBException e){
                                                throw org.apache.axis2.AxisFault.makeFault(e);
                                            }


                            }


                             /* methods to provide back word compatibility */



                            private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.ListReservations param, boolean optimizeContent)
                            throws org.apache.axis2.AxisFault{


                                        try{

                                                org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.ListReservations.MY_QNAME,factory));
                                                return emptyEnvelope;
                                            } catch(org.apache.axis2.databinding.ADBException e){
                                                throw org.apache.axis2.AxisFault.makeFault(e);
                                            }


                            }


                             /* methods to provide back word compatibility */



                            private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.ModifyReservation param, boolean optimizeContent)
                            throws org.apache.axis2.AxisFault{


                                        try{

                                                org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.ModifyReservation.MY_QNAME,factory));
                                                return emptyEnvelope;
                                            } catch(org.apache.axis2.databinding.ADBException e){
                                                throw org.apache.axis2.AxisFault.makeFault(e);
                                            }


                            }


                             /* methods to provide back word compatibility */



                            private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.CreatePath param, boolean optimizeContent)
                            throws org.apache.axis2.AxisFault{


                                        try{

                                                org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.CreatePath.MY_QNAME,factory));
                                                return emptyEnvelope;
                                            } catch(org.apache.axis2.databinding.ADBException e){
                                                throw org.apache.axis2.AxisFault.makeFault(e);
                                            }


                            }


                             /* methods to provide back word compatibility */



                            private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.InitiateTopologyPull param, boolean optimizeContent)
                            throws org.apache.axis2.AxisFault{


                                        try{

                                                org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.InitiateTopologyPull.MY_QNAME,factory));
                                                return emptyEnvelope;
                                            } catch(org.apache.axis2.databinding.ADBException e){
                                                throw org.apache.axis2.AxisFault.makeFault(e);
                                            }


                            }


                             /* methods to provide back word compatibility */



                            private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.Forward param, boolean optimizeContent)
                            throws org.apache.axis2.AxisFault{


                                        try{

                                                org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.Forward.MY_QNAME,factory));
                                                return emptyEnvelope;
                                            } catch(org.apache.axis2.databinding.ADBException e){
                                                throw org.apache.axis2.AxisFault.makeFault(e);
                                            }


                            }


                             /* methods to provide back word compatibility */



                            private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.es.oscars.wsdlTypes.CancelReservation param, boolean optimizeContent)
                            throws org.apache.axis2.AxisFault{


                                        try{

                                                org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                emptyEnvelope.getBody().addChild(param.getOMElement(net.es.oscars.wsdlTypes.CancelReservation.MY_QNAME,factory));
                                                return emptyEnvelope;
                                            } catch(org.apache.axis2.databinding.ADBException e){
                                                throw org.apache.axis2.AxisFault.makeFault(e);
                                            }


                            }


                             /* methods to provide back word compatibility */




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

        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }




   }
