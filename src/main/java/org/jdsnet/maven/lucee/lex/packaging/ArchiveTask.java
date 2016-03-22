package org.jdsnet.maven.lucee.lex.packaging;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.jdsnet.maven.lucee.lex.ExtensionType;
import org.jdsnet.maven.lucee.lex.config.Config;

public class ArchiveTask implements PackagingTask {
	

    private static final String[] DEFAULT_EXCLUDES = new String[] { "**/package.html" };
    private static final String[] DEFAULT_INCLUDES = new String[] { "**/**" };

    private static final String ARCHIVE_FILE_EXTENSION		= "lex";
    
    private static final String MF_KEY_ID 					= "id";
    private static final String MF_KEY_VERSION 				= "version";
    private static final String MF_KEY_NAME 				= "name";
    private static final String MF_KEY_DESCRIPTION 			= "description";
    private static final String MF_KEY_CATEGORIES 			= "category";
    private static final String MF_KEY_INSTALLTARGET 		= "release-type";
    private static final String MF_KEY_TRIAL 				= "trial";
    private static final String MF_KEY_LUCEECOREVERSION 	= "lucee-core-version";
    private static final String MF_KEY_LUCEELOADERVERSION 	= "lucee-loader-version";
    private static final String MF_KEY_STARTBUNDLES 		= "start-bundles";
    
    private static final String MF_KEY_CACHEHANDLERS		= "cache-handler";
    private static final String MF_KEY_ORMENGINES			= "orm";
    private static final String MF_KEY_MONITORS				= "monitor";
    private static final String MF_KEY_SEARCHENGINES		= "search";
    private static final String MF_KEY_RESOURCEPROVIDERS	= "resource";
    private static final String MF_KEY_AMFS					= "amf";
    private static final String MF_KEY_JDBCDRIVERS			= "jdbc";
    private static final String MF_KEY_MAPPINGS				= "mapping";
    private static final String MF_KEY_EVENTGATEWAYS		= "event-handlers";

    private MavenArchiveConfiguration archiveConfig;
    private JarArchiver lexArchiver;
	private String classifier;
	
	private String id;
	private String version;
	private String name;
	private String description;
	private List<String> categories;
	private Boolean trial;
	private String luceeCoreVersion;
	private String luceeLoaderVersion;
	private Boolean startBundles;

	private List<? extends Config> 	cacheHandlers;
	private List<? extends Config> 	ormEngines;
	private List<? extends Config> 	monitors;
	private List<? extends Config> 	searchEngines;
	private List<? extends Config> 	resourceProviders;
	private List<? extends Config> 	amfs;
	private List<? extends Config> 	jdbcDrivers;
	private List<? extends Config> 	mappings;
	private List<? extends Config> 	eventGateways;

	
	public ArchiveTask(
			 MavenArchiveConfiguration archiveConfig	,JarArchiver lexArchiver
			,String classifier 
			
			,String id, String version, String name,String description
			,List<String> categories, Boolean trial
			,String luceeCoreVersion, String luceeLoaderVersion ,Boolean startBundles
			,List<? extends Config> cacheHandlers		,List<? extends Config> ormEngines
			,List<? extends Config>  monitors			,List<? extends Config> searchEngines
			,List<? extends Config> resourceProviders	,List<? extends Config> amfs
			,List<? extends Config> jdbcDrivers			,List<? extends Config> mappings
			,List<? extends Config> eventGateways
	) {
		
		this.archiveConfig = archiveConfig;
		this.lexArchiver = lexArchiver;
		this.classifier = classifier;
		this.id = id;
		this.version = version;
		this.name = name;
		this.description = description;
		this.categories = categories;
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
		this.mappings = mappings;
		this.eventGateways = eventGateways;
	}
 
	public void doPackaging(PackagingContext context) throws Exception {
		MavenArchiver archiver = new MavenArchiver();
		
		File lexFile = getLexFile(context);
		
		archiver.setArchiver(lexArchiver);
		archiver.setOutputFile(lexFile);
		
		if (id == null) throw new MojoExecutionException("No extension ID provided."); // id is generated if one is not set, so this should never happen!
		if (version == null) throw new MojoExecutionException("No extension version set."); // version is gathered from the pom, so this should never happen!
		
		if (name == null) name = context.getProject().getArtifactId();
		
		archiveConfig.getManifest().setMainClass(null);
		
		addStringEntry(archiveConfig, MF_KEY_ID,					id);
		addStringEntry(archiveConfig, MF_KEY_VERSION,				version);
		addStringEntry(archiveConfig, MF_KEY_NAME,					name);
		addStringEntry(archiveConfig, MF_KEY_DESCRIPTION,			description);
		addStringEntry(archiveConfig, MF_KEY_INSTALLTARGET,			context.getExtensionType().name());
		addStringEntry(archiveConfig, MF_KEY_LUCEECOREVERSION,		luceeCoreVersion);
		addStringEntry(archiveConfig, MF_KEY_LUCEELOADERVERSION,	luceeLoaderVersion);

		if (categories != null)
			addStringEntry(archiveConfig, MF_KEY_CATEGORIES, categories.toString());
		
		addBooleanEntry(archiveConfig, MF_KEY_TRIAL, trial);
		addBooleanEntry(archiveConfig, MF_KEY_STARTBUNDLES, startBundles);

		addConfigListEntry(archiveConfig, MF_KEY_CACHEHANDLERS,		cacheHandlers);
		addConfigListEntry(archiveConfig, MF_KEY_ORMENGINES,		ormEngines);
		addConfigListEntry(archiveConfig, MF_KEY_MONITORS,			monitors);
		addConfigListEntry(archiveConfig, MF_KEY_SEARCHENGINES,		searchEngines);
		addConfigListEntry(archiveConfig, MF_KEY_RESOURCEPROVIDERS,	resourceProviders);
		addConfigListEntry(archiveConfig, MF_KEY_AMFS,				amfs);
		addConfigListEntry(archiveConfig, MF_KEY_JDBCDRIVERS,		jdbcDrivers);
		addConfigListEntry(archiveConfig, MF_KEY_MAPPINGS,			mappings);
		addConfigListEntry(archiveConfig, MF_KEY_EVENTGATEWAYS,		eventGateways);
		
		archiver.getArchiver().addDirectory(context.getExtensionDirectory(), DEFAULT_INCLUDES, DEFAULT_EXCLUDES);
		
		archiver.createArchive(context.getSession(), context.getProject(), archiveConfig);
		
		context.getProject().getArtifact().setFile(lexFile);
	}

	private void addStringEntry(MavenArchiveConfiguration archiveConfig, String key, String value) {
		if (value != null && !archiveConfig.getManifestEntries().containsKey(key)) {
			archiveConfig.addManifestEntry(key, '"' + value + '"');
		}
	}

	private void addBooleanEntry(MavenArchiveConfiguration archiveConfig, String key, Boolean value) {
		if (value != null && !archiveConfig.getManifestEntries().containsKey(key)) {
			archiveConfig.addManifestEntry(key, String.valueOf(value));
		}
	}
	
	private void addConfigListEntry(MavenArchiveConfiguration archiveConfig, String key, List<? extends Config> configs) {
		if (configs.size() > 0 && !archiveConfig.getManifestEntries().containsKey(key)) {
			archiveConfig.addManifestEntry(key, '"'+toJsonArray(configs)+'"');
		}
	}

	private String toJsonArray(List<? extends Config> configs) {
		StringBuilder jsonArray = new StringBuilder();
		
		jsonArray.append("[");
		boolean first = true;
		for (Config c : configs) {
			if (first) first = false;
			else jsonArray.append(',');
			
			jsonArray.append(c.serializeJSON());
		}
		jsonArray.append("]");
		
		return jsonArray.toString();
	}
	
	private File getLexFile(PackagingContext context) {
		MavenProject project = context.getProject();
		
		StringBuffer fileName = new StringBuffer(project.getBuild().getFinalName());
		
		if (classifier != null) {
			fileName.append("-")
					.append(classifier)
					;
		}
		
		fileName.append('.')
				.append(ARCHIVE_FILE_EXTENSION);
		
		return new File(context.getOutputDirectory(), fileName.toString());
	}

}
