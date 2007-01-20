
            /**
            * ExtensionMapper.java
            *
            * This file was auto-generated from WSDL
            * by the Apache Axis2 version: #axisVersion# #today#
            */

            package net.es.oscars.oscars;
            /**
            *  ExtensionMapper class
            */
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://oscars.es.net/OSCARS".equals(namespaceURI) &&
                  "login".equals(typeName)){
                   
                            return  net.es.oscars.wsdlTypes.Login.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://oscars.es.net/OSCARS".equals(namespaceURI) &&
                  "forwardPayload".equals(typeName)){
                   
                            return  net.es.oscars.wsdlTypes.ForwardPayload.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://oscars.es.net/OSCARS".equals(namespaceURI) &&
                  "forwardReply".equals(typeName)){
                   
                            return  net.es.oscars.wsdlTypes.ForwardReply.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://oscars.es.net/OSCARS".equals(namespaceURI) &&
                  "createReply".equals(typeName)){
                   
                            return  net.es.oscars.wsdlTypes.CreateReply.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://oscars.es.net/OSCARS".equals(namespaceURI) &&
                  "resCreateContent".equals(typeName)){
                   
                            return  net.es.oscars.wsdlTypes.ResCreateContent.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://oscars.es.net/OSCARS".equals(namespaceURI) &&
                  "emptyArg".equals(typeName)){
                   
                            return  net.es.oscars.wsdlTypes.EmptyArg.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://oscars.es.net/OSCARS".equals(namespaceURI) &&
                  "resTag".equals(typeName)){
                   
                            return  net.es.oscars.wsdlTypes.ResTag.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://oscars.es.net/OSCARS".equals(namespaceURI) &&
                  "resInfoContent".equals(typeName)){
                   
                            return  net.es.oscars.wsdlTypes.ResInfoContent.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://oscars.es.net/OSCARS".equals(namespaceURI) &&
                  "resDetails".equals(typeName)){
                   
                            return  net.es.oscars.wsdlTypes.ResDetails.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://oscars.es.net/OSCARS".equals(namespaceURI) &&
                  "listReply".equals(typeName)){
                   
                            return  net.es.oscars.wsdlTypes.ListReply.Factory.parse(reader);
                        

                  }

              
             throw new java.lang.RuntimeException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    