package org.lucee.maven.lex.packaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.lucee.maven.lex.InstallTarget;
import org.lucee.maven.lex.config.Config;

public class ManifestTask implements PackagingTask {
	
	private String id;
	private String version;
	private String name;
	private String description;
	private List<String> categories;
	private InstallTarget installTarget; // for release type
	private boolean trial;
	private String luceeCoreVersion;
	private String luceeLoaderVersion;
	private boolean startBundles;

	private List<? extends Config> 	cacheHandlers;
	private List<? extends Config> 	ormEngines;
	private List<? extends Config> 	monitors;
	private List<? extends Config> 	searchEngines;
	private List<? extends Config> 	resourceProviders;
	private List<? extends Config> 	amfs;
	private List<? extends Config> 	jdbcDrivers;
	private List<? extends Config> 	mappings;
	private List<? extends Config> 	jdbcConfig;

	
	public ManifestTask(String id, String version, String name,String description
			,List<String> categories, InstallTarget installTarget,boolean trial
			,String luceeCoreVersion, String luceeLoaderVersion ,boolean startBundles
			,List<? extends Config> cacheHandlers		,List<? extends Config> ormEngines
			,List<? extends Config>  monitors			,List<? extends Config> searchEngines
			,List<? extends Config> resourceProviders	,List<? extends Config> amfs
			,List<? extends Config> jdbcDrivers			,List<? extends Config> mappings
			,List<? extends Config> jdbcConfig
	) {
		this.id = id;
		this.version = version;
		this.name = name;
		this.description = description;
		this.categories = categories;
		this.installTarget = installTarget; // for release type
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
		this.jdbcConfig = jdbcConfig;
	}
 
	public void doPackaging(PackagingContext context) {
		Manifest mf = new Manifest();
		Attributes a = mf.getMainAttributes();
		a.putValue("a", "a");
		
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println(mf.toString());
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
//		try {
//			OutputStream outstr = new FileOutputStream(new File("target/test.mf"));
//			mf.write(outstr);
//			outstr.close();
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
	}

}
