<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:variable name="java">@start java </xsl:variable>
<xsl:variable name="javaw">@start javaw </xsl:variable>
<xsl:variable name="p1">-Dserver.host=10.0.0.1 -Dusername=</xsl:variable>
<xsl:variable name="p2"> -Dpassword=</xsl:variable>
<xsl:variable name="p3"> -Dping=5 -Dconsole.encoding=CP866 -jar chat-client.jar</xsl:variable>
            

    <xsl:template match="/">
        <xsl:for-each-group select="Openfire/User" group-by="Username">
            <xsl:result-document href="chat-{Username}.bat" method="text">
                <xsl:copy>
                   <xsl:value-of select="$java"/>
                   <xsl:value-of select="$p1"/>
				   <xsl:value-of select="Username"/>
				   <xsl:value-of select="$p2"/>
				   <xsl:value-of select="Password"/>
				   <xsl:value-of select="$p3"/>
				</xsl:copy>
            </xsl:result-document>
            <xsl:result-document href="chat-{Username}-noconsole.bat" method="text">
           		<xsl:copy>
                   <xsl:value-of select="$javaw"/>
                   <xsl:value-of select="$p1"/>
				   <xsl:value-of select="Username"/>
				   <xsl:value-of select="$p2"/>
				   <xsl:value-of select="Password"/>
				   <xsl:value-of select="$p3"/>
				</xsl:copy>
            </xsl:result-document>
        </xsl:for-each-group>
    </xsl:template>
</xsl:stylesheet>
