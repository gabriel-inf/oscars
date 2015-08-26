<?xml version="1.0" encoding="UTF-8"?>
<CtrlPlane:topology xmlns:CtrlPlane="http://ogf.org/schema/network/topology/ctrlPlane/20080828/" id="${base.topologyId}">
  <CtrlPlane:idcId>${base.idcId}</CtrlPlane:idcId>
  <CtrlPlane:domain id="urn:ogf:network:domain=${base.domainId}">

  <#list devices as deviceSpec>
  <CtrlPlane:node id="urn:ogf:network:domain=${base.domainId}:node=${deviceSpec.name}">
    <CtrlPlane:address>${deviceSpec.address}</CtrlPlane:address>
<#list deviceSpec.ports as port>
    <CtrlPlane:port id="urn:ogf:network:domain=${base.domainId}:node=${deviceSpec.name}:port=${port.name}">
      <CtrlPlane:capacity>${port.capacity}</CtrlPlane:capacity>
      <CtrlPlane:maximumReservableCapacity>${port.maxResCap}</CtrlPlane:maximumReservableCapacity>
      <CtrlPlane:minimumReservableCapacity>${port.minResCap}</CtrlPlane:minimumReservableCapacity>
      <CtrlPlane:granularity>${port.granularity}</CtrlPlane:granularity>
<#list port.links as link>
<#assign isMpls = link.isMpls>
<#assign isCustomer = link.isCustomer>
        <CtrlPlane:link id="urn:ogf:network:domain=${base.domainId}:node=${deviceSpec.name}:port=${port.name}:link=${link.name}">
<#if isCustomer>
          <CtrlPlane:remoteLinkId>${link.remoteId}</CtrlPlane:remoteLinkId>
<#else>
          <CtrlPlane:remoteLinkId>urn:ogf:network:domain=${base.domainId}:node=${deviceSpec.name}:port=${port.name}:link=${link.name}_site</CtrlPlane:remoteLinkId>
</#if>
          <CtrlPlane:trafficEngineeringMetric>${link.metric?string.computer}</CtrlPlane:trafficEngineeringMetric>
          <CtrlPlane:SwitchingCapabilityDescriptors>
    <#if isMpls>
            <CtrlPlane:switchingcapType />
    <#else>
            <CtrlPlane:switchingcapType>psc-1</CtrlPlane:switchingcapType>
    </#if>
            <CtrlPlane:encodingType>packet</CtrlPlane:encodingType>
            <CtrlPlane:switchingCapabilitySpecificInfo>
    <#if isMpls>
            <CtrlPlane:capability>unimplemented</CtrlPlane:capability>
    <#else>
            <CtrlPlane:capability />
    </#if>
            <CtrlPlane:interfaceMTU>${link.mtu?string.computer}</CtrlPlane:interfaceMTU>
    <#if !isMpls>
            <CtrlPlane:vlanRangeAvailability>${link.vlanRange}</CtrlPlane:vlanRangeAvailability>
    </#if>
            <CtrlPlane:vlanTranslation>${link.canTranslate}</CtrlPlane:vlanTranslation>
          </CtrlPlane:switchingCapabilitySpecificInfo>
        </CtrlPlane:SwitchingCapabilityDescriptors>
      </CtrlPlane:link>
</#list>
    </CtrlPlane:port>
</#list>
  </CtrlPlane:node>

  </#list>
  </CtrlPlane:domain>
</CtrlPlane:topology>