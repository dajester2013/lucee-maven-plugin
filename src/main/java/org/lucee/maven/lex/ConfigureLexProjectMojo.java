package org.lucee.maven.lex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name="lex-config-project", threadSafe=true)
public class ConfigureLexProjectMojo extends AbstractLexMojo {
	/**
	 * Import the list of resources configured in the project.
	 */
	@Parameter( defaultValue = "${project.resources}", required = true, readonly = true )
	private List<Resource> resources;
	
	@Parameter(defaultValue="src/main/extension")
	private File extensionSourceDir;
	
	@Parameter(defaultValue="all")
	private ExtensionType installTarget; // for release type
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		Resource r;
		
		if (!getExtensionDirectory().exists())
			getExtensionDirectory().mkdirs();
		
		String extensionDirectory = getExtensionDirectory().getAbsolutePath();
				
		if (extensionSourceDir.exists()) {
			List<String> excludes = new ArrayList<String>();
			excludes.add("**/*.jar");
			excludes.add("**/*.lar");
			
			r = new Resource();
			r.setDirectory(extensionSourceDir.getAbsolutePath());
			r.setFiltering(true);
			r.setTargetPath(extensionDirectory);
			r.setExcludes(excludes);
			resources.add(r);

			
			List<String> includes = new ArrayList<String>();
			includes.add("**/*.jar");
			includes.add("**/*.lar");
			
			r = new Resource();
			r.setDirectory(extensionSourceDir.getAbsolutePath());
			r.setFiltering(false);
			r.setTargetPath(extensionDirectory);
			r.setIncludes(includes);
			resources.add(r);
		}
		
	}
	
}
