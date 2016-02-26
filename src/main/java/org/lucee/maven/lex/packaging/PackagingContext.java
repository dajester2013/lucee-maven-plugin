package org.lucee.maven.lex.packaging;

import java.io.File;
import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.lucee.maven.lex.InstallTarget;

public class PackagingContext {
	
	private final MavenProject project;
	private final File outputDirectory;
	private final InstallTarget installTarget;
	
	/*private String id = null;
	private String version;
	private String name;
	private String description;
	private List<String> categories;
	private InstallTarget installTarget; // for release type
	private boolean trial;
	private String luceeCoreVersion = null;
	private String luceeLoaderVersion = null;
	private boolean startBundles;

	private List<CacheHandlerConfig> 		cacheHandlers		= new ArrayList<CacheHandlerConfig>();
	private List<ORMConfig> 				ormEngines			= new ArrayList<ORMConfig>();
	private List<MonitorConfig> 			monitors			= new ArrayList<MonitorConfig>();
	private List<SearchConfig> 				searchEngines	 	= new ArrayList<SearchConfig>();
	private List<ResourceProviderConfig> 	resourceProviders	= new ArrayList<ResourceProviderConfig>();
	private List<AMFConfig> 				amfs				= new ArrayList<AMFConfig>();
	private List<JdbcConfig> 				jdbcDrivers			= new ArrayList<JdbcConfig>();
	private List<MappingConfig> 			mappings			= new ArrayList<MappingConfig>();*/

	/*, String id, String version, String name, String description, List<String> categories,
	InstallTarget installTarget, boolean trial, String luceeCoreVersion, String luceeLoaderVersion,
	boolean startBundles, List<CacheHandlerConfig> cacheHandlers, List<ORMConfig> ormEngines,
	List<MonitorConfig> monitors, List<SearchConfig> searchEngines,
	List<ResourceProviderConfig> resourceProviders, List<AMFConfig> amfs, List<JdbcConfig> jdbcDrivers,
	List<MappingConfig> mappings, List<JdbcConfig> jdbcConfig*/
	
	
	public PackagingContext(MavenProject project, File outputDirectory, InstallTarget installTarget) {
		
		this.project = project;
		this.outputDirectory = outputDirectory;
		this.installTarget = installTarget;
		
		/*this.id = id;
		this.version = version;
		this.name = name;
		this.description = description;
		this.categories = categories;
		this.installTarget = installTarget;
		this.trial = trial;
		this.luceeCoreVersion = luceeCoreVersion;
		this.luceeLoaderVersion = luceeLoaderVersion;
		this.startBundles = startBundles;

		this.cacheHandlers = cacheHandlers;
		this.ormEngines = ormEngines;
		this.monitors = monitors;
		this.searchEngines = searchEngines;
		this.resourceProviders = resourceProviders;
		this.amfs = amfs;
		this.jdbcDrivers = jdbcDrivers;
		this.mappings = mappings;*/

	}
	
	public MavenProject getProject() {return project;}
	public Set<?> getArtifacts() {return project.getArtifacts();}
	public File getOutputDirectory() {return outputDirectory;}
	public InstallTarget getInstallTarget() {return installTarget;}
	
	
	
	
	/*public String getId() {return id;};
	public String getVersion() {return version;}
	public String getName() {return name;}
	public String getDescription() {return description;}
	public List<String> getCategories() {return categories;}
	public InstallTarget getInstallTarget() {return installTarget;}
	public String getReleaseType() {return installTarget.name();}
	public boolean isTrial() {return trial;}
	public String getLuceeCoreVersion() {return luceeCoreVersion;}
	public String getLuceeLoaderVersion() {return luceeLoaderVersion;}
	public boolean startBundles() {return startBundles;}
	
	public List<? extends Config> getCacheHandlers() {return cacheHandlers;}
	public List<? extends Config> getOrmEngines() {return ormEngines;}
	public List<? extends Config> getMonitors() {return monitors;}
	public List<? extends Config> getSearchEngines() {return searchEngines;}
	public List<? extends Config> getResourceProviders() {return resourceProviders;}
	public List<? extends Config> getAMFs() {return amfs;}
	public List<? extends Config> getJDBCDrivers() {return jdbcDrivers;}
	public List<? extends Config> getMappings() {return mappings;}*/
}
