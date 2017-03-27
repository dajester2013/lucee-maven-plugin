package org.jdsnet.maven.lucee.testing;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(
	 name="test"
	,requiresDependencyResolution=ResolutionScope.TEST
	,threadSafe=true
)
public class LuceeTestingMojo extends AbstractMojo {
	

	@Parameter(defaultValue="${project.build.directory}/cfml-tests")
	private File targetTestCFMLDirectory;

	@Parameter(defaultValue="${project.build.directory}/cfml")
	private File targetCFMLDirectory;

	@Parameter(defaultValue="${project.build.directory}/cfml-test-reports")
	private File resultFolder;
	
	@Parameter(defaultValue="src/tests/cfml")
	private File testsRoot;
	
	@Parameter(defaultValue="false", property="skipTests")
	private boolean skipTests;

	@Parameter(defaultValue="false", property="maven.test.skip")
	private boolean skip;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("lucee testing mojo!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

}
