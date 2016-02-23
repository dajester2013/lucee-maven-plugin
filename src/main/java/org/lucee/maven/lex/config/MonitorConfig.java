package org.lucee.maven.lex.config;

import org.apache.maven.plugins.annotations.Parameter;

public class MonitorConfig {

	@Parameter(required=true)
	private String type;

	@Parameter(required=true)
	private String name;
	
	@Parameter(required=true)
	private String className;
	
	@Parameter
	private String bundleName=null;
	
	@Parameter
	private String bundleVersion=null;

}
