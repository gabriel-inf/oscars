<?xml version='1.0' encoding='UTF-8'?>
<message xmlns:nml="http://schemas.ogf.org/nml/2013/05/base#"
         xmlns="http://schemas.ogf.org/nsi/2013/09/messaging#"
         xmlns:nsi="http://schemas.ogf.org/nsi/2013/09/topology#">
    <body>
    <nsi:NSA
            id="${base.nsa}"
            version="${base.timestamp}">
        <nml:Location id="${location.id}">
            <nml:lat>${location.latitude}</nml:lat>
            <nml:long>${location.longitude}</nml:long>
        </nml:Location>
        <nsi:Service id="${service.id}">
            <nsi:link>${service.link}</nsi:link>
            <nsi:type>application/vnd.org.ogf.nsi.cs.v2+soap</nsi:type>
            <nsi:Relation type="http://schemas.ogf.org/nsi/2013/09/topology#providedBy">
                <nsi:NSA id="${base.nsa}"/>
            </nsi:Relation>
        </nsi:Service>
        <nml:Topology id="${topo.id}">
            <nml:name>${topo.name}</nml:name>

<#list nnis as nni>
            <nml:Relation type="http://schemas.ogf.org/nml/2013/05/base#hasOutboundPort">
                <nml:PortGroup id="${nni.outId}">
                    <nml:LabelGroup labeltype="http://schemas.ogf.org/nml/2012/10/ethernet#vlan">${nni.outVlans}</nml:LabelGroup>
                    <nml:Relation type="http://schemas.ogf.org/nml/2013/05/base#isAlias">
                        <nml:PortGroup id="${nni.inId}"/>
                    </nml:Relation>
                </nml:PortGroup>
            </nml:Relation>
            <nml:Relation type="http://schemas.ogf.org/nml/2013/05/base#hasInboundPort">
                <nml:PortGroup id="${nni.inId}">
                    <nml:LabelGroup labeltype="http://schemas.ogf.org/nml/2012/10/ethernet#vlan">${nni.inVlans}</nml:LabelGroup>
                    <nml:Relation type="http://schemas.ogf.org/nml/2013/05/base#isAlias">
                        <nml:PortGroup id="${nni.outId}"/>
                    </nml:Relation>
                </nml:PortGroup>
            </nml:Relation>
</#list>


<#list unis as uni>
            <nml:Relation type="http://schemas.ogf.org/nml/2013/05/base#hasOutboundPort">
                <nml:PortGroup id="${uni.outId}">
                    <nml:LabelGroup labeltype="http://schemas.ogf.org/nml/2012/10/ethernet#vlan">${uni.outVlans}</nml:LabelGroup>
                </nml:PortGroup>
            </nml:Relation>
            <nml:Relation type="http://schemas.ogf.org/nml/2013/05/base#hasInboundPort">
                <nml:PortGroup id="${uni.inId}">
                    <nml:LabelGroup labeltype="http://schemas.ogf.org/nml/2012/10/ethernet#vlan">${uni.inVlans}</nml:LabelGroup>
                </nml:PortGroup>
            </nml:Relation>
</#list>

        </nml:Topology>





<#list peers as peer>
    <nsi:Relation type="http://schemas.ogf.org/nsi/2013/09/topology#peersWith">
            <nsi:NSA id="${peer}"/>
        </nsi:Relation>
</#list>

    </nsi:NSA>
    </body>
</message>