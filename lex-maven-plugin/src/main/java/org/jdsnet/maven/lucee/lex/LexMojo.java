package org.jdsnet.maven.lucee.lex;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.jdsnet.maven.lucee.lex.config.AMFProviderConfig;
import org.jdsnet.maven.lucee.lex.config.CacheHandlerConfig;
import org.jdsnet.maven.lucee.lex.config.EventGatewayConfig;
import org.jdsnet.maven.lucee.lex.config.JdbcDriverConfig;
import org.jdsnet.maven.lucee.lex.config.MappingConfig;
import org.jdsnet.maven.lucee.lex.config.MonitorConfig;
import org.jdsnet.maven.lucee.lex.config.ORMConfig;
import org.jdsnet.maven.lucee.lex.config.ResourceProviderConfig;
import org.jdsnet.maven.lucee.lex.config.SearchConfig;
import org.jdsnet.maven.lucee.lex.packaging.ArchiveTask;
import org.jdsnet.maven.lucee.lex.packaging.CopyDependenciesTask;
import org.jdsnet.maven.lucee.lex.packaging.NoDepsPomTask;
import org.jdsnet.maven.lucee.lex.packaging.PackagingContext;
import org.jdsnet.maven.lucee.lex.packaging.PackagingTask;

/**
 * Packaging task for Lucee Extensions.  Configure the plugin with extensions set to true, then use <b>lex</b> as the packaging type.
 * 
 * @author jesse.shaffer
 *
 */
@Mojo(name = "lex", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyCollection = ResolutionScope.RUNTIME, requiresDependencyResolution=ResolutionScope.RUNTIME)
public class LexMojo extends AbstractLexMojo {

	/**
	 * Classifier to add to the artifact generated.
	 */
	@Parameter
	private String classifier;
	
	
	/**
	 * ID to use for the extension.  This must be a valid CFML UUID, which is in the format of:
	 * xxxxxxxx-xxxx-xxxx-xxxxxxxxxxxxxxxx (note the missing 4th dash)
	 * 
	 * If one is not supplied one will be generated using an MD5 hash of the project's groupId + artifactId.
	 */
	@Parameter
	private String id = null;
	
	/**
	 * A list of one or more categories for this extension.
	 */
	@Parameter
	private List<String> categories;
	
	/**
	 * What type of extension this is.  See {@link ExtensionType}.
	 */
	@Parameter(defaultValue="all")
	private ExtensionType extensionType; // for release type
	
	/**
	 * Whether to flag the extension as a trial
	 */
	@Parameter(defaultValue="false")
	private Boolean trial;

	/**
	 * Minimum required version of the Lucee core.
	 */
	@Parameter
	private String luceeCoreVersion = null;

	/**
	 * Minimum required version of the Lucee loader.
	 */
	@Parameter
	private String luceeLoaderVersion = null;

	/**
	 * Whether or not to immediately start any included OSGi bundles immediately.
	 */
	@Parameter(defaultValue = "false")
	private Boolean startBundles;

	/**
	 * List of AMF providers that are made available by this extension. 
	 * See {@link AMFProviderConfig}
	 */
	@Parameter
	private List<AMFProviderConfig> 		amfProviders		= new ArrayList<AMFProviderConfig>();
	
	/**
	 * List of Resource providers that are made available by this extension.  These provide file schemes (such as ram:/// or s3:///).
	 * See {@link ResourceProviderConfig}
	 */
	@Parameter
	private List<ResourceProviderConfig> 	resourceProviders	= new ArrayList<ResourceProviderConfig>();
	
	/**
	 * List of search providers that are made available by this extension.
	 * See {@link SearchConfig}
	 */
	@Parameter
	private List<SearchConfig> 				searchEngines	 	= new ArrayList<SearchConfig>();
	
	/**
	 * List of ORM engine providers that are made available by this extension.
	 * See {@link ORMConfig}
	 */
	@Parameter
	private List<ORMConfig> 				ormEngines			= new ArrayList<ORMConfig>();
	
	/**
	 * List of Monitors that are made available by this extension.
	 * See {@link MonitorConfig}
	 */
	@Parameter
	private List<MonitorConfig> 			monitors			= new ArrayList<MonitorConfig>();
	
	/**
	 * List of Cache Handlers that are made available by this extension.
	 * See {@link CacheHandlerConfig}
	 */
	@Parameter
	private List<CacheHandlerConfig> 		cacheHandlers		= new ArrayList<CacheHandlerConfig>();
	
	/**
	 * List of JDBC Drivers that are made available by this extension.
	 * See {@link JdbcDriverConfig}
	 */
	@Parameter
	private List<JdbcDriverConfig> 			jdbcDrivers			= new ArrayList<JdbcDriverConfig>();
	
	/**
	 * List of Java-based Event Gateways that are made available by this extension.
	 * See {@link EventGatewayConfig}
	 */
	@Parameter
	private List<EventGatewayConfig> 		eventGateways		= new ArrayList<EventGatewayConfig>();
	
	/**
	 * List of mappings to create in the Lucee configuration, depending on the type of extension this is either server or web.
	 * See {@link MappingConfig}
	 */
	@Parameter
	private List<MappingConfig> 			mappings			= new ArrayList<MappingConfig>();

    /**
     * Configuration for the archive. {@link MavenArchiveConfiguration}
     */
    @Parameter
    private MavenArchiveConfiguration extensionConfig = new MavenArchiveConfiguration();
    
    @Component( role = Archiver.class, hint = "jar" )
    private JarArchiver lexArchiver;

    
    private List<PackagingTask> packagingTasks = new ArrayList<PackagingTask>();
	
    
    
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
	
	/**
	 * generate a valid id from the project's groupId and artifactId
	 * @throws MojoExecutionException
	 */
	private void generateId() throws MojoExecutionException {
		try {
			MessageDigest md5 = MessageDigest.getInstance("md5");
			md5.update((getProject().getGroupId() + getProject().getArtifactId()).getBytes());
			
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
		
		packagingTasks.add(
			new ArchiveTask(
				 extensionConfig		,lexArchiver
				,classifier
				
				,id						,getProject().getVersion()
				,getProject().getName()	,getProject().getDescription()
				
				,categories				,trial
				,luceeCoreVersion		,luceeLoaderVersion
				,startBundles
				
				,cacheHandlers			,ormEngines
				,monitors				,searchEngines
				,resourceProviders		,amfProviders
				,jdbcDrivers			,mappings
				,eventGateways
			)	
		);
		
		packagingTasks.add(new NoDepsPomTask());
	}
	
	private void runPackagingTasks() throws Exception {
		PackagingContext context = new PackagingContext(getProject(), getSession(), getExtensionStagingDir(), getOutputDirectory(), extensionType);

		for (PackagingTask task : packagingTasks) {
			task.doPackaging(context);
		}
	}
}
