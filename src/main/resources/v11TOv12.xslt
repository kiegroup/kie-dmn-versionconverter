<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:old="http://www.omg.org/spec/DMN/20151101/dmn.xsd"
xmlns:triso="http://www.trisotech.com/2015/triso/modeling"
xmlns:drools="http://www.drools.org/kie/dmn/1.1"
 >
<!--
  ~ Copyright 2020 Red Hat, Inc. and/or its affiliates.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~       http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
<xsl:output method="xml" indent="yes"/>

<xsl:variable name="feelprefix" select="name(old:definitions/namespace::*[. = 'http://www.omg.org/spec/FEEL/20140401'])"/>
<xsl:variable name="feelprefixcolumn" select="concat($feelprefix, ':')" />

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="@triso:logoChoice" />

<xsl:template match="old:definitions/old:extensionElements/drools:decisionServices" />
<xsl:template match="*/old:businessKnowledgeModel/old:variable" />

<xsl:template match="old:definitions/old:itemDefinition/old:typeRef/text()">
   <xsl:value-of select="replace(replace(., $feelprefixcolumn, ''), '^.*:', '')"/>
</xsl:template>

<xsl:template match="*/old:itemComponent/old:typeRef/text()">
<xsl:value-of select="replace(replace(., $feelprefixcolumn, ''), '^.*:', '')"/>
</xsl:template>

<xsl:template match="old:*/@typeRef">
    <xsl:attribute name="typeRef">
      <xsl:value-of select="replace(replace(., $feelprefixcolumn, ''), '^.*:', '')"/>
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
  <xsl:variable name="defns"><xsl:value-of select="@namespace"/></xsl:variable>
  <xsl:element name="semantic:{local-name()}" namespace="http://www.omg.org/spec/DMN/20180521/MODEL/" >
    <xsl:namespace name="feel" select="'http://www.omg.org/spec/DMN/20180521/FEEL/'"/>
    <xsl:namespace name="dmndi" select="'http://www.omg.org/spec/DMN/20180521/DMNDI/'"/>
    <xsl:namespace name="di" select="'http://www.omg.org/spec/DMN/20180521/DI/'"/>
    <xsl:namespace name="dc" select="'http://www.omg.org/spec/DMN/20180521/DC/'"/>
    <xsl:if test="local-name()='definitions'">
      <xsl:namespace name="" select="$defns"/>
    </xsl:if>
           
    <xsl:for-each select="namespace::*">
      <xsl:choose>
          <xsl:when test="name() = 'trisofeed' " />
          <xsl:when test="name() = 'semantic' " />
          <xsl:when test=". = 'http://www.omg.org/spec/DMN/20151101/dmn.xsd' " />
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
   
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates select="old:description"/>
    <xsl:apply-templates select="old:extensionElements"/>
    <xsl:if test="local-name()='businessKnowledgeModel'"> <!--need copy BKM variable as first element -->
      <xsl:for-each select="old:variable">
        <xsl:element name="semantic:variable" namespace="http://www.omg.org/spec/DMN/20180521/MODEL/" >
          <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
      </xsl:for-each>
    </xsl:if>
    <xsl:apply-templates select="node()[not(local-name()='description' or local-name()='extensionElements')]"/>
    <xsl:for-each select="old:extensionElements/drools:decisionServices/old:decisionService">
      <!-- the above can only be a valid relative path IFF we are the 'definitions' element, we make a copy here of the DS elements in the new ns -->
      <xsl:variable name="dsname"><xsl:value-of select="@name"/></xsl:variable>
      <xsl:element name="semantic:decisionService" namespace="http://www.omg.org/spec/DMN/20180521/MODEL/" >
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates select="old:description"/>
        <xsl:apply-templates select="old:extensionElements"/>
        <xsl:element name="semantic:variable" namespace="http://www.omg.org/spec/DMN/20180521/MODEL/" >
          <xsl:attribute name="name"><xsl:value-of select="$dsname"/></xsl:attribute>
          <xsl:attribute name="typeRef">Any</xsl:attribute>
        </xsl:element>
        <xsl:apply-templates select="node()[not(local-name()='description' or local-name()='extensionElements')]"/>
      </xsl:element>
    </xsl:for-each>
  </xsl:element>
</xsl:template>

</xsl:stylesheet>
