package org.jdsnet.maven.lucee.lar.util;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

public class CompileTimeMapping {

	@Parameter
	private String virtual;
	
	@Parameter
	private File physical;
	
	
	public String getVirtual() {
		return virtual;
	}
	
	public File getPhysical() {
		return physical;
	}
	
}
