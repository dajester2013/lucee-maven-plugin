package org.jdsnet.maven.lucee.lar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jdsnet.maven.lucee.AbstractLuceeMojo;

@Mojo(
	 name			= "lar"
	,defaultPhase	= LifecyclePhase.PACKAGE
	,threadSafe		= true
	,requiresDependencyCollection = ResolutionScope.COMPILE
)
public class LarMojo extends AbstractLuceeMojo {
    /**
     * Project classpath.
     */
    @Parameter( defaultValue = "${project.compileClasspathElements}", readonly = true, required = true )
    private List<String> classpathElements;
    
    /**
     * Whether or not to include source cfc/cfm/lucee/lc files in the final lar file.
     */
	@Parameter(defaultValue="false")
	private boolean larIncludeSourceFiles;

	/**
	 * Whether or not to include non-cfml/lucee files into the final lar file.
	 */
	@Parameter(defaultValue="true")
	private boolean larIncludeStaticFiles;
	
	/**
	 * The type of lar archive this is.  Valid options are "component" and "mapping".
	 */
	@Parameter(defaultValue="component", required=true)
	private String larType;
	
	/**
	 * The virtual path for the lar mapping.  For component archives, this is not necessary, but for mapping archives, this is important.
	 */
	@Parameter(defaultValue="/${project.artifactId}-${project.version}", required=true)
	private String larVirtualPath;
	
	/**
	 * Output destination.  For lar packaging, this is the where to place the cfml/lucee/static files that will be packaged into the final archive.
	 */
	@Parameter(defaultValue="${project.build.directory}/archive", required=true)
	private File outputDirectory;
	
	/**
	 * Where to place the lucee runtime files necessary for the compilation phase
	 */
	@Parameter(defaultValue="${project.build.directory}/lucee", required=true)
	private File luceeRuntimeDirectory;
	
	@Parameter(defaultValue="${project.build.directory}", required=true)
	private String targetPath;
	
	/**
	 * The lar's file name.
	 */
	@Parameter(defaultValue="${project.build.finalName}", required=true)
	private String outputFileName;
	
	/**
	 * Whether to show verbose outut from the lucee build process.
	 */
	@Parameter(defaultValue="false")
	private boolean verbose;
	
	public void execute() throws MojoExecutionException {
		validate();
    	Log log = getLog();
    	
    	if (!outputDirectory.exists())
    		if (getProject().getPackaging().equals("lar"))
    			throw new MojoExecutionException("Missing source for Lucee archive");
    		else
    			return;
    	
    	System.getProperties().put("lucee.server.dir", luceeRuntimeDirectory.getAbsolutePath());
    	System.getProperties().put("lucee.web.dir", luceeRuntimeDirectory.getAbsolutePath());
    	
    	PrintStream devnull = new PrintStream(new ByteArrayOutputStream());
    	PrintStream out = System.out;
    	PrintStream err = System.err;
    	
    	if (!verbose) {
	    	System.setOut(devnull);
	    	System.setErr(devnull);
    	}
    	
    	log.info("Initializing Lucee execution environment...");
    	ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("Lucee");
		log.info("done.");
		
		if (engine == null) {
			if (getProject().getPackaging().equals("lar"))
				throw new MojoExecutionException("Missing plugin dependency for Lucee for creating the archive.");
			else {
				out.println("[WARNING] Lucee runtime dependency not included in the plugin classpath - Lucee archive not built.");
				return;
			}
		}
		
		log.info("Lucee version " + engine.getFactory().getEngineVersion());
		
		try {
			String finalFileName = targetPath + "/" + outputFileName + (getClassifier() != null ? "-" + getClassifier() : "");
			log.info("Packaging Lucee Archive...");
			engine.eval(  "try{admin	action=\"updatePassword\" type=\"web\" newPassword=\"password\";}catch(any e){/*password already set*/}"
					
						+ "admin	action=\"" + (larType.equalsIgnoreCase("component") ? "updateComponentMapping" : "updateMapping") + "\""
						+ "			type=\"web\""
						+ "			physical=\"" + outputDirectory + "\""
						+ "			archive=\"\""
						+ "			virtual=\"" + larVirtualPath + "\""
						+ "			password=\"password\""
						+ "			primary=\"physical\""
						+ "			;"
						
						+ "admin	action=\"" + (larType.equalsIgnoreCase("component") ? "createComponentArchive" : "createArchive") + "\""
						+ "			type=\"web\""
						+ "			virtual=\"" + larVirtualPath + "\""
						+ "			password=\"password\""
						+ "			file=\""+finalFileName+".lar\""
						+ "			addCFMLFiles=" + (larIncludeSourceFiles ? "true" : "false")
						+ "			addNonCFMLFiles=" + (larIncludeStaticFiles ? "true" : "false")
						+ "			append=false"
						+ "			;"

						+ "admin	action=\"" + (larType.equalsIgnoreCase("component") ? "removeComponentMapping" : "removeMapping") + "\""
						+ "			type=\"web\""
						+ "			virtual=\"" + larVirtualPath + "\""
						+ "			password=\"password\""
						+ "			;"
						
						);
			log.info("done.");
	    	
			if (getProject().getPackaging().equals("lar")) {
				log.info("Attaching artifact " + finalFileName + ".lar");
				getProject().getArtifact().setFile(new File(finalFileName + ".lar"));
			}
		} catch (ScriptException e) {
			throw new MojoExecutionException("Error", e);
		} finally {
	    	System.setOut(out);
	    	System.setErr(err);
		}
	}
	
	private void validate() throws MojoExecutionException {
		if (!larType.equals("component") && !larType.equals("mapping"))
			throw new MojoExecutionException("Invalid larType \""+larType+"\".  Valid options are \"component\" or \"mapping\".");
	}
	
}
