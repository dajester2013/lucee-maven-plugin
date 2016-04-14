package org.jdsnet.maven.lucee.war;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.jdsnet.maven.lucee.AbstractLuceeMojo;

@Mojo(name = "lucee-war-dependencies", requiresDependencyCollection = ResolutionScope.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class LuceeWarDependenciesMojo extends AbstractLuceeMojo {

	@Parameter(defaultValue="${project.build.directory}/${project.build.finalName}/WEB-INF/lucee/deploy")
	private File luceeDependencyOutputDir;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (project.getPackaging().equals("war")) {
			Log log = getLog();
			Artifact lastArtifact = null;
			log.info("Copying LAR/LEX dependencies: ");
			log.debug("  Dependency Output Directory: "+luceeDependencyOutputDir.toString());
			for (Artifact a : project.getArtifacts()) {
				lastArtifact = a;
				log.debug("    * " + a.getFile().toString());
				if (a.getType().equals("lar") || a.getType().equals("lex")) {
					try {
						FileUtils.copyFileToDirectory(a.getFile(), luceeDependencyOutputDir);
					} catch (IOException e) {
						String errorMessage = "Error copying dependency" + (lastArtifact != null ? " "+lastArtifact.toString() : "");
						if (getFailOnError())
							throw new MojoFailureException(errorMessage, e);
						else
							log.warn(errorMessage);
					}
				}
			}
		}
	}

}
