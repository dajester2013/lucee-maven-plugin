package org.jdsnet.maven.lucee.lar.util;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

public class CompileTimeMapping {

	@Parameter
	private String mapping;
	
	@Parameter
	private File path;
	
	
	public String getMapping() {
		return mapping;
	}
	
	public File getPath() {
		return path;
	}
	
}
