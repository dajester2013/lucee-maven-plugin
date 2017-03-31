package org.jdsnet.maven.lucee.lar.test;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.catalina.startup.Tomcat;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.utils.io.IOUtil;

@Mojo(
	 name="cfml-test"
	,requiresDependencyResolution=ResolutionScope.TEST
	,threadSafe=true
)
public class LuceeTestingMojo extends CommonTestConfigMojo {
	
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		Log log = getLog();
		
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("lets do some testing!");
		
		
		
		try {
			Tomcat tc = getTomcat();
			tc.start();
			startLuceeContext("/test", getTestboxDirectory(), getLuceeRuntimeDirectory());
			try {
				URL url = new URL(String.format(
					 "http://localhost:%d/test/testbox/test-runner/index.cfm"
					,getTCPort()
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
			} finally {
				//try {tc.stop();} catch(Exception shutdowne) {}
			}
			tc.getServer().await();
		} catch(Exception e) {
			throw new MojoExecutionException("Unexpected error", e);
		}

    	/*File lrdTest = new File(getLuceeRuntimeDirectory(), "/test");
    	System.getProperties().put("lucee.server.dir", lrdTest.getAbsolutePath());
    	System.getProperties().put("lucee.web.dir", lrdTest.getAbsolutePath());*/
		
		/*try {
			startLuceeContext("/test", webroot, luceeServerDir);
		} catch(Exception e) {
			throw new MojoExecutionException("Unexpected error", e);
		}*/
		
		
//		
//		Set<File> componentPaths = new HashSet<>();
//
//		componentPaths.add(getTargetCFMLDirectory());
//		componentPaths.add(getTestsDirectory());
//		componentPaths.add(getTestboxDirectory());
//		
//		StringBuilder cmd = new StringBuilder();
//		
//		cmd.append("try{admin	action=\"updatePassword\" type=\"web\" newPassword=\"password\";}catch(any e){/*password already set*/}").append("\n");
//		
//		for (File cpath : componentPaths) {
//			if (cpath.exists()) {
//				cmd.append("admin	action=\"updateMapping\""
//						+ "			type=\"web\"").append("\n");
//			
//			
//				if (cpath.isDirectory()) {
//					cmd.append("physical=\"").append(cpath.getAbsolutePath()).append("\"\n");
//				} else if (cpath.isFile()) {
//					cmd.append("archive=\"").append(cpath.getAbsolutePath()).append("\"\n");
//				}
//			}
//		}
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
		log.info("");
	}

}
