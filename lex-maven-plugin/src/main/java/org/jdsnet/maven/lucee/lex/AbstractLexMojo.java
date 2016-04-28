package org.jdsnet.maven.lucee.lex;

import java.io.File;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

public abstract class AbstractLexMojo extends AbstractMojo {
	/**
	 * A classifier to attach to the artifact
	 */
	@Parameter
	private String classifier;
	@Parameter(defaultValue="${project}", readonly=true, required=true)
	
	/**
	 * Import the project
	 */
	private MavenProject project;

	/**
	 * Import the project helper
	 */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Import the {@link MavenSession}.
     */
    @Parameter(defaultValue="${session}", readonly=true, required=true)
    private MavenSession session;
    
	/**
	 * Import the list of resources configured in the project.
	 */
	@Parameter( defaultValue = "${project.resources}", required = true, readonly = true )
	private List<Resource> resources;

	/**
	 * Where to place the extension sources during the build process.
	 */
	@Parameter(defaultValue="src/main/extension")
	private File extensionSourceDir;

	/**
	 * Where to place the extension sources during the build process.
	 */
	@Parameter(defaultValue="${project.build.directory}/extension")
	private File extensionStagingDir;
	
	/**
	 * Directory containing the generated archive.
	 */
	@Parameter(defaultValue = "${project.build.directory}", required = true)
	private File outputDirectory;
	
	
	public String getClassifier() {
		return classifier;
	}
	
	public MavenProject getProject() {
		return project;
	}

	public MavenProjectHelper getProjectHelper() {
		return projectHelper;
	}

	protected MavenSession getSession() {
		return session;
	}
	
	protected List<Resource> getResources() {
		return resources;
	}
	
	protected File getExtensionSourceDir() {
		return extensionSourceDir;
	}
	
	protected File getExtensionStagingDir() {
		return extensionStagingDir;
	}
	
	protected File getOutputDirectory() {
		return outputDirectory;
	}
	
}
