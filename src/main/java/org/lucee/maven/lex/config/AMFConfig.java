package org.lucee.maven.lex.config;

import org.apache.maven.plugins.annotations.Parameter;

public class AMFConfig implements Config {
	
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

	public String serializeJSON() {
		return		"{"
				+	 "\"caster\":\"" + caster + "\""
				+ 	",\"configuration\":\""+configuration+"\""
				+ 	",\"className\":\""+className+"\""
				
				+ (
					bundleName != null 
						?		",\"bundle-name\":\""+className+"\""
							+	",\"bundleName\":\""+className+"\""
						:	""
				)
				+	"}"
				;
	}

}