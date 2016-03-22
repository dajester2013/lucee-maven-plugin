package org.jdsnet.maven.lucee;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

abstract public class AbstractLuceeMojo extends AbstractMojo {

	@Parameter
	private String classifier;

	@Parameter(defaultValue="${project}", readonly=true, required=true)
	private MavenProject project;

    @Component
    private MavenProjectHelper projectHelper;
    
	
	public String getClassifier() {
		return classifier;
	}
	
	public MavenProject getProject() {
		return project;
	}

	public MavenProjectHelper getProjectHelper() {
		return projectHelper;
	}
}
