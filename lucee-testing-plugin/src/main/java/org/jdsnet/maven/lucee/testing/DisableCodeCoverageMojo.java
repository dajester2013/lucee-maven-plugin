package org.jdsnet.maven.lucee.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(
	 name			= "disable-codecoverage"
	,defaultPhase	= LifecyclePhase.GENERATE_RESOURCES
)
public class DisableCodeCoverageMojo extends CommonTestConfigMojo {

	

	/**
	 * Import the project
	 */
	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		File frJar =  new File(project.getBasedir(), ".mvn/fusionreactor/fusionreactor.jar");
		File jvmConf = new File(project.getBasedir(), ".mvn/jvm.config");

		try {
			if (jvmConf.exists()) {
				try (
					BufferedReader jvmConfReader = new BufferedReader(new FileReader(jvmConf));
					FileWriter jvmConfWriter = new FileWriter(jvmConf);
				) {
					String newJvmConf = jvmConfReader.lines()
						.map(line -> {
							return line.replace("-javaagent:.+fusionreactor.jar=[^\\s]+\\s*","");
						})
						.collect(Collectors.joining("\\n"));
					
					jvmConfWriter.write(newJvmConf);
				}
			}

			// if (frJar.exists()) {
			// 	FileUtils.deleteDirectory(frJar.getParentFile());
			// }
		} catch(IOException e) {
			throw new MojoExecutionException(e, "Error removing FusionReactor", "");
		}
	}
}