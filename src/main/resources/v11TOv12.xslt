<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:old="http://www.omg.org/spec/DMN/20151101/dmn.xsd"
xmlns:triso="http://www.trisotech.com/2015/triso/modeling"
xmlns:drools="http://www.drools.org/kie/dmn/1.1"
 >
<xsl:output method="xml" indent="yes"/>

<xsl:variable name="feelprefix" select="name(old:definitions/namespace::*[. = 'http://www.omg.org/spec/FEEL/20140401'])"/>
<xsl:variable name="feelprefixcolumn" select="concat($feelprefix, ':')" />

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="@triso:logoChoice" />

<xsl:template match="old:definitions/old:itemDefinition/old:typeRef/text()">
   <xsl:value-of select="replace(., $feelprefixcolumn, '')"/>
</xsl:template>

<xsl:template match="old:*/@typeRef">
    <xsl:attribute name="typeRef">
        <xsl:value-of select="replace(., $feelprefixcolumn, '')"/>
    </xsl:attribute>
</xsl:template>

<xsl:template match="old:*/@drools:kind">
    <xsl:attribute name="kind">
    <xsl:choose>
        <xsl:when test=". = 'F' "><xsl:value-of select="'FEEL'"/></xsl:when>
        <xsl:when test=". = 'J' "><xsl:value-of select="'Java'"/></xsl:when>
        <xsl:when test=". = 'P' "><xsl:value-of select="'PMML'"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="'FEEL'"/></xsl:otherwise>
    </xsl:choose>
    </xsl:attribute>
</xsl:template>

<xsl:template match="old:*">
  <xsl:element name="semantic:{local-name()}" namespace="http://www.omg.org/spec/DMN/20180521/MODEL/" >
<xsl:namespace name="feel" select="'http://www.omg.org/spec/DMN/20180521/FEEL/'"/>
<xsl:namespace name="dmndi" select="'http://www.omg.org/spec/DMN/20180521/DMNDI/'"/>
<xsl:namespace name="di" select="'http://www.omg.org/spec/DMN/20180521/DI/'"/>
<xsl:namespace name="dc" select="'http://www.omg.org/spec/DMN/20180521/DC/'"/>
           
   <xsl:for-each select="namespace::*">
<xsl:choose>
    <xsl:when test="name() = 'trisofeed' " />
    <xsl:when test="name() = 'semantic' " />
    <xsl:when test=". = 'http://www.trisotech.com/2015/triso/modeling' " />
    <xsl:when test=". = 'http://www.omg.org/spec/FEEL/20140401' "/>
    <xsl:when test=". = 'http://www.drools.org/kie/dmn/1.1' "/> <!-- functionDefintion @kind is transformed above -->
    <xsl:when test="name()">
        <xsl:copy-of select="."/>
    </xsl:when>
    <xsl:otherwise>
        <xsl:copy-of select="."/>
    </xsl:otherwise>
</xsl:choose>    
   </xsl:for-each>
   
    <xsl:apply-templates select="@*|node()"/>
  </xsl:element>
</xsl:template>
</xsl:stylesheet>
