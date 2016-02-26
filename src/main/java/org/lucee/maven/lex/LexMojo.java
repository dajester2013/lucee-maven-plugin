package org.lucee.maven.lex;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.lucee.maven.lex.config.AMFConfig;
import org.lucee.maven.lex.config.CacheHandlerConfig;
import org.lucee.maven.lex.config.Config;
import org.lucee.maven.lex.config.JdbcConfig;
import org.lucee.maven.lex.config.MappingConfig;
import org.lucee.maven.lex.config.MonitorConfig;
import org.lucee.maven.lex.config.ORMConfig;
import org.lucee.maven.lex.config.ResourceProviderConfig;
import org.lucee.maven.lex.config.SearchConfig;
import org.lucee.maven.lex.packaging.ArchiveTask;
import org.lucee.maven.lex.packaging.CopyDependenciesTask;
import org.lucee.maven.lex.packaging.ManifestTask;
import org.lucee.maven.lex.packaging.PackagingContext;
import org.lucee.maven.lex.packaging.PackagingTask;
import org.lucee.maven.lex.packaging.NoDepsPomTask;

@Mojo(name = "lex", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyCollection = ResolutionScope.RUNTIME, requiresDependencyResolution=ResolutionScope.RUNTIME)
public class LexMojo extends AbstractMojo {

	@Parameter(defaultValue="${project.build.directory}/extension")
	private String outputDirectory;
	
	@Parameter
	private String id = null;

	@Parameter(defaultValue = "${project.version}", readonly = true, required = true)
	private String version;

	@Parameter(defaultValue = "${project.name}", readonly = true)
	private String name;

	@Parameter(defaultValue = "${project.description}", readonly = true)
	private String description;

	@Parameter
	private List<String> categories;
	
	@Parameter(defaultValue="all")
	private InstallTarget installTarget; // for release type
	
	@Parameter(defaultValue="false")
	private boolean trial;

	@Parameter
	private String luceeCoreVersion = null;

	@Parameter
	private String luceeLoaderVersion = null;

	@Parameter(defaultValue = "false")
	private boolean startBundles;

	@Parameter
	private List<CacheHandlerConfig> 		cacheHandlers		= new ArrayList<CacheHandlerConfig>();
	@Parameter
	private List<ORMConfig> 				ormEngines			= new ArrayList<ORMConfig>();
	@Parameter
	private List<MonitorConfig> 			monitors			= new ArrayList<MonitorConfig>();
	@Parameter
	private List<SearchConfig> 				searchEngines	 	= new ArrayList<SearchConfig>();
	@Parameter
	private List<ResourceProviderConfig> 	resourceProviders	= new ArrayList<ResourceProviderConfig>();
	@Parameter
	private List<AMFConfig> 				amfs				= new ArrayList<AMFConfig>();
	@Parameter
	private List<JdbcConfig> 				jdbcDrivers			= new ArrayList<JdbcConfig>();
	@Parameter
	private List<MappingConfig> 			mappings			= new ArrayList<MappingConfig>();
	@Parameter
	private List<JdbcConfig> 				jdbcConfig			= new ArrayList<JdbcConfig>();

	private List<PackagingTask> packagingTasks = new ArrayList<PackagingTask>();
	
	/**
	 * The Maven project.
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (id == null) {
			generateId();
		}
		
		preparePackagingTasks();
		
		try {
			runPackagingTasks();
		} catch (Exception e) {
			throw new MojoExecutionException("Unexpected error", e);
		}
	}
	
	
	private void generateId() throws MojoExecutionException {
		try {
			MessageDigest md5 = MessageDigest.getInstance("md5");
			md5.update((this.project.getArtifactId() + this.project.getGroupId()).getBytes());
			
			char[] idHex = Hex.encodeHex(md5.digest());
			StringBuffer idBuilder = new StringBuffer();
			
			for (char c : idHex) {
				if (	idBuilder.length() == 8
					||	idBuilder.length() == 13
					||	idBuilder.length() == 18)
					idBuilder.append('-');
				
				idBuilder.append(c);
			}
			
			id = idBuilder.toString().toUpperCase();
		} catch(Exception e) {
			throw new MojoExecutionException("Failed to generate ID", e);
		}
	}

	
	private void preparePackagingTasks() {
		
		packagingTasks.add(new CopyDependenciesTask());
		
		packagingTasks.add(new ManifestTask(
				id
				,version
				,name
				,description
				,categories
				,installTarget
				,trial
				,luceeCoreVersion
				,luceeLoaderVersion
				,startBundles
				,cacheHandlers
				,ormEngines
				, monitors			,searchEngines
				,resourceProviders	,amfs
				,jdbcDrivers			,mappings
				,jdbcConfig));
		
		packagingTasks.add(new ArchiveTask());
		packagingTasks.add(new NoDepsPomTask());
	}
	
	private void runPackagingTasks() throws Exception {
		PackagingContext context = new PackagingContext(project, new File(outputDirectory), installTarget);

		for (PackagingTask task : packagingTasks) {
			task.doPackaging(context);
		}
	}
}
