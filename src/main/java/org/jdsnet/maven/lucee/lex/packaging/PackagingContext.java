package org.jdsnet.maven.lucee.lex.packaging;

import java.io.File;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.jdsnet.maven.lucee.lex.ExtensionType;

public class PackagingContext {
	
	private final MavenProject project;
	private final MavenSession session;

	private final File extensionDirectory;
	private final File outputDirectory;
	private final ExtensionType extensionType;
	
	public PackagingContext(MavenProject project, MavenSession session, File extensionDirectory, File outputDirectory, ExtensionType extensionType) {
		
		this.project = project;
		this.session = session;
		this.extensionDirectory = extensionDirectory;
		this.outputDirectory = outputDirectory;
		this.extensionType = extensionType;

	}
	
	public MavenProject getProject() {return project;}
	public MavenSession getSession() {return session;}
	public Set<?> getArtifacts() {return project.getArtifacts();}
	public File getExtensionDirectory() {return extensionDirectory;}
	public File getOutputDirectory() {return outputDirectory;}
	public ExtensionType getExtensionType() {return extensionType;}
	
}
