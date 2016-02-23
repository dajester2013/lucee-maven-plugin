package org.lucee.maven.lex.config;

import org.apache.maven.plugins.annotations.Parameter;

public class ORMConfig {

	@Parameter(required=true)
	private String className;
	
	@Parameter
	private String bundleName=null;
	
	@Parameter
	private String bundleVersion=null;

}
