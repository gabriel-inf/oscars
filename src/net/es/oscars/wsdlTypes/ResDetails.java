
            /**
            * ResDetails.java
            *
            * This file was auto-generated from WSDL
            * by the Apache Axis2 version: #axisVersion# #today#
            */

            package net.es.oscars.wsdlTypes;
            /**
            *  ResDetails bean class
            */
        
        public  class ResDetails
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = resDetails
                Namespace URI = http://oscars.es.net/OSCARS
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for Info
                        */

                        protected net.es.oscars.wsdlTypes.ResSummary localInfo ;
                        

                           /**
                           * Auto generated getter method
                           * @return net.es.oscars.wsdlTypes.ResSummary
                           */
                           public  net.es.oscars.wsdlTypes.ResSummary getInfo(){
                               return localInfo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Info
                               */
                               public void setInfo(net.es.oscars.wsdlTypes.ResSummary param){
                            
                                    this.localInfo=param;
                            

                               }
                            

                        /**
                        * field for CreateTime
                        */

                        protected long localCreateTime ;
                        

                           /**
                           * Auto generated getter method
                           * @return long
                           */
                           public  long getCreateTime(){
                               return localCreateTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CreateTime
                               */
                               public void setCreateTime(long param){
                            
                                    this.localCreateTime=param;
                            

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
                        * field for ResClass
                        */

                        protected java.lang.String localResClass ;
                        

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getResClass(){
                               return localResClass;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ResClass
                               */
                               public void setResClass(java.lang.String param){
                            
                                    this.localResClass=param;
                            

                               }
                            

                        /**
                        * field for Path
                        */

                        protected net.es.oscars.wsdlTypes.ExplicitPath localPath ;
                        

                           /**
                           * Auto generated getter method
                           * @return net.es.oscars.wsdlTypes.ExplicitPath
                           */
                           public  net.es.oscars.wsdlTypes.ExplicitPath getPath(){
                               return localPath;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Path
                               */
                               public void setPath(net.es.oscars.wsdlTypes.ExplicitPath param){
                            
                                    this.localPath=param;
                            

                               }
                            

                        /**
                        * field for Vtag
                        */

                        protected java.lang.String localVtag ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localVtagTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getVtag(){
                               return localVtag;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Vtag
                               */
                               public void setVtag(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localVtagTracker = true;
                                       } else {
                                          localVtagTracker = false;
                                              
                                       }
                                   
                                    this.localVtag=param;
                            

                               }
                            

                        /**
                        * field for SrcPortId
                        */

                        protected net.es.oscars.wsdlTypes.PortID localSrcPortId ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSrcPortIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return net.es.oscars.wsdlTypes.PortID
                           */
                           public  net.es.oscars.wsdlTypes.PortID getSrcPortId(){
                               return localSrcPortId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SrcPortId
                               */
                               public void setSrcPortId(net.es.oscars.wsdlTypes.PortID param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localSrcPortIdTracker = true;
                                       } else {
                                          localSrcPortIdTracker = false;
                                              
                                       }
                                   
                                    this.localSrcPortId=param;
                            

                               }
                            

                        /**
                        * field for DestPortId
                        */

                        protected net.es.oscars.wsdlTypes.PortID localDestPortId ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDestPortIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return net.es.oscars.wsdlTypes.PortID
                           */
                           public  net.es.oscars.wsdlTypes.PortID getDestPortId(){
                               return localDestPortId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DestPortId
                               */
                               public void setDestPortId(net.es.oscars.wsdlTypes.PortID param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDestPortIdTracker = true;
                                       } else {
                                          localDestPortIdTracker = false;
                                              
                                       }
                                   
                                    this.localDestPortId=param;
                            

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
                        * field for SrcIpPort
                        */

                        protected int localSrcIpPort ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSrcIpPortTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getSrcIpPort(){
                               return localSrcIpPort;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SrcIpPort
                               */
                               public void setSrcIpPort(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localSrcIpPortTracker = true;
                                   
                                    this.localSrcIpPort=param;
                            

                               }
                            

                        /**
                        * field for DestIpPort
                        */

                        protected int localDestIpPort ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDestIpPortTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getDestIpPort(){
                               return localDestIpPort;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DestIpPort
                               */
                               public void setDestIpPort(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localDestIpPortTracker = true;
                                   
                                    this.localDestIpPort=param;
                            

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

                
               
                                    if (localInfo==null){
                                         throw new RuntimeException("info cannot be null!!");
                                    }
                                   localInfo.getOMDataSource(
                                       new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","info"),
                                       factory).serialize(xmlWriter);
                                
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"createTime", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"createTime");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("createTime");
                                    }
                                
                                       xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCreateTime));
                                    
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

                                            xmlWriter.writeStartElement(prefix,"resClass", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"resClass");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("resClass");
                                    }
                                

                                          if (localResClass==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("resClass cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localResClass);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    if (localPath==null){
                                         throw new RuntimeException("path cannot be null!!");
                                    }
                                   localPath.getOMDataSource(
                                       new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","path"),
                                       factory).serialize(xmlWriter);
                                 if (localVtagTracker){
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"vtag", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"vtag");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("vtag");
                                    }
                                

                                          if (localVtag==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("vtag cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localVtag);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSrcPortIdTracker){
                                    if (localSrcPortId==null){
                                         throw new RuntimeException("srcPortId cannot be null!!");
                                    }
                                   localSrcPortId.getOMDataSource(
                                       new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","srcPortId"),
                                       factory).serialize(xmlWriter);
                                } if (localDestPortIdTracker){
                                    if (localDestPortId==null){
                                         throw new RuntimeException("destPortId cannot be null!!");
                                    }
                                   localDestPortId.getOMDataSource(
                                       new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","destPortId"),
                                       factory).serialize(xmlWriter);
                                }
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
                             } if (localSrcIpPortTracker){
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"srcIpPort", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"srcIpPort");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("srcIpPort");
                                    }
                                
                                       xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSrcIpPort));
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDestIpPortTracker){
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"destIpPort", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"destIpPort");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("destIpPort");
                                    }
                                
                                       xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDestIpPort));
                                    
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
                                                                      "info"));
                            
                            
                                    if (localInfo==null){
                                         throw new RuntimeException("info cannot be null!!");
                                    }
                                    elementList.add(localInfo);
                                
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "createTime"));
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCreateTime));
                            
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "bandwidth"));
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBandwidth));
                            
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "burstLimit"));
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localBurstLimit));
                            
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "resClass"));
                            
                                        if (localResClass != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localResClass));
                                        } else {
                                           throw new RuntimeException("resClass cannot be null!!");
                                        }
                                    
                            elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "path"));
                            
                            
                                    if (localPath==null){
                                         throw new RuntimeException("path cannot be null!!");
                                    }
                                    elementList.add(localPath);
                                 if (localVtagTracker){
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "vtag"));
                            
                                        if (localVtag != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVtag));
                                        } else {
                                           throw new RuntimeException("vtag cannot be null!!");
                                        }
                                    } if (localSrcPortIdTracker){
                            elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "srcPortId"));
                            
                            
                                    if (localSrcPortId==null){
                                         throw new RuntimeException("srcPortId cannot be null!!");
                                    }
                                    elementList.add(localSrcPortId);
                                } if (localDestPortIdTracker){
                            elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "destPortId"));
                            
                            
                                    if (localDestPortId==null){
                                         throw new RuntimeException("destPortId cannot be null!!");
                                    }
                                    elementList.add(localDestPortId);
                                }
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "description"));
                            
                                        if (localDescription != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDescription));
                                        } else {
                                           throw new RuntimeException("description cannot be null!!");
                                        }
                                     if (localProtocolTracker){
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "protocol"));
                            
                                        if (localProtocol != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localProtocol));
                                        } else {
                                           throw new RuntimeException("protocol cannot be null!!");
                                        }
                                    } if (localSrcIpPortTracker){
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "srcIpPort"));
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSrcIpPort));
                            } if (localDestIpPortTracker){
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "destIpPort"));
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDestIpPort));
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
        public static ResDetails parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ResDetails object = new ResDetails();
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
                    if (!"resDetails".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (ResDetails)net.es.oscars.wsdlTypes.ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","info").equals(reader.getName())){
                                
                                        object.setInfo(net.es.oscars.wsdlTypes.ResSummary.Factory.parse(reader));
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","createTime").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCreateTime(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToLong(content));
                                              
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","resClass").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setResClass(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","path").equals(reader.getName())){
                                
                                        object.setPath(net.es.oscars.wsdlTypes.ExplicitPath.Factory.parse(reader));
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","vtag").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setVtag(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","srcPortId").equals(reader.getName())){
                                
                                        object.setSrcPortId(net.es.oscars.wsdlTypes.PortID.Factory.parse(reader));
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","destPortId").equals(reader.getName())){
                                
                                        object.setDestPortId(net.es.oscars.wsdlTypes.PortID.Factory.parse(reader));
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","protocol").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setProtocol(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","srcIpPort").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSrcIpPort(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","destIpPort").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDestIpPort(
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
           
          