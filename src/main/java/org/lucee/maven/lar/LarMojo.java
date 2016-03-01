package org.lucee.maven.lar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

@Mojo(
	 name			= "lar"
	,defaultPhase	= LifecyclePhase.PACKAGE
	,threadSafe		= true
)
public class LarMojo extends AbstractMojo {
    /**
     * Project classpath.
     */
    @Parameter( defaultValue = "${project.compileClasspathElements}", readonly = true, required = true )
    private List<String> classpathElements;

	@Parameter
	private boolean includeSourceFiles=false;

	@Parameter
	private boolean includeStaticFiles=true;
	
	@Parameter
	private String classifier;

	@Parameter(defaultValue="component", required=true)
	private String archiveType;

	@Parameter(defaultValue="${project.build.outputDirectory}", required=true)
	private String outputDirectory;

	@Parameter(defaultValue="${project.build.directory}/lucee", required=true)
	private String luceeServerDirectory;

	@Parameter(defaultValue="${project.build.directory}/lucee", required=true)
	private String luceeWebDirectory;

	@Parameter(defaultValue="${project.build.directory}", required=true)
	private String targetPath;
	
	@Parameter(defaultValue="${project.build.finalName}", required=true)
	private String outputFileName;
	
	@Parameter(defaultValue="${project}", readonly=true, required=true)
	private MavenProject project;

    @Component
    private MavenProjectHelper projectHelper;
    
    @SuppressWarnings("restriction")
	public void execute() throws MojoExecutionException {
    	if (!(new File("src/main/lucee").exists())) // fix this
    		if (project.getPackaging().equals("lar"))
    			throw new MojoExecutionException("Missing source for Lucee archive");
    		else
    			return;
    	
    	System.getProperties().put("lucee.server.dir", luceeServerDirectory);
    	System.getProperties().put("lucee.web.dir", luceeWebDirectory);
    	
    	PrintStream devnull = new PrintStream(new ByteArrayOutputStream());
    	PrintStream out = System.out;
    	PrintStream err = System.err;
    	System.setOut(devnull);
    	System.setErr(devnull);
    	
    	out.print("Initializing Lucee execution environment...");
    	ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("Lucee");
		out.println("done.");
		
		if (engine == null) {
			if (project.getPackaging().equals("lar"))
				throw new MojoExecutionException("Missing plugin dependency for Lucee for creating the archive.");
			else {
				System.out.println("[WARNING] Lucee runtime dependency not included in the plugin classpath - Lucee archive not built.");
				return;
			}
		}
		
		try {
			String finalFileName = targetPath + "/" + outputFileName + (classifier != null ? "-" + classifier : "") + ".lar";
			out.print("Packaging Lucee Archive...");
			engine.eval(  "try{admin	action=\"updatePassword\" type=\"web\" newPassword=\"password\";}catch(any e){/*password already set*/}"
					
						+ "admin	action=\"" + (archiveType.equalsIgnoreCase("component") ? "updateComponentMapping" : "updateMapping") + "\""
						+ "			type=\"web\""
						+ "			physical=\"" + outputDirectory + "\""
						+ "			archive=\"\""
						+ "			virtual=\"/" + project.getArtifactId() + "-" + project.getVersion() + "\""
						+ "			password=\"password\""
						+ "			primary=\"physical\""
						+ "			;"
						
						+ "admin	action=\"" + (archiveType.equalsIgnoreCase("component") ? "createComponentArchive" : "createArchive") + "\""
						+ "			type=\"web\""
						+ "			virtual=\"/" + project.getArtifactId() + "-" + project.getVersion() + "\""
						+ "			password=\"password\""
						+ "			file=\""+finalFileName+"\""
						+ "			addCFMLFiles=" + (includeSourceFiles ? "true" : "false")
						+ "			addNonCFMLFiles=" + (includeStaticFiles ? "true" : "false")
						+ "			append=false"
						+ "			;"

						+ "admin	action=\"" + (archiveType.equalsIgnoreCase("component") ? "removeComponentMapping" : "removeMapping") + "\""
						+ "			type=\"web\""
						+ "			virtual=\"/" + project.getArtifactId() + "-" + project.getVersion() + "\""
						+ "			password=\"password\""
						+ "			;"
						
						);
			out.println("done.");
			
		} catch (ScriptException e) {
			throw new MojoExecutionException("Error", e);
		} finally {
	    	System.setOut(out);
	    	System.setErr(err);
		}
	}
	
}
