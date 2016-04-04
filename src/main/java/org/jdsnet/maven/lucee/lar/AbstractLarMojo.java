package org.jdsnet.maven.lucee.lar;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;
import org.jdsnet.maven.lucee.AbstractLuceeMojo;

abstract public class AbstractLarMojo extends AbstractLuceeMojo {
	
	@Parameter(defaultValue="${project.build.directory}/lar")
	private File larOutputDirectory;
	
	public File getLarOutputDirectory() {
		return larOutputDirectory;
	}

}
