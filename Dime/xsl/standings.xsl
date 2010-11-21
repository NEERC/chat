<?xml version = "1.0" encoding="WINDOWS-1251"?>

<xsl:stylesheet 
    version         = "1.0" 
    xmlns:xsl       = "http://www.w3.org/1999/XSL/Transform"
    xmlns:locale    = "locale"
>
    <xsl:output method="html" indent="yes"  encoding="windows-1251"/>
    <xsl:strip-space elements="*"/>

    <xsl:param name="lang" select="'en'"/>
    <xsl:param name="show-time" select="''"/>
    <xsl:param name="party-width" select="27"/>
    <xsl:param name="teams-prefix" select="'russia-team-2007.main.S'"/>
    <xsl:variable name="locale" select="document('')/xsl:stylesheet/locale:data[@lang = $lang]/item"/>
    <xsl:variable name="teams" select="document('w:/school/teams.xml')/teams/team"/>

    <xsl:template match="standings">
        <xsl:apply-templates select="contest"/>
    </xsl:template>

    <xsl:template match="contest">      
        <xsl:for-each select="session">
            <xsl:sort select="@solved" data-type="number" order="ascending"/>
            <xsl:sort select="@penalty" data-type="number" order="descending"/>

            <xsl:variable name="session-problems" select="problem"/>
            <xsl:variable name="cursolved" select="@solved"/>
            <xsl:variable name="unique">
                <xsl:choose>
                    <xsl:when test="count(following-sibling::session[@solved = $cursolved]) = 0">1</xsl:when>
                    <xsl:otherwise>0</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="solved" select="count(following-sibling::session[not(following-sibling::session/@solved = @solved)])+$unique"/>

            <xsl:variable name="color">
                <xsl:choose>
                    <xsl:when test="($solved mod 2 = 1) and (position() mod 2 = 0)">f0f0f0</xsl:when>
                    <xsl:when test="($solved mod 2 = 1) and (position() mod 2 = 1)">ffffff</xsl:when>
                    <xsl:when test="($solved mod 2 = 0) and (position() mod 2 = 1)">d0f0ff</xsl:when>
                    <xsl:when test="($solved mod 2 = 0) and (position() mod 2 = 0)">c0e0f0</xsl:when>
                </xsl:choose>
            </xsl:variable>

            <pre style="background: #{$color}; margin:0;">
                <xsl:text> </xsl:text>
                <xsl:call-template name="align-left">                    
                    <xsl:with-param name="string" select="substring-before(@party, ' (')"/>
                    <xsl:with-param name="width" select="$party-width"/>
                </xsl:call-template>
                <xsl:text> </xsl:text>
                <xsl:call-template name="align-right">                    
                    <xsl:with-param name="string" select="string(@solved)"/>
                    <xsl:with-param name="width" select="2"/>
                </xsl:call-template>
                <xsl:text> </xsl:text>
                <xsl:call-template name="align-right">                    
                    <xsl:with-param name="string" select="string(@penalty)"/>
                    <xsl:with-param name="width" select="5"/>
                </xsl:call-template>
                <xsl:text> </xsl:text>
                <xsl:variable name="penalty" select="@penalty"/>
                <xsl:call-template name="align-right">                    
                    <xsl:with-param name="string" select="string(count(../*) - (position()-count(preceding::session[@solved=$solved and @penalty=$penalty])))"/>
                    <xsl:with-param name="width" select="3"/>
                </xsl:call-template>
                <xsl:text> </xsl:text>
            </pre>
            <xsl:text>&#10;</xsl:text>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="align-left">
        <xsl:param name="string"/>
        <xsl:param name="width"/>

        <xsl:choose>
            <xsl:when test="string-length($string) &gt;= $width">
                <xsl:value-of select="$string"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="align-left">
                    <xsl:with-param name="string" select="$string"/>
                    <xsl:with-param name="width" select="$width - 1"/>
                </xsl:call-template>
                <xsl:text> </xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="align-right">
        <xsl:param name="string"/>
        <xsl:param name="width"/>

        <xsl:choose>
            <xsl:when test="string-length($string) &gt;= $width">
                <xsl:value-of select="$string"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text> </xsl:text>
                <xsl:call-template name="align-right">
                    <xsl:with-param name="string" select="$string"/>
                    <xsl:with-param name="width" select="$width - 1"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <locale:data lang="en">
        <item key="title">Standings</item>
        <item key="of">of</item>
        <item key="status">status: </item>
        <item key="frozen">, frozen</item>
        <item key="runs">runs:</item>
        <item key="accepted">accepted:</item>
        <item key="last-success">last success</item>
        <item key="team">Team</item>
        <item key="time">Time</item>
        <item key="rank">Rank</item>
        <item key="total-runs">Total runs</item>
        <item key="Accepted">Accepted</item>
        <item key="Rejected">Rejected</item>
        <item key="status-running">running</item>
        <item key="status-before">before</item>
        <item key="status-over">over</item>
        <item key="status-paused">paused</item>
    </locale:data>
</xsl:stylesheet>
