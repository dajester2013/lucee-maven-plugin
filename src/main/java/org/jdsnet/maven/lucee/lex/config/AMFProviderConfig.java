package org.jdsnet.maven.lucee.lex.config;

import org.apache.maven.plugins.annotations.Parameter;

public class AMFProviderConfig implements Config {
	
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
