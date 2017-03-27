package org.jdsnet.maven.lucee.lar;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

abstract public class AbstractLarMojo extends AbstractMojo {

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
	 * Where to output processed CFML files.
	 */
	@Parameter(defaultValue="${project.build.directory}/cfml", required=true)
	private File cfmlOutputDir;
	

	@Parameter(defaultValue="false")
	private boolean failOnError;
	

	
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
	
	protected File getCFMLOutputDir() {
		return cfmlOutputDir;
	}
	
}
