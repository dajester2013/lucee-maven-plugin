package org.jdsnet.maven.lucee.testing.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.stream.Collectors;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.jdsnet.maven.lucee.util.LuceeConfiguration;
import org.jdsnet.maven.lucee.util.LuceeConfiguration.Mapping;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.servlet.CFMLServlet;

public class Executor {
	
	public static boolean execute(TestRun testRun) throws MojoExecutionException, MojoFailureException {
		try {
			PrintStream err = System.err;
			PrintStream nul = new PrintStream(new ByteArrayOutputStream());
	
			System.setErr(nul);

			configureClassPath(testRun);
			
			installTestbox(testRun);
			populateWebroot(testRun);

			configureLucee(testRun);

			Tomcat tc = startServer(testRun);
			
			int status = runTests(testRun, tc.getConnector().getPort());
			
			if (status == -1)
				throw new MojoExecutionException("One or more tests errored");
			if (status == 0 && !testRun.ignoreFailures)
				throw new MojoFailureException("One or more tests failed");
			

			stopServer(tc);

			System.setErr(err);
		} catch(IOException e) {
			throw new MojoExecutionException("", e);
		}
		return false;
	}

	private static void installTestbox(TestRun testRun) throws IOException {
		if (testRun.testboxVersion != null) {
			TestboxComponents.TESTBOX.install(testRun.testRuntimeDir, testRun.testboxVersion);
			if (testRun.mockdataVersion != null)
				TestboxComponents.MOCKDATACFC.install(testRun.testRuntimeDir, testRun.mockdataVersion);
			if (testRun.cbstreamsVersion != null)
				TestboxComponents.CBSTREAMS.install(testRun.testRuntimeDir, testRun.cbstreamsVersion);
		} else {
			TestboxComponents.installAll(testRun.testRuntimeDir);
		}
	}

	private static void configureClassPath(TestRun testRun) {
		ClassRealm cl = (ClassRealm) Executor.class.getClassLoader();

		testRun.project.getArtifacts().stream()
			.filter(a ->a.getType().equals("jar"))
			.forEach(a -> {
				try {
					cl.addURL(a.getFile().getAbsoluteFile().toURI().toURL());
				} catch (IOException e) {
					throw new RuntimeException("failed to add archive to classpath: "+a.getFile(), e);
				}
			});
			;
	}

	private static void populateWebroot(TestRun testRun) throws IOException {
		URL res;
		File outFile;
		
		res = Executor.class.getResource("/test-runner.cfm");
		outFile = new File(testRun.testRuntimeDir, "test-runner.cfm");
		if (outFile.exists()) outFile.delete();
			FileUtils.copyURLToFile(res, outFile);
	}

	private static void configureLucee(TestRun testRun) throws MojoExecutionException, IOException {
		LuceeConfiguration lc = new LuceeConfiguration(new File(testRun.testRuntimeDir,"WEB-INF/lucee/lucee-web.xml.cfm"));
		
		testRun.project.getArtifacts().stream()
			.filter(a->a.getType().equals("lar"))
			.forEach(a-> {
				try {
					lc.addArtifactMapping(a);
				} catch(IOException e) {
					throw new RuntimeException("failed to add archive "+a.getFile());
				}
			})
			;
		
		if (testRun.testSources.exists()) {
			lc.addMapping("mappings", new Mapping(
				testRun.testSources.getAbsolutePath(), 
				"/tests", 
				"physical"
			));
		} else {
			new File(testRun.testRuntimeDir, "tests").mkdirs();
		}

		if (testRun.sources.exists()) {
			lc.addMapping(
				testRun.sourceType.equals("mapping") ? "mappings" : "component", 
				new Mapping(
					testRun.sources.getAbsolutePath(), 
					testRun.sourceVirtualPath, 
					"physical"
				)
			);
		}

		testRun.addtMappings
			.parallelStream()
			.forEach(am -> {
				lc.addMapping("cfc", new Mapping(
					am.getPhysical().getAbsolutePath(),
					am.getVirtual(), 
					am.getPhysical().getName().endsWith(".lar")	? "archive" : "physical"
				));
			});

		lc.write();
	}

	private static Tomcat startServer(TestRun testRun) throws IOException,MojoExecutionException {
		int port = getOpenPort();
		Tomcat tc = new Tomcat();
		
		System.getProperties().put("lucee.controller.disabled", "true");
		System.getProperties().put("catalina.home", testRun.project.getBuild().getDirectory());
		System.getProperties().put("catalina.base", testRun.project.getBuild().getDirectory());
		System.getProperties().put("lucee.server.dir", testRun.luceeRuntimeDir.getAbsolutePath());
		
		Connector c = new Connector("org.apache.coyote.http11.Http11Nio2Protocol");
		c.setProperty("address", "127.0.0.1");
		c.setPort(port);
		c.setProperty("relaxedQueryChars", "{}[]|<>\"");
		tc.setConnector(c);
		
		tc.setBaseDir(testRun.project.getBuild().getOutputDirectory());

		Context ctx = tc.addContext("/", testRun.testRuntimeDir.getAbsolutePath());
		Wrapper servletWrapper = Tomcat.addServlet(ctx, "CFMLServlet", new CFMLServlet());
		servletWrapper.setLoadOnStartup(1);
		servletWrapper.addMapping("*.cfm");
		servletWrapper.addMapping("*.cfc");
		
		testRun.log.info("Starting server on http://127.0.0.1:"+port+"...");
		try {
			tc.init();
			tc.start();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
		testRun.log.info("Ready to run tests.");
		
		return tc;
	}


	private static int runTests(TestRun testRun, int port) throws IOException {

		StringBuilder urlstr = new StringBuilder(); 

		urlstr.append("http://localhost:").append(port).append("/test-runner.cfm?directory=/tests&reportpath=").append(testRun.reportsDir.getAbsolutePath());
		urlstr.append("&codecoverage=true&codecoveragepath=").append(testRun.sources.getAbsolutePath());
		urlstr.append("&codecoveragereportpath=").append(testRun.ccReportsDir.getAbsolutePath());

		if(testRun.hasBundles())
			urlstr.append("&bundles=").append(testRun.bundles.stream().map(s->URLEncoder.encode(s)).collect(Collectors.joining(",")));
		if (testRun.hasSuites())
			urlstr.append("&suites=").append(testRun.suites.stream().map(s->URLEncoder.encode(s)).collect(Collectors.joining(",")));
		if (testRun.hasSpecs())
			urlstr.append("&specs=").append(testRun.specs.stream().map(s->URLEncoder.encode(s)).collect(Collectors.joining(",")));
		if (testRun.hasLabels())
			urlstr.append("&labels=").append(testRun.labels.stream().map(s->URLEncoder.encode(s)).collect(Collectors.joining(",")));

		System.out.println(urlstr);

		URLConnection testconn = new URL(urlstr.toString()).openConnection();
		
		// test-runner.cfm should set this header
		String statusHeader = testconn.getHeaderField("x-test-result-status");

		int status = statusHeader == null ? -1 : Integer.valueOf(statusHeader);

		return status;

	}


	private static void stopServer(Tomcat tc) throws MojoExecutionException {
		try {
			tc.stop();
			tc.getServer().await();

			/* clear out the CFMLEngineFactory after each run...because. */
			Field f = CFMLEngineFactory.class.getDeclaredField("singelton");
			if (f != null) {
				f.setAccessible(true);
				f.set(null, null);
			}
		} catch(LifecycleException|NoSuchFieldException|IllegalAccessException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private static Integer getOpenPort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0);) {
			return socket.getLocalPort();
		}
	}


}
