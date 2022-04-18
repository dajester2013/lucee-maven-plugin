<cfscript>
	try {
		server.password = "testing";
		cfadmin(type="web",		action="updatePassword", newPassword="#server.password#");
		cfadmin(type="server",	action="updatePassword", newPassword="#server.password#");

		cfadmin(type="server",	action="updateDefaultSecurityManager",
								access_read="open",			access_write="protected",
								cache="yes",					cfx_setting="yes",
								cfx_usage="yes",				custom_tag="yes",
								datasource="yes",			debugging="yes",
								direct_java_access="yes",	file="yes",
								gateway="yes",				mail="yes",
								mapping="yes",				orm="yes",
								remote="yes",				scheduled_task="yes",
								search="yes",				setting="yes",
								tag_execute="yes",			tag_import="yes",
								tag_object="yes",			tag_registry="yes",
								password="#server.password#");
	} catch(any e) {}

	try {
		param name="url.directory";
		param name="url.recurse" default=true;
		param name="url.reportpath" default="";
		param name="url.codecoverage" default=false;
		param name="url.codecoveragepath" default="";
		// param name="url.codecoveragereportpath" default="";


		testbox = new testbox.system.TestBox(
			options={
				coverage: {
					enabled:url.codecoverage,
					pathToCapture: url.codecoveragepath,
					sonarQube     	: {
						XMLOutputPath : (url.codecoveragereportpath ?: url.reportpath) & "/sonarqube-coverage.xml"
					},
					browser			: {
						outputDir : (url.codecoveragereportpath ?: url.reportpath) & "/browser"
					}
				}
			}
		);
		results = testbox.runRaw(
			directory=url.directory
		);

		new testbox.system.reports.ConsoleReporter().runReport(results, testbox, {});
		new testbox.system.reports.SimpleReporter().runReport(results, testbox, {});
		
		junitReport = new testbox.system.reports.ANTJUnitReporter().runReport(results,testbox, {});
		junitReport = xmlParse(junitReport);

		if (url.reportpath.len()) {
			if (!directoryExists(url.reportpath))
				directoryCreate(url.reportpath, true, true);

			for (suite in junitReport.testsuites.XMLChildren) {
				fileWrite(url.reportpath & "/TEST-#suite.XMLAttributes.package#.xml", toString(suite));
			}
		}

		
	} catch(any e) {
		systemOutput("Error running tests: #e.message#",true);
		systemOutput(serializeJSON(e),true);
		throw e;
	} finally {
		if (!isNull(results)) {
			cfheader(
				name="x-test-result-status", 
				value="#results.getTotalError() ? -1 : (results.getTotalFail() ? 0 : 1)#"
			);
		}
	}
</cfscript>