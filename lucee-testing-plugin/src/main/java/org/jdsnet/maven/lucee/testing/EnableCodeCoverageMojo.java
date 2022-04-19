package org.jdsnet.maven.lucee.testing;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(
	 name			= "enable-codecoverage"
	,defaultPhase	= LifecyclePhase.GENERATE_RESOURCES
)
public class EnableCodeCoverageMojo extends CommonTestConfigMojo {

	private static final String FR_DOWNLOAD_URL = "https://download.fusionreactor.io/FR/Latest/fusionreactor.jar";
	/**
	 * Import the project
	 */
	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			URL frUrl = new URL(FR_DOWNLOAD_URL);
			File frJar =  new File(project.getBasedir(), ".mvn/fusionreactor/fusionreactor.jar");
			File jvmConf = new File(project.getBasedir(), ".mvn/jvm.config");

			frJar.getParentFile().mkdirs();
			jvmConf.getParentFile().mkdirs();

			if (!frJar.exists()) {
				getLog().info("Downloading FusionReactor...");
				CompletableFuture<Boolean> downloadTask = CompletableFuture.supplyAsync(() -> {
					try {
						FileUtils.copyURLToFile(frUrl,frJar);
						return true;
					} catch(IOException e) {
						return false;
					}
				});

				while(!downloadTask.isDone()) {
					System.out.print("\r" + frJar.getAbsolutePath() + " :: " + frJar.length() / 1024L + "kb");
					Thread.sleep(1000);
				}
				System.out.print("\r");
			}

			getLog().info("Adding FusionReactor JavaAgent to project...");
			
			String conf = "";
			
			if (jvmConf.exists())
				conf = FileUtils.readFileToString(jvmConf, "UTF-8");
			
			if (!conf.matches(".*-javaagent:[^\\s]+fusionreactor.*"))
				FileUtils.write(jvmConf, " -javaagent:"+frJar.getAbsolutePath()+"=name=test,address=10011 ", "UTF-8", true);
		} catch(IOException | InterruptedException e) {
			throw new MojoExecutionException(e, "Error downloading fusionreactor", "");
		}
	}
}