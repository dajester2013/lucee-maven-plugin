<cfscript>
	try {
		server.password = "testing";
		cfadmin(type="web",		action="updatePassword", newPassword="#server.password#");
		cfadmin(type="server",	action="updatePassword", newPassword="#server.password#");

		var y = "yes";
		cfadmin(type="server",	action="updateDefaultSecurityManager",
								access_read="open",		access_write="protected",
								cache=y,				cfx_setting=y,
								cfx_usage=y,			custom_tag=y,
								datasource=y,			debugging=y,
								direct_java_access=y,	file=y,
								gateway=y,				mail=y,
								mapping=y,				orm=y,
								remote=y,				scheduled_task=y,
								search=y,				setting=y,
								tag_execute=y,			tag_import=y,
								tag_object=y,			tag_registry=y,
								password="#server.password#");
	} catch(any e) {}

	function _toArray(String src) { //cflint ignore:line
		if (!src.len()) return [];

		if (src.startsWith("[")) {
			return deserializeJSON(src);
		} else {
			return src.listToArray();
		}
	}

	htmlReport = "";

	try {
		param name="url.directory" default="";
		param name="url.recurse" default=true;
		param name="url.reportpath" default="";
		param name="url.codecoverage" default=false;
		param name="url.codecoveragepath" default="";

		param name="url.bundles" default="";
		param name="url.suites" default="";
		param name="url.specs" default="";
		param name="url.labels" default="";

		testbox = new testbox.system.TestBox(
			options={
				"coverage": {
					"enabled"		: !(url.bundles.len()||url.suites.len()||url.specs.len()) && url.codecoverage,
					"pathToCapture"	: url.codecoveragepath,

					"sonarQube"   	: {
						"XMLOutputPath" : (url.codecoveragereportpath ?: url.reportpath) & "/sonarqube-coverage.xml"
					},
					
					"browser" : {
						"outputDir" : (url.codecoveragereportpath ?: url.reportpath) & "/browser"
					}
				}
			}
		);

		runArgs = {
			directory: url.directory,
			recurse: url.recurse
		};

		if (url.bundles.len()) {
			testpkg = url.directory.replaceAll("^/|/$","").replaceAll("/",".") & ".";
			runArgs.testBundles = _toArray(bundles).map((b) => b.startsWith(testpkg) ? b : "#testpkg##b#");
		}
		if (url.suites.len()) {
			runArgs.testSuites = _toArray(suites);
		}
		if (url.specs.len()) {
			runArgs.testSpecs = _toArray(specs);
		}
		if (url.labels.len()) {
			runArgs.labels = _toArray(labels);
		}

		systemOutput(serializeJson(runArgs),true);

		results = testbox.runRaw(argumentCollection=runArgs);

		new testbox.system.reports.ConsoleReporter().runReport(results, testbox, {});

		htmlReport = new testbox.system.reports.SimpleReporter().runReport(results, testbox, {}, true);

		junitReport = new testbox.system.reports.ANTJUnitReporter().runReport(results,testbox, {});
		junitReport = xmlParse(junitReport);

		if (url.reportpath.len()) {
			if (!directoryExists(url.reportpath))
				directoryCreate(url.reportpath, true, true);

			fileWrite(url.reportpath & "/testReport.html", htmlReport);
			
			for (suite in junitReport.testsuites.XMLChildren) {
				fileWrite(url.reportpath & "/TEST-#suite.XMLAttributes.package#.xml", toString(suite));
			}
		}

		
	} catch(any e) {
		systemOutput("Error running tests: #e.message#",true);
		systemOutput(serializeJSON(e),true);
		throw e;
	} finally {
		cfheader(
			name="x-test-result-status", 
			value="#results.getTotalError() ? -1 : (results.getTotalFail() ? 0 : 1)#"
		);
	}
</cfscript>