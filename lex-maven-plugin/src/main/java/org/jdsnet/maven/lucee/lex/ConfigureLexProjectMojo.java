package org.jdsnet.maven.lucee.lex;

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
	
	@Parameter(defaultValue="all")
	private ExtensionType installTarget; // for release type
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		Resource r;
		
		if (!getExtensionStagingDir().exists())
			getExtensionStagingDir().mkdirs();
		
		String extensionDirectory = getExtensionStagingDir().getAbsolutePath();
				
		if (getExtensionSourceDir().exists()) {
			List<String> excludes = new ArrayList<String>();
			excludes.add("**/*.jar");
			excludes.add("**/*.lar");
			
			r = new Resource();
			r.setDirectory(getExtensionSourceDir().getAbsolutePath());
			r.setFiltering(true);
			r.setTargetPath(extensionDirectory);
			r.setExcludes(excludes);
			getResources().add(r);

			
			List<String> includes = new ArrayList<String>();
			includes.add("**/*.jar");
			includes.add("**/*.lar");
			
			r = new Resource();
			r.setDirectory(getExtensionSourceDir().getAbsolutePath());
			r.setFiltering(false);
			r.setTargetPath(extensionDirectory);
			r.setIncludes(includes);
			getResources().add(r);
		}
		
	}
	
}
