package org.jdsnet.maven.lucee.lex;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jdsnet.maven.lucee.AbstractLuceeMojo;

public abstract class AbstractLexMojo extends AbstractLuceeMojo {

    
	/**
	 * Where to place the extension sources during the build process.
	 */
	@Parameter(defaultValue="${project.build.directory}/extension")
	private File extensionDirectory;
	
	
	protected File getExtensionDirectory() {
		return extensionDirectory;
	}
	
}
