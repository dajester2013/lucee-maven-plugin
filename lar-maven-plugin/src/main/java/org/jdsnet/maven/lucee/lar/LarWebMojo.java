package org.jdsnet.maven.lucee.lar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.utils.io.FileUtils;
import org.apache.maven.shared.utils.io.IOUtil;
import org.jdsnet.maven.lucee.lar.util.CompileTimeMapping;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.servlet.CFMLServlet;

@Mojo(
	 name			= "lar-web"
	,defaultPhase	= LifecyclePhase.PACKAGE
	,threadSafe		= true
	,requiresDependencyCollection = ResolutionScope.COMPILE
)
public class LarWebMojo extends AbstractLarMojo {
	
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
	
	@Parameter(defaultValue="${project.build.directory}")
	private File larOutputDirectory;
	
	@Parameter
	private List<CompileTimeMapping> larCompileTimeMappings = new ArrayList<>();
	
	public void execute() throws MojoExecutionException {
		final Log log = getLog();
		
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
    	
		try {
			File webroot = new File(larOutputDirectory + "/lar-build-scripts/");
			
			// TODO: move this to function
			URL res = LarWebMojo.class.getResource("/lar-build-scripts/generate.cfm");
			File outFile = new File(larOutputDirectory, "/lar-build-scripts/generate.cfm");
			if (outFile.exists()) outFile.delete();
			FileUtils.copyURLToFile(res, outFile);
			
			final int port = getOpenPort();
			final Tomcat tc = new Tomcat();
			
			tc.setBaseDir(larOutputDirectory.getAbsolutePath());
			tc.setPort(port);
			
			Context ctx = tc.addContext("/lar", webroot.getAbsolutePath());
			
			// add cfml servlet
			Tomcat	.addServlet(ctx, "CFMLServlet", new CFMLServlet())
					.setLoadOnStartup(1);
			ctx		.addServletMapping("*.cfm", "CFMLServlet");
			
			getLog().info("Open tomcat on port "+port);
			tc.start();

			try {
				log.debug("Build url");
				
				String finalFileName = larOutputDirectory.getAbsolutePath() + "/" + outputFileName + (getClassifier() != null ? "-" + getClassifier() : "");
				
				StringBuffer mappingsJson = new StringBuffer("{");
				boolean first=true;
				
				for (CompileTimeMapping m : larCompileTimeMappings) {
					if (first) first=false;
					else mappingsJson.append(",");
					
					mappingsJson.append("\"")
								.append(m.getVirtual())
								.append("\":\"")
								.append(m.getPhysical().getAbsolutePath().replaceAll("\\\\", "/"))
								.append("\"");
				}
				
				mappingsJson.append("}");
				
				URL url = new URL(String.format(
					 "http://localhost:%d/lar/generate.cfm?type=%s&phys=%s&virt=%s&mappings=%s&larFile=%s&includeSource=%s&includeStatic=%s"
					,port
					,larType
					,URLEncoder.encode(getLarStagingDir().getAbsolutePath(), "UTF-8")
					,URLEncoder.encode(larVirtualPath, "UTF-8")
					,URLEncoder.encode(mappingsJson.toString(), "UTF-8")
					,URLEncoder.encode(finalFileName, "UTF-8")
					,String.valueOf(larIncludeSourceFiles)
					,String.valueOf(larIncludeStaticFiles)
				));
				
				log.debug("Open connection to "+url.toString());
				URLConnection conn = url.openConnection();
				
				log.debug("Read response stream");
				InputStream responseStream = conn.getInputStream();
				String response = IOUtil.toString(responseStream);
				responseStream.close();
				
				log.info(response);
				
				log.debug("Process response stream");
				for (String line : response.split("[\r\n]+")) {
					log.info(line);
				}
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			} finally {
				try { tc.stop(); } catch (Exception tce) {log.debug("Error shutting down");}
			}
			
			tc.getServer().await();
			
			getLog().info("done.");
			
		} catch(Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
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
		

		
		if (!getLarStagingDir().exists())
			if (getProject().getPackaging().equals("lar"))
				throw new MojoExecutionException("Missing source for Lucee archive");
			else
				return false;
		
		
		return true;
	}

	private Integer getOpenPort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0);) {
			 return socket.getLocalPort();
		}
	}
	
}
