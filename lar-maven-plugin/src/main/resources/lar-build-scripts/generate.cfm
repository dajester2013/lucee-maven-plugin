<cfsetting enablecfoutputonly="true">

<cfoutput>


==========================================================
== lar-maven-plugin / generate.cfm                      ==
== lucee version : #server.lucee.version##repeatString(' ', 37-len(tostring(server.lucee.version)))#==
==========================================================


</cfoutput>

<cfparam name="url.type" type="string" default="component">
<cfparam name="url.phys" type="string">
<cfparam name="url.virt" type="string">
<cfparam name="url.mappings" type="string" default="{}">
<cfparam name="url.larFile" type="string">
<cfparam name="url.includeSource" type="boolean" default="false">
<cfparam name="url.includeStatic" type="boolean" default="true">

<cftry>
	<cfadmin action="updatePassword" type="web" newPassword="password" />
	<cfcatch></cfcatch>
</cftry>

<cfoutput>Create source mapping</cfoutput>
<cfadmin	action		= "#url.type == "component" ? "updateComponentMapping" : "updateMapping"#"
			type		= "web"
			physical	= "#url.phys#"
			archive		= ""
			virtual		= "#url.virt#"
			password	= "password"
			primary		= "physical"
			inspect		= "true"
			/>

<cfset url.mappings = deserializeJson(url.mappings)>
<cfloop collection="#url.mappings#" item="mVirt">
	<cfoutput>Create additional mapping: #mVirt# -> #url.mappings[mVirt]#</cfoutput>
	<cfadmin	action		= "updateMapping"
				type		= "web"
				physical	= "#url.mappings[mVirt]#"
				archive		= ""
				virtual		= "#mVirt#"
				password	= "password"
				primary		= "physical"
				inspect		= "true"
				/>
</cfloop>

<cfoutput>Compile mapping</cfoutput>
<cfadmin	action			= "#url.type == "component" ? "createComponentArchive" : "createArchive"#"
			type			= "web"
			password		= "password"

			file			= "#url.larFile#.lar"
			virtual			= "#url.virt#"
			addCFMLFiles	= "#url.includeSource#"
			addNonCFMLFiles	= "#url.includeStatic#"
			append			= "false"
			/>



<cfoutput>Cleanup</cfoutput>
<cfadmin	action			= "#url.type == "component" ? "removeComponentMapping" : "removeMapping"#"
			type			= "web"
			virtual			= "#url.virt#"
			password		= "password"
			/>

<cfloop collection="#url.mappings#" item="mVirt">
	<cfadmin	action		= "removeMapping"
				type		= "web"
				virtual		= "#mVirt#"
				password	= "password"
				/>
</cfloop>