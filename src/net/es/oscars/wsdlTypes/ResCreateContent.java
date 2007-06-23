
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

                        protected long localStartTime ;
                        

                           /**
                           * Auto generated getter method
                           * @return long
                           */
                           public  long getStartTime(){
                               return localStartTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StartTime
                               */
                               public void setStartTime(long param){
                            
                                    this.localStartTime=param;
                            

                               }
                            

                        /**
                        * field for EndTime
                        */

                        protected long localEndTime ;
                        

                           /**
                           * Auto generated getter method
                           * @return long
                           */
                           public  long getEndTime(){
                               return localEndTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param EndTime
                               */
                               public void setEndTime(long param){
                            
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
                        * field for IngressNodeIP
                        */

                        protected java.lang.String localIngressNodeIP ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIngressNodeIPTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIngressNodeIP(){
                               return localIngressNodeIP;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IngressNodeIP
                               */
                               public void setIngressNodeIP(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIngressNodeIPTracker = true;
                                       } else {
                                          localIngressNodeIPTracker = false;
                                              
                                       }
                                   
                                    this.localIngressNodeIP=param;
                            

                               }
                            

                        /**
                        * field for EgressNodeIP
                        */

                        protected java.lang.String localEgressNodeIP ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEgressNodeIPTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getEgressNodeIP(){
                               return localEgressNodeIP;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param EgressNodeIP
                               */
                               public void setEgressNodeIP(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localEgressNodeIPTracker = true;
                                       } else {
                                          localEgressNodeIPTracker = false;
                                              
                                       }
                                   
                                    this.localEgressNodeIP=param;
                            

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
                        * field for ReqPath
                        */

                        protected net.es.oscars.wsdlTypes.ExplicitPath localReqPath ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localReqPathTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return net.es.oscars.wsdlTypes.ExplicitPath
                           */
                           public  net.es.oscars.wsdlTypes.ExplicitPath getReqPath(){
                               return localReqPath;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReqPath
                               */
                               public void setReqPath(net.es.oscars.wsdlTypes.ExplicitPath param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localReqPathTracker = true;
                                       } else {
                                          localReqPathTracker = false;
                                              
                                       }
                                   
                                    this.localReqPath=param;
                            

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
                                
                                       xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStartTime));
                                    
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
                                
                                       xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEndTime));
                                    
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
                              if (localIngressNodeIPTracker){
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"ingressNodeIP", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"ingressNodeIP");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("ingressNodeIP");
                                    }
                                

                                          if (localIngressNodeIP==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("ingressNodeIP cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIngressNodeIP);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localEgressNodeIPTracker){
                                    namespace = "http://oscars.es.net/OSCARS";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"egressNodeIP", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"egressNodeIP");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("egressNodeIP");
                                    }
                                

                                          if (localEgressNodeIP==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("egressNodeIP cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localEgressNodeIP);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localVtagTracker){
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
                                } if (localReqPathTracker){
                                    if (localReqPath==null){
                                         throw new RuntimeException("reqPath cannot be null!!");
                                    }
                                   localReqPath.getOMDataSource(
                                       new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","reqPath"),
                                       factory).serialize(xmlWriter);
                                } if (localProtocolTracker){
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
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStartTime));
                            
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "endTime"));
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEndTime));
                            
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
                                     if (localIngressNodeIPTracker){
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "ingressNodeIP"));
                            
                                        if (localIngressNodeIP != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIngressNodeIP));
                                        } else {
                                           throw new RuntimeException("ingressNodeIP cannot be null!!");
                                        }
                                    } if (localEgressNodeIPTracker){
                             elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "egressNodeIP"));
                            
                                        if (localEgressNodeIP != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEgressNodeIP));
                                        } else {
                                           throw new RuntimeException("egressNodeIP cannot be null!!");
                                        }
                                    } if (localVtagTracker){
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
                                } if (localReqPathTracker){
                            elementList.add(new javax.xml.namespace.QName("http://oscars.es.net/OSCARS",
                                                                      "reqPath"));
                            
                            
                                    if (localReqPath==null){
                                         throw new RuntimeException("reqPath cannot be null!!");
                                    }
                                    elementList.add(localReqPath);
                                } if (localProtocolTracker){
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
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToLong(content));
                                              
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","ingressNodeIP").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIngressNodeIP(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","egressNodeIP").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEgressNodeIP(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://oscars.es.net/OSCARS","reqPath").equals(reader.getName())){
                                
                                        object.setReqPath(net.es.oscars.wsdlTypes.ExplicitPath.Factory.parse(reader));
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
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
           
          