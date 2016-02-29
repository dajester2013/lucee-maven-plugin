package org.lucee.maven.lex.packaging;

import java.io.File;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.lucee.maven.lex.ExtensionType;

public class PackagingContext {
	
	private final MavenProject project;
	private final MavenSession session;

	private final File outputDirectory;
	private final ExtensionType installTarget;
	
	public PackagingContext(MavenProject project, MavenSession session, File outputDirectory, ExtensionType installTarget) {
		
		this.project = project;
		this.session = session;
		this.outputDirectory = outputDirectory;
		this.installTarget = installTarget;

	}
	
	public MavenProject getProject() {return project;}
	public MavenSession getSession() {return session;}
	public Set<?> getArtifacts() {return project.getArtifacts();}
	public File getOutputDirectory() {return outputDirectory;}
	public ExtensionType getInstallTarget() {return installTarget;}
	
}
