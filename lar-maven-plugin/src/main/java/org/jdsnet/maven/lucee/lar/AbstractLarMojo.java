package org.jdsnet.maven.lucee.lar;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import lucee.loader.servlet.CFMLServlet;

abstract public class AbstractLarMojo extends AbstractMojo {

	/**
	 * A classifier to attach to the artifact
	 */
	@Parameter
	private String classifier;
	
	
	/**
	 * Import the project
	 */
	@Parameter(defaultValue="${project}", readonly=true, required=true)
	private MavenProject project;

	/**
	 * Import the project helper
	 */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Import the {@link MavenSession}.
     */
    @Parameter(defaultValue="${session}", readonly=true, required=true)
    private MavenSession session;
    
	/**
	 * Where to output processed CFML files.
	 */
	@Parameter(defaultValue="${project.build.directory}/cfml", required=true)
	private File cfmlOutputDir;
	
	/**
	 * Where to place the lucee runtime files necessary for the compilation phase
	 */
	@Parameter(property="lucee.runtime.dir", defaultValue="${project.build.directory}/lucee", required=true)
	private File luceeRuntimeDirectory;
	
	/**
	 * Whether to show verbose outut from the lucee build process.
	 */
	@Parameter(defaultValue="false")
	private boolean verbose;
	

	@Parameter(defaultValue="false")
	private boolean failOnError;
	

	
	public String getClassifier() {
		return classifier;
	}
	
	public MavenProject getProject() {
		return project;
	}

	public MavenProjectHelper getProjectHelper() {
		return projectHelper;
	}

	protected MavenSession getSession() {
		return session;
	}
	
	protected File getCFMLOutputDir() {
		return cfmlOutputDir;
	}
	
	protected File getLuceeRuntimeDirectory() {
		return luceeRuntimeDirectory;
	}
	
	protected boolean getVerbose() {
		return verbose;
	}
	
	
	
	private static Tomcat tcinstance;
	private static File tcbase;
	private static int tcport=-1;
	
	protected Tomcat getTomcat() throws IOException {
		if (tcinstance == null) {
			int port = getTCPort();
			tcinstance = new Tomcat();
			tcinstance.setPort(port);
			tcbase = new File(getProject().getBuild().getDirectory(), "/tc-"+port);
			if (!tcbase.exists()) tcbase.mkdirs();
			tcinstance.setBaseDir(tcbase.getAbsolutePath());
			getLog().info("Open tomcat on port "+port);
		}
		
		return tcinstance;
	}
	
	protected File getTCBase() throws IOException {
		getTomcat();
		return tcbase;
	}
	
	protected int getTCPort() throws IOException {
		if (tcport == -1) tcport = getOpenPort();
		return tcport;
	}
	
	protected void startLuceeContext(String contextPath, File webroot, File luceeServerDir) throws IOException, LifecycleException {
		getLog().info("Start lucee context "+contextPath);
		Tomcat tc = getTomcat();
		Context ctx = tc.addContext(contextPath, webroot.getAbsolutePath());
		Wrapper cfw = Tomcat.addServlet(ctx, "CFMLServlet", new CFMLServlet());
		cfw.setLoadOnStartup(1);
		//cfw.addInitParameter("lucee-server-directory", luceeServerDir.getAbsolutePath());
		cfw.addMapping("*.cfm");
		ctx.start();
	}
	

	private static Integer getOpenPort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0);) {
			 return socket.getLocalPort();
		}
	}

}
