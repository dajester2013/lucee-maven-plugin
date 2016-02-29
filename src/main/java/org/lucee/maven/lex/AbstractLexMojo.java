package org.lucee.maven.lex;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractLexMojo extends AbstractMojo {

	/**
	 * Import the {@link MavenProject}.
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

    /**
     * Import the {@link MavenSession}.
     */
    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;
    
	/**
	 * Where to output the built extension.
	 */
	@Parameter(defaultValue="${project.build.directory}")
	private File outputDirectory;

	/**
	 * Where to place the extension sources during the build process.
	 */
	@Parameter(defaultValue="${project.build.directory}/extension")
	private File extensionDirectory;
	
	
	protected MavenProject getProject() {
		return project;
	}
	
	protected MavenSession getSession() {
		return session;
	}
	
	protected File getOutputDirectory() {
		return outputDirectory;
	}
	
	protected File getExtensionDirectory() {
		return extensionDirectory;
	}
	
}
