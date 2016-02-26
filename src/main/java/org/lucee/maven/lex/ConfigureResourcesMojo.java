package org.lucee.maven.lex;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name="lex-config-resources", threadSafe=true)
public class ConfigureResourcesMojo extends AbstractMojo {
	/**
	 * The list of resources we want to transfer.
	 */
	@Parameter( defaultValue = "${project.resources}", required = true, readonly = true )
	private List<Resource> resources;

	@Parameter(defaultValue="${project.build.directory}/extension")
	private String outputDirectory;

	@Parameter(defaultValue="src/main/flds")
	private String fldsDir;
	@Parameter(defaultValue="src/main/tlds")
	private String tldsDir;
	@Parameter(defaultValue="src/main/tags")
	private String tagsDir;
	@Parameter(defaultValue="src/main/functions")
	private String functionsDir;
	@Parameter(defaultValue="src/main/eventGateways")
	private String eventGatewaysDir;
	@Parameter(defaultValue="src/main/context")
	private String contextDir;
	@Parameter(defaultValue="src/main/app")
	private String appDir;
	@Parameter(defaultValue="src/main/plugins")
	private String pluginsDir;
	@Parameter(defaultValue="src/main/jars")
	private String jarsDir;
	@Parameter(defaultValue="src/main/lars")
	private String archivesDir;

	@Parameter(defaultValue="all")
	private InstallTarget installTarget; // for release type

	@Parameter(defaultValue="${project}", readonly=true)
	private MavenProject project; // for release type
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		Resource r;
		
		project.getProperties().setProperty("$lucee.lex.extensions.outputdir", outputDirectory);
		
		if (new File(fldsDir).exists()) {
			r = new Resource();
			r.setDirectory(fldsDir);
			r.setFiltering(true);
			r.setTargetPath(outputDirectory + "/" + Constants.DIR_FLDS);
			resources.add(r);
		}

		if (new File(tldsDir).exists()) {
			r = new Resource();
			r.setDirectory(tldsDir);
			r.setFiltering(true);
			r.setTargetPath(outputDirectory + "/" + Constants.DIR_TLDS);	   	
			resources.add(r);
		}
		
		if (new File(tagsDir).exists()) {
			r = new Resource();
			r.setDirectory(tagsDir);
			r.setFiltering(true);
			r.setTargetPath(outputDirectory + "/" + Constants.DIR_TAGS);
			resources.add(r);
		}

		if (new File(functionsDir).exists()) {
			r = new Resource();
			r.setDirectory(functionsDir);
			r.setFiltering(true);
			r.setTargetPath(outputDirectory + "/" + Constants.DIR_FUNCTIONS);
			resources.add(r);
		}

		if (new File(eventGatewaysDir).exists()) {
			r = new Resource();
			r.setDirectory(eventGatewaysDir);
			r.setFiltering(true);
			r.setTargetPath(outputDirectory + "/" + Constants.DIR_EVENTGATEWAYS);
			resources.add(r);
		}

		if (new File(contextDir).exists()) {
			r = new Resource();
			r.setDirectory(contextDir);
			r.setFiltering(true);
			r.setTargetPath(outputDirectory + "/" + (installTarget == InstallTarget.web ? Constants.DIR_CONTEXT : Constants.DIR_WEBCONTEXTS));
			resources.add(r);
		}

		if (new File(appDir).exists()) {
			r = new Resource();
			r.setDirectory(appDir);
			r.setFiltering(true);
			r.setTargetPath(outputDirectory + "/" + Constants.DIR_APPLICATIONS);
			resources.add(r);
		}

		if (new File(pluginsDir).exists()) {
			r = new Resource();
			r.setDirectory(pluginsDir);
			r.setFiltering(true);
			r.setTargetPath(outputDirectory + "/" + Constants.DIR_PLUGINS);
			resources.add(r);
		}

		if (new File(jarsDir).exists()) {
			r = new Resource();
			r.setDirectory(jarsDir);
			r.setFiltering(false);
			r.setTargetPath(outputDirectory + "/" + Constants.DIR_JARS);
			resources.add(r);
		}

		if (new File(archivesDir).exists()) {
			r = new Resource();
			r.setDirectory(archivesDir);
			r.setFiltering(false);
			r.setTargetPath(outputDirectory + "/" + Constants.DIR_ARCHIVES);
			resources.add(r);
		}
		
	}
	
}
