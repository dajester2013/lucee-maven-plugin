package org.jdsnet.maven.lucee.lex.config;

import org.apache.maven.plugins.annotations.Parameter;

public class EventGatewayConfig implements Config {
	
	@Parameter(required=true)
	private String id;
	
	@Parameter(required=true)
	private String className;
	
	@Parameter
	private String bundleName=null;
	
	@Parameter
	private String bundleVersion=null;

	public String serializeJSON() {
		return		"{"
				+	 "\"id\":\"" + id + "\""
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
