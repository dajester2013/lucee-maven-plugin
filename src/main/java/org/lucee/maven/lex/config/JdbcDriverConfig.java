package org.lucee.maven.lex.config;

import org.apache.maven.plugins.annotations.Parameter;

public class JdbcDriverConfig implements Config {
	
	@Parameter(required=true)
	private String label;
	
	@Parameter(required=true)
	private String className;
	
	@Parameter
	private String bundleName=null;
	
	@Parameter
	private String bundleVersion=null;

	public String serializeJSON() {
		return		"{"
				+	 "\"label\":\"" + label + "\""
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
