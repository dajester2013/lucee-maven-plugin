package org.jdsnet.maven.lucee.war;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

@Mojo(name = "lucee-war-dependencies", requiresDependencyCollection = ResolutionScope.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class LuceeWarDependenciesMojo extends AbstractMojo {

	@Parameter(defaultValue="${project.build.directory}/${project.build.finalName}/WEB-INF/lucee/deploy")
	private File luceeDependencyOutputDir;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (project.getPackaging().equals("war")) {
			try {
				for (Artifact a : project.getArtifacts()) {
					if (a.getType().equals("lar") || a.getType().equals("lex")) {
						FileUtils.copyFileToDirectory(a.getFile(), luceeDependencyOutputDir);
					}
				}
			} catch (IOException e) {
				throw new MojoExecutionException("Error copying dependency", e);
			}
		}
	}

}
