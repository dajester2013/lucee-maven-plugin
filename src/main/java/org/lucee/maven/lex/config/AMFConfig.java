package org.lucee.maven.lex.config;

import org.apache.maven.plugins.annotations.Parameter;

public class AMFConfig {
	
	@Parameter(required=true)
	private String caster;

	@Parameter(required=true)
	private String configuration;
	
	
	@Parameter(required=true)
	private String className;
	
	@Parameter
	private String bundleName=null;
	
	@Parameter
	private String bundleVersion=null;

}
