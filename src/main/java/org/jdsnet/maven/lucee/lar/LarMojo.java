package org.jdsnet.maven.lucee.lar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
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
import org.jdsnet.maven.lucee.lar.util.CompileTimeMapping;

@Mojo(
	 name			= "lar"
	,defaultPhase	= LifecyclePhase.PACKAGE
	,threadSafe		= true
	,requiresDependencyCollection = ResolutionScope.COMPILE
)
public class LarMojo extends AbstractLarMojo {
    
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
	 * Where to place the lucee runtime files necessary for the compilation phase
	 */
	@Parameter(property="lucee.runtime.dir", defaultValue="${project.build.directory}/lucee", required=true)
	private File luceeRuntimeDirectory;
	
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
	
	@Parameter
	private List<CompileTimeMapping> larCompileTimeMappings = new ArrayList<>();
	
	public void execute() throws MojoExecutionException {
    	Log log = getLog();
    	
    	if (!validate()) return;
    	
    	System.getProperties().put("lucee.server.dir", luceeRuntimeDirectory.getAbsolutePath());
    	System.getProperties().put("lucee.web.dir", luceeRuntimeDirectory.getAbsolutePath());
    	
    	ByteArrayOutputStream devnullout = new ByteArrayOutputStream();
    	PrintStream devnull = new PrintStream(devnullout);
    	PrintStream out = System.out;
    	PrintStream err = System.err;
    	
    	if (!verbose) {
	    	System.setOut(devnull);
	    	System.setErr(devnull);
    	}
    	
    	log.info("Initializing Lucee execution environment...");
    	ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("Lucee");
		
		for(String line : new String(devnullout.toByteArray()).split("[\n\r]+")) {
			log.debug(line);
		}
		
		log.info("done.");
		
		if (engine == null) {
			if (getProject().getPackaging().equals("lar"))
				throw new MojoExecutionException("Lucee runtime dependency not included in the plugin classpath.  Please verify the plugin dependency is set.");
			else {
				getLog().warn("Lucee runtime dependency not included in the plugin classpath - Lucee archive not built.");
				return;
			}
		}
		
		log.info("Lucee version " + engine.getFactory().getEngineVersion());
		
		try {
			String finalFileName = getOutputDirectory().getAbsolutePath() + "/" + outputFileName + (getClassifier() != null ? "-" + getClassifier() : "");
			log.info("Packaging Lucee Archive...");
			
			
			StringBuilder cmd = new StringBuilder();
			
			cmd.append(   "try{admin	action=\"updatePassword\" type=\"web\" newPassword=\"password\";}catch(any e){/*password already set*/}"
					
						+ "admin	action=\"" + (larType.equalsIgnoreCase("component") ? "updateComponentMapping" : "updateMapping") + "\""
						+ "			type=\"web\""
						+ "			physical=\"" + getLarOutputDirectory().getAbsolutePath() + "\""
						+ "			archive=\"\""
						+ "			virtual=\"" + larVirtualPath + "\""
						+ "			password=\"password\""
						+ "			primary=\"physical\""
						+ "			;\n");
			
			// add mappings necessary to compile the source
			for (CompileTimeMapping m : larCompileTimeMappings) {
				log.info("Add Mapping:");
				log.info("  Virtual:  " + m.getMapping());
				log.info("  Physical: " + m.getPath().getAbsolutePath());
				cmd.append(	  "admin	action=\"updateMapping\""
							+ "			type=\"web\""
							+ "			physical=\"" + m.getPath().getAbsolutePath() + "\""
							+ "			archive=\"\""
							+ "			virtual=\"" + m.getMapping() + "\""
							+ "			password=\"password\""
							+ "			primary=\"physical\""
							+ "			;\n");
			}
			
			
			cmd.append("admin	action=\"" + (larType.equalsIgnoreCase("component") ? "createComponentArchive" : "createArchive") + "\""
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
						+ "			;");
			
			// clean up mappings
			for (CompileTimeMapping m : larCompileTimeMappings) {
				cmd.append(	  "admin	action=\"removeMapping\""
							+ "			type=\"web\""
							+ "			virtual=\"" + m.getMapping() + "\""
							+ "			password=\"password\""
							+ "			;");
			}
			
			engine.eval(cmd.toString());
			
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
	
	/**
	 * Throws exception on fatal invalidation, otherwise returns false for a non-fatal invalidation.
	 * @return
	 * @throws MojoExecutionException
	 */
	private boolean validate() throws MojoExecutionException {
		if (!larType.equals("component") && !larType.equals("mapping"))
			throw new MojoExecutionException("Invalid larType \""+larType+"\".  Valid options are \"component\" or \"mapping\".");
		

    	
    	if (!getLarOutputDirectory().exists())
    		if (getProject().getPackaging().equals("lar"))
    			throw new MojoExecutionException("Missing source for Lucee archive");
    		else
    			return false;
    	
    	
    	return true;
	}
	
}
