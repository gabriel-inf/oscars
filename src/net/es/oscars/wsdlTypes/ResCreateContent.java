
            /**
            * ResCreateContent.java
            *
            * This file was auto-generated from WSDL
            * by the Apache Axis2 version: #axisVersion# #today#
            */

            package net.es.oscars.wsdlTypes;
            /**
            *  ResCreateContent bean class
            */
        
        public  class ResCreateContent
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = resCreateContent
                Namespace URI = http://oscars.es.net/OSCARS
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for SrcHost
                        */

                        protected java.lang.String localSrcHost ;
                        

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSrcHost(){
                               return localSrcHost;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SrcHost
                               */
                               public void setSrcHost(java.lang.String param){
                            
                                    this.localSrcHost=param;
                            

                               }
                            

                        /**
                        * field for DestHost
                        */

                        protected java.lang.String localDestHost ;
                        

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDestHost(){
                               return localDestHost;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DestHost
                               */
                               public void setDestHost(java.lang.String param){
                            
                                    this.localDestHost=param;
                            

                               }
                            

                        /**
                        * field for StartTime
                        */

                        protected java.util.Calendar localStartTime ;
                        

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getStartTime(){
                               return localStartTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StartTime
                               */
                               public void setStartTime(java.util.Calendar param){
                            
                                    this.localStartTime=param;
                            

                               }
                            

                        /**
                        * field for EndTime
                        */

                        protected java.util.Calendar localEndTime ;
                        

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getEndTime(){
                               return localEndTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param EndTime
                               */
                               public void setEndTime(java.util.Calendar param){
                            
                                    this.localEndTime=param;
                            

                               }
                            

                        /**
                        * field for Bandwidth
                        */

                        protected int localBandwidth ;
                        

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getBandwidth(){
                               return localBandwidth;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Bandwidth
                               */
                               public void setBandwidth(int param){
                            
                                    this.localBandwidth=param;
                            

                               }
                            

                        /**
                        * field for BurstLimit
                        */

                        protected int localBurstLimit ;
                        

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getBurstLimit(){
                               return localBurstLimit;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param BurstLimit
                               */
                               public void setBurstLimit(int param){
                            
                                    this.localBurstLimit=param;
                            

                               }
                            

                        /**
                        * field for Description
                        */

                        protected java.lang.String localDescription ;
                        

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDescription(){
                               return localDescription;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Description
                               */
                               public void setDescription(java.lang.String param){
                            
                                    this.localDescription=param;
                            

                               }
                            

                        /**
                        * field for IngressRouterIP
                        */

                        protected java.lang.String localIngressRouterIP ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIngressRouterIPTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIngressRouterIP(){
                               return localIngressRouterIP;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IngressRouterIP
                               */
                               public void setIngressRouterIP(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIngressRouterIPTracker = true;
                                       } else {
                                          localIngressRouterIPTracker = false;
                                              
                                       }
                                   
                                    this.localIngressRouterIP=param;
                            

                               }
                            

                        /**
                        * field for EgressRouterIP
                        */

                        protected java.lang.String localEgressRouterIP ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEgressRouterIPTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getEgressRouterIP(){
                               return localEgressRouterIP;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param EgressRouterIP
                               */
                               public void setEgressRouterIP(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localEgressRouterIPTracker = true;
                                       } else {
                                          localEgressRouterIPTracker = false;
                                              
                                       }
                                   
                                    this.localEgressRouterIP=param;
                            

                               }
                            

                        /**
                        * field for CreateRouteDirection
                        */

                        protected java.lang.String localCreateRouteDirection ;
                        

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getCreateRouteDirection(){
                               return localCreateRouteDirection;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CreateRouteDirection
                               */
                               public void setCreateRouteDirection(java.lang.String param){
                            
                                    this.localCreateRouteDirection=param;
                            

                               }
                            

                        /**
                        * field for Protocol
                        */

                        protected java.lang.String localProtocol ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localProtocolTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getProtocol(){
                               return localProtocol;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Protocol
                               */
                               public void setProtocol(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localProtocolTracker = true;
                                       } else {
                                          localProtocolTracker = false;
                                              
                                       }
                                   
                                    this.localProtocol=param;
                            

                               }
                            

                        /**
                        * field for SrcPort
                        */

                        protected int localSrcPort ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSrcPortTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getSrcPort(){
                               return localSrcPort;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SrcPort
                               */
                               public void setSrcPort(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localSrcPortTracker = true;
                                   
                                    this.localSrcPort=param;
                            

                               }
                            

                        /**
                        * field for DestPort
                        */

                        protected int localDestPort ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDestPortTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getDestPort(){
                               return localDestPort;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DestPort
                               */
                               public void setDestPort(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localDestPortTracker = true;
                                   
                                    this.localDestPort=param;
                            

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

                                            xmlWriter.writeStartElement(prefix,"srcHost", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"srcHost");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("srcHost");
                                    }
                                

                                          if (localSrcHost==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("srcHost cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSrcHost);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"destHost", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"destHost");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("destHost");
                                    }
                                

                                          if (localDestHost==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("destHost cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDestHost);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"startTime", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"startTime");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("startTime");
                                    }
                                

                                          if (localStartTime==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("startTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStartTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"endTime", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"endTime");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("endTime");
                                    }
                                

                                          if (localEndTime==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("endTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEndTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"bandwidth", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"bandwidth");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("bandwidth");
                                    }
                                
                                       xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBandwidth));
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"burstLimit", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"burstLimit");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("burstLimit");
                                    }
                                
                                       xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBurstLimit));
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"description", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"description");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("description");
                                    }
                                

                                          if (localDescription==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("description cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDescription);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                              if (localIngressRouterIPTracker){
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"ingressRouterIP", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"ingressRouterIP");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("ingressRouterIP");
                                    }
                                

                                          if (localIngressRouterIP==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("ingressRouterIP cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIngressRouterIP);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localEgressRouterIPTracker){
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"egressRouterIP", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"egressRouterIP");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("egressRouterIP");
                                    }
                                

                                          if (localEgressRouterIP==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("egressRouterIP cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localEgressRouterIP);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"createRouteDirection", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"createRouteDirection");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("createRouteDirection");
                                    }
                                

                                          if (localCreateRouteDirection==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("createRouteDirection cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localCreateRouteDirection);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                              if (localProtocolTracker){
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"protocol", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"protocol");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("protocol");
                                    }
                                

                                          if (localProtocol==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("protocol cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localProtocol);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSrcPortTracker){
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"srcPort", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"srcPort");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("srcPort");
                                    }
                                
                                       xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSrcPort));
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDestPortTracker){
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"destPort", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"destPort");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("destPort");
                                    }
                                
                                       xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDestPort));
                                    
                                   xmlWriter.writeEndElement();
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
                                                                      "srcHost"));
                            
                                        if (localSrcHost != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSrcHost));
                                        } else {
                                           throw new RuntimeException("srcHost cannot be null!!");
                                        }
                                    
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "destHost"));
                            
                                        if (localDestHost != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDestHost));
                                        } else {
                                           throw new RuntimeException("destHost cannot be null!!");
                                        }
                                    
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "startTime"));
                            
                                        if (localStartTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStartTime));
                                        } else {
                                           throw new RuntimeException("startTime cannot be null!!");
                                        }
                                    
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "endTime"));
                            
                                        if (localEndTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEndTime));
                                        } else {
                                           throw new RuntimeException("endTime cannot be null!!");
                                        }
                                    
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "bandwidth"));
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBandwidth));
                            
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "burstLimit"));
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBurstLimit));
                            
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "description"));
                            
                                        if (localDescription != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDescription));
                                        } else {
                                           throw new RuntimeException("description cannot be null!!");
                                        }
                                     if (localIngressRouterIPTracker){
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "ingressRouterIP"));
                            
                                        if (localIngressRouterIP != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIngressRouterIP));
                                        } else {
                                           throw new RuntimeException("ingressRouterIP cannot be null!!");
                                        }
                                    } if (localEgressRouterIPTracker){
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "egressRouterIP"));
                            
                                        if (localEgressRouterIP != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEgressRouterIP));
                                        } else {
                                           throw new RuntimeException("egressRouterIP cannot be null!!");
                                        }
                                    }
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "createRouteDirection"));
                            
                                        if (localCreateRouteDirection != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCreateRouteDirection));
                                        } else {
                                           throw new RuntimeException("createRouteDirection cannot be null!!");
                                        }
                                     if (localProtocolTracker){
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "protocol"));
                            
                                        if (localProtocol != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localProtocol));
                                        } else {
                                           throw new RuntimeException("protocol cannot be null!!");
                                        }
                                    } if (localSrcPortTracker){
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "srcPort"));
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSrcPort));
                            } if (localDestPortTracker){
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "destPort"));
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDestPort));
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
        public static ResCreateContent parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ResCreateContent object = new ResCreateContent();
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
                    if (!"resCreateContent".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (ResCreateContent)net.es.oscars.wsdlTypes.ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","srcHost").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSrcHost(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","destHost").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDestHost(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","startTime").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStartTime(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","endTime").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEndTime(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","bandwidth").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setBandwidth(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","burstLimit").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setBurstLimit(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","description").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDescription(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","ingressRouterIP").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIngressRouterIP(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","egressRouterIP").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEgressRouterIP(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","createRouteDirection").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCreateRouteDirection(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","protocol").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setProtocol(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","srcPort").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSrcPort(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","destPort").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDestPort(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
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
           
          