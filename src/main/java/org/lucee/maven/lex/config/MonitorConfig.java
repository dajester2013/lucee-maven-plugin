package org.lucee.maven.lex.config;

import org.apache.maven.plugins.annotations.Parameter;

public class MonitorConfig implements Config {

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

	public String serializeJSON() {
		return		"{"
				+	 "\"type\":\"" + type + "\""
				+ 	",\"name\":\""+name+"\""
				
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
