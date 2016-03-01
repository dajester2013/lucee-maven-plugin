package org.lucee.maven.lex;

import java.io.File;
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
	
	@Parameter(defaultValue="src/main/ext-resources")
	private String extensionResourcesDir;
	
	@Parameter(defaultValue="src/main/flds")
	private String fldsSourceDir;
	@Parameter(defaultValue="src/main/tlds")
	private String tldsSourceDir;
	@Parameter(defaultValue="src/main/tags")
	private String tagsSourceDir;
	@Parameter(defaultValue="src/main/functions")
	private String functionsSourceDir;
	@Parameter(defaultValue="src/main/eventGateways")
	private String eventGatewaysSourceDir;
	@Parameter(defaultValue="src/main/context")
	private String contextSourceDir;
	@Parameter(defaultValue="src/main/app")
	private String appSourceDir;
	@Parameter(defaultValue="src/main/plugins")
	private String pluginsSourceDir;

	@Parameter(defaultValue="all")
	private ExtensionType installTarget; // for release type
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		Resource r;
		
		if (!getExtensionDirectory().exists())
			getExtensionDirectory().mkdirs();
		
		String extensionDirectory = getExtensionDirectory().getAbsolutePath();
		
		if (new File(extensionResourcesDir).exists()) {
			r = new Resource();
			r.setDirectory(extensionResourcesDir);
			r.setFiltering(true);
			r.setTargetPath(extensionDirectory);
			resources.add(r);
		}
		
		if (new File(fldsSourceDir).exists()) {
			r = new Resource();
			r.setDirectory(fldsSourceDir);
			r.setFiltering(true);
			r.setTargetPath(extensionDirectory + "/" + Constants.DIR_FLDS);
			resources.add(r);
		}

		if (new File(tldsSourceDir).exists()) {
			r = new Resource();
			r.setDirectory(tldsSourceDir);
			r.setFiltering(true);
			r.setTargetPath(extensionDirectory + "/" + Constants.DIR_TLDS);	   	
			resources.add(r);
		}
		
		if (new File(tagsSourceDir).exists()) {
			r = new Resource();
			r.setDirectory(tagsSourceDir);
			r.setFiltering(true);
			r.setTargetPath(extensionDirectory + "/" + Constants.DIR_TAGS);
			resources.add(r);
		}

		if (new File(functionsSourceDir).exists()) {
			r = new Resource();
			r.setDirectory(functionsSourceDir);
			r.setFiltering(true);
			r.setTargetPath(extensionDirectory + "/" + Constants.DIR_FUNCTIONS);
			resources.add(r);
		}

		if (new File(eventGatewaysSourceDir).exists()) {
			r = new Resource();
			r.setDirectory(eventGatewaysSourceDir);
			r.setFiltering(true);
			r.setTargetPath(extensionDirectory + "/" + Constants.DIR_EVENTGATEWAYS);
			resources.add(r);
		}

		if (new File(contextSourceDir).exists()) {
			r = new Resource();
			r.setDirectory(contextSourceDir);
			r.setFiltering(true);
			r.setTargetPath(extensionDirectory + "/" + (installTarget == ExtensionType.web ? Constants.DIR_CONTEXT : Constants.DIR_WEBCONTEXTS));
			resources.add(r);
		}

		if (new File(appSourceDir).exists()) {
			r = new Resource();
			r.setDirectory(appSourceDir);
			r.setFiltering(true);
			r.setTargetPath(extensionDirectory + "/" + Constants.DIR_APPLICATIONS);
			resources.add(r);
		}

		if (new File(pluginsSourceDir).exists()) {
			r = new Resource();
			r.setDirectory(pluginsSourceDir);
			r.setFiltering(true);
			r.setTargetPath(extensionDirectory + "/" + Constants.DIR_PLUGINS);
			resources.add(r);
		}
		
	}
	
}
