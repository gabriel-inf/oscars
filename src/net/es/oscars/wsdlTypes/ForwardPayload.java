
            /**
            * ForwardPayload.java
            *
            * This file was auto-generated from WSDL
            * by the Apache Axis2 version: #axisVersion# #today#
            */

            package net.es.oscars.wsdlTypes;
            /**
            *  ForwardPayload bean class
            */
        
        public  class ForwardPayload
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = forwardPayload
                Namespace URI = http://oscars.es.net/OSCARS
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for ContentType
                        */

                        protected java.lang.String localContentType ;
                        

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getContentType(){
                               return localContentType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ContentType
                               */
                               public void setContentType(java.lang.String param){
                            
                                    this.localContentType=param;
                            

                               }
                            

                        /**
                        * field for CreateReservation
                        */

                        protected net.es.oscars.wsdlTypes.ResCreateContent localCreateReservation ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCreateReservationTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return net.es.oscars.wsdlTypes.ResCreateContent
                           */
                           public  net.es.oscars.wsdlTypes.ResCreateContent getCreateReservation(){
                               return localCreateReservation;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CreateReservation
                               */
                               public void setCreateReservation(net.es.oscars.wsdlTypes.ResCreateContent param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localCreateReservationTracker = true;
                                       } else {
                                          localCreateReservationTracker = false;
                                              
                                       }
                                   
                                    this.localCreateReservation=param;
                            

                               }
                            

                        /**
                        * field for CancelReservation
                        */

                        protected net.es.oscars.wsdlTypes.ResTag localCancelReservation ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCancelReservationTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return net.es.oscars.wsdlTypes.ResTag
                           */
                           public  net.es.oscars.wsdlTypes.ResTag getCancelReservation(){
                               return localCancelReservation;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CancelReservation
                               */
                               public void setCancelReservation(net.es.oscars.wsdlTypes.ResTag param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localCancelReservationTracker = true;
                                       } else {
                                          localCancelReservationTracker = false;
                                              
                                       }
                                   
                                    this.localCancelReservation=param;
                            

                               }
                            

                        /**
                        * field for QueryReservation
                        */

                        protected net.es.oscars.wsdlTypes.ResTag localQueryReservation ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localQueryReservationTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return net.es.oscars.wsdlTypes.ResTag
                           */
                           public  net.es.oscars.wsdlTypes.ResTag getQueryReservation(){
                               return localQueryReservation;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param QueryReservation
                               */
                               public void setQueryReservation(net.es.oscars.wsdlTypes.ResTag param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localQueryReservationTracker = true;
                                       } else {
                                          localQueryReservationTracker = false;
                                              
                                       }
                                   
                                    this.localQueryReservation=param;
                            

                               }
                            

                        /**
                        * field for ListReservations
                        */

                        protected net.es.oscars.wsdlTypes.EmptyArg localListReservations ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localListReservationsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return net.es.oscars.wsdlTypes.EmptyArg
                           */
                           public  net.es.oscars.wsdlTypes.EmptyArg getListReservations(){
                               return localListReservations;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ListReservations
                               */
                               public void setListReservations(net.es.oscars.wsdlTypes.EmptyArg param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localListReservationsTracker = true;
                                       } else {
                                          localListReservationsTracker = false;
                                              
                                       }
                                   
                                    this.localListReservations=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
               
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"contentType", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"contentType");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("contentType");
                                    }
                                

                                          if (localContentType==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("contentType cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localContentType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                              if (localCreateReservationTracker){
                                    if (localCreateReservation==null){
                                         throw new RuntimeException("createReservation cannot be null!!");
                                    }
                                   localCreateReservation.getOMDataSource(
                                       new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","createReservation"),
                                       factory).serialize(xmlWriter);
                                } if (localCancelReservationTracker){
                                    if (localCancelReservation==null){
                                         throw new RuntimeException("cancelReservation cannot be null!!");
                                    }
                                   localCancelReservation.getOMDataSource(
                                       new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","cancelReservation"),
                                       factory).serialize(xmlWriter);
                                } if (localQueryReservationTracker){
                                    if (localQueryReservation==null){
                                         throw new RuntimeException("queryReservation cannot be null!!");
                                    }
                                   localQueryReservation.getOMDataSource(
                                       new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","queryReservation"),
                                       factory).serialize(xmlWriter);
                                } if (localListReservationsTracker){
                                    if (localListReservations==null){
                                         throw new RuntimeException("listReservations cannot be null!!");
                                    }
                                   localListReservations.getOMDataSource(
                                       new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","listReservations"),
                                       factory).serialize(xmlWriter);
                                }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "contentType"));
                            
                                        if (localContentType != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localContentType));
                                        } else {
                                           throw new RuntimeException("contentType cannot be null!!");
                                        }
                                     if (localCreateReservationTracker){
                            elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "createReservation"));
                            
                            
                                    if (localCreateReservation==null){
                                         throw new RuntimeException("createReservation cannot be null!!");
                                    }
                                    elementList.add(localCreateReservation);
                                } if (localCancelReservationTracker){
                            elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "cancelReservation"));
                            
                            
                                    if (localCancelReservation==null){
                                         throw new RuntimeException("cancelReservation cannot be null!!");
                                    }
                                    elementList.add(localCancelReservation);
                                } if (localQueryReservationTracker){
                            elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "queryReservation"));
                            
                            
                                    if (localQueryReservation==null){
                                         throw new RuntimeException("queryReservation cannot be null!!");
                                    }
                                    elementList.add(localQueryReservation);
                                } if (localListReservationsTracker){
                            elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "listReservations"));
                            
                            
                                    if (localListReservations==null){
                                         throw new RuntimeException("listReservations cannot be null!!");
                                    }
                                    elementList.add(localListReservations);
                                }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static ForwardPayload parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ForwardPayload object = new ForwardPayload();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"forwardPayload".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (ForwardPayload)net.es.oscars.oscars.ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","contentType").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setContentType(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","createReservation").equals(reader.getName())){
                                
                                        object.setCreateReservation(net.es.oscars.wsdlTypes.ResCreateContent.Factory.parse(reader));
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","cancelReservation").equals(reader.getName())){
                                
                                        object.setCancelReservation(net.es.oscars.wsdlTypes.ResTag.Factory.parse(reader));
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","queryReservation").equals(reader.getName())){
                                
                                        object.setQueryReservation(net.es.oscars.wsdlTypes.ResTag.Factory.parse(reader));
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","listReservations").equals(reader.getName())){
                                
                                        object.setListReservations(net.es.oscars.wsdlTypes.EmptyArg.Factory.parse(reader));
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          