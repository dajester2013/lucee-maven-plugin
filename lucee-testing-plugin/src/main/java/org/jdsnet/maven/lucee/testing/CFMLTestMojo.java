package org.jdsnet.maven.lucee.testing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.jdsnet.maven.lucee.lar.util.CompileTimeMapping;
import org.jdsnet.maven.lucee.util.LuceeConfiguration;
import org.jdsnet.maven.lucee.util.LuceeConfiguration.Mapping;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.servlet.CFMLServlet;

@Mojo(
	 name			= "cfml-test"
	,defaultPhase	= LifecyclePhase.TEST
	,threadSafe		= true
	,requiresDependencyResolution = ResolutionScope.TEST
	,requiresDependencyCollection = ResolutionScope.TEST
)
public class CFMLTestMojo extends CommonTestConfigMojo {

	@Parameter(defaultValue="false")
	private boolean verbose;

	@Parameter
	private List<CompileTimeMapping> larCompileTimeMappings = new ArrayList<>();
	
	/**
	 * Where to place the lucee runtime files necessary for the compilation phase
	 */
	@Parameter(property="lucee.runtime.dir", defaultValue="${project.build.directory}/lucee", required=true)
	private File luceeRuntimeDirectory;
	
	@Parameter(defaultValue="component")
	private String larType;

	@Parameter(defaultValue="")
	private String larVirtualPath;
	
	/**
	 * Import the project
	 */
	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final Log log = getLog();

		PrintStream err = System.err;
		PrintStream nul = new PrintStream(new ByteArrayOutputStream());

		// System.setOut(nul);
		System.setErr(nul);

		// Set<Artifact> artifacts = getSession().getCurrentProject().getArtifacts();

		// Stream<Artifact> astr = artifacts.stream();
		// astr = astr.filter(a -> a.getArtifactId().equals("lucee-testing-testbox"));
		// boolean hasTestbox = astr.findFirst().isPresent();

		// if (isSkipTests()) {
		// 	log.info("CFML tests have been skipped.");
		// 	return;
		// }
		// if (!hasTestbox) {
		// 	log.warn("The lucee-testing-testbox dependency was not found, tests may fail due to missing testbox dependencies.");
		// }


		installTestbox();
		
		System.getProperties().put("lucee.controller.disabled", "true");
		System.getProperties().put("catalina.home", project.getBuild().getDirectory());
		System.getProperties().put("catalina.base", project.getBuild().getDirectory());
		System.getProperties().put("lucee.server.dir", luceeRuntimeDirectory.getAbsolutePath());
    
    	log.info("Initializing Lucee execution environment...");
		
    	try {
			int port = getOpenPort();
			Tomcat tc = new Tomcat();
			
			Connector c = new Connector("org.apache.coyote.http11.Http11Nio2Protocol");
			c.setPort(port);
			c.setProperty("relaxedQueryChars", "{}[]|<>\"");
			tc.setConnector(c);
			
			tc.setBaseDir(project.getBuild().getOutputDirectory());
			
			File webroot = getTestboxDirectory();
			if (!webroot.exists()) webroot.mkdirs();
			
			populateWebroot(webroot);
			
			LuceeConfiguration lc = new LuceeConfiguration(new File(webroot, "WEB-INF/lucee/lucee-web.xml.cfm"));

			project.getArtifacts().stream()
			
					// filter artifacts for runtime/compile scoped lar artifacts
					.filter(a ->
							a.getType().equals("lar")
						&&	(
								a.getScope().equals("runtime")
							|| 	a.getScope().equals("compile")
							|| 	a.getScope().equals("test")
						)
					)
					
					// process a component or custom tag mapping for the lucee config file
					.forEach(a->{
						try {
							lc.addArtifactMapping(a);
						} catch (IOException e) {
							throw new RuntimeException("failed to add archive + "+a.getFile(), e);
						}
					});

			ClassRealm classLoader = (ClassRealm)CFMLTestMojo.class.getClassLoader();
			project.getArtifacts().stream()
					.filter(a ->
						!a.getType().equals("lar")
					&&	(
							a.getScope().equals("runtime")
						|| 	a.getScope().equals("compile")
						|| 	a.getScope().equals("test")
					)
				)
				.forEach(a->{
					try {
						URL artifactUrl = a.getFile().getAbsoluteFile().toURI().toURL();
						classLoader.addURL(artifactUrl);
					} catch (IOException e) {
						throw new RuntimeException("failed to add archive + "+a.getFile(), e);
					}
				});
			
			Context ctx = tc.addContext("/", webroot.getAbsolutePath());
			Wrapper servletWrapper = Tomcat.addServlet(ctx, "CFMLServlet", new CFMLServlet());
			
			servletWrapper.setLoadOnStartup(1);
			servletWrapper.addMapping("*.cfm");

			Mapping srcMapping = new Mapping(
				this.getCfmlSourceDirectory().getAbsolutePath(),
				this.larVirtualPath,
				"physical"
			);
				lc.addMapping(this.larType.equals("mapping") ? "mappings" : "component", srcMapping);
			
			if (getTestsDirectory().exists()) {
				Mapping testsMapping = new Mapping(
					getTestsDirectory().getAbsolutePath(), 
					"/tests", 
					"physical"
				);
				lc.addMapping("mappings", testsMapping);
			} else {
				new File(webroot, "tests").mkdirs();
			}
			
			larCompileTimeMappings.parallelStream().map(
				m->new Mapping(
					m.getPhysical().getAbsolutePath(),
					m.getVirtual(),
					"physical"
				)
			).forEach(m -> lc.addMapping("cfc", m));
			
			lc.write();

			tc.init();
			tc.start();
				
			log.info("Running tests...");

			File reportsDir = new File(project.getBasedir(), "target/test-reports");
			reportsDir.mkdirs();
			File coverageReportsDir = new File(reportsDir, "coverage");
			coverageReportsDir.mkdirs();

			URLConnection testconn = new URL(String.format(
				"http://localhost:%d/test-runner.cfm?directory=/tests&reportpath=" + reportsDir.getAbsolutePath() + 
				"&codecoverage=true&codecoveragepath=" + getCfmlSourceDirectory().getAbsolutePath() + 
				"&codecoveragereportpath=" + coverageReportsDir.getAbsolutePath()

				,port
			)).openConnection();
			
			String statusHeader = testconn.getHeaderField("x-test-result-status");
			int status = statusHeader == null ? -1 : Integer.valueOf(statusHeader);

			if (status < 1) {
				if (status == 0)
					throw new MojoFailureException("One or more tests failed.");
				else
					throw new MojoExecutionException("One or more tests errored.");
			}

			tc.stop();
			tc.getServer().await();
			
			/* clear out the CFMLEngineFactory after each run...because. */
			Field f = CFMLEngineFactory.class.getDeclaredField("singelton");
			if (f != null) {
				f.setAccessible(true);
				f.set(null, null);
			}
			
			getLog().info("done.");

		} catch(Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} finally {
			System.setErr(err);
		}
	}
	
	private void populateWebroot(File webroot) throws IOException {
		URL res;
		File outFile;
		
		res = CFMLTestMojo.class.getResource("/test-runner.cfm");
		outFile = new File(webroot, "test-runner.cfm");
		if (outFile.exists()) outFile.delete();
		FileUtils.copyURLToFile(res, outFile);
	}
	
	private Integer getOpenPort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0);) {
			 return socket.getLocalPort();
		}
	}

	private final static String ORTUS_DISTRIBUTION = "https://downloads.ortussolutions.com/ortussolutions";

	private static enum Components {
		TESTBOX("/testbox/${version}/testbox-${version}.zip", ""),
		MOCKDATACFC("/coldbox-modules/MockDataCFC/${version}/MockDataCFC-${version}.zip", "testbox/system/modules/mockdatacfc"),
		CBSTREAMS("/coldbox-modules/cbstreams/${version}/cbstreams-${version}.zip", "testbox/system/modules/cbstreams");

		private String downloadPath;
		private String installPath;

		Components(String downloadPath, String installPath) {
			this.downloadPath = downloadPath;
			this.installPath = installPath;
		}

		public URL getDownloadUrl(String version) throws MalformedURLException {
			Map<String,String> p = new HashMap<>();
			p.put("version", version);
			StringSubstitutor sub = new StringSubstitutor(p);

			return new URL(ORTUS_DISTRIBUTION + sub.replace(downloadPath));
		}

		public boolean installed(File in) {
			return new File(in, name().toLowerCase()).exists();
		}

		public void install(File dest, String version, MavenProject project) throws IOException {
			if (installed(dest)) return;
			try {
				URL dlUrl = getDownloadUrl(version);


				File dlFile = new File(project.getBuild().getDirectory());
				String name = name().toLowerCase() + ".zip";
				dlFile = new File(dlFile, name);

				FileUtils.copyURLToFile(dlUrl, dlFile);

				if (!dest.exists()) {
					dest.mkdirs();
				}

				Path destPath = dest.toPath();
				ZipFile dlZip = new ZipFile(dlFile);
				Enumeration<? extends ZipEntry> entries = dlZip.entries();
				while(entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					Path extractPath = destPath.resolve(installPath).resolve(entry.getName());
					if (entry.isDirectory()) {
						Files.createDirectories(extractPath);
					} else {
						Files.createDirectories(extractPath.getParent());
						try (
							InputStream is = dlZip.getInputStream(entry);
							OutputStream os = new FileOutputStream(extractPath.toFile());
						) {
							IOUtils.copy(is, os);
						} 
					}

				}
				dlZip.close();
			} catch(MalformedURLException e) {
				throw new IOException("Could not download " + name() + ".", e);
			}
		}
	}
	
	public void installTestbox() throws MojoExecutionException, MojoFailureException {
		try {
			Components.TESTBOX.install(getTestboxDirectory(), getTestboxVersion(), project);
			Components.MOCKDATACFC.install(getTestboxDirectory(), getMockdataVersion(), project);
			Components.CBSTREAMS.install(getTestboxDirectory(), getCbstreamsVersion(), project);
		} catch(IOException e) {
			throw new MojoExecutionException("Could not install testbox components.", e);
		}
	}
}