package org.jdsnet.maven.lucee.testing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.jdsnet.maven.lucee.lar.util.CompileTimeMapping;
import org.jdsnet.maven.lucee.testing.util.TestExecutor;
import org.jdsnet.maven.lucee.testing.util.TestRun;

@Mojo(
	 name			= "cfml-test"
	,defaultPhase	= LifecyclePhase.TEST
	,threadSafe		= true
	,requiresDependencyResolution = ResolutionScope.TEST
	,requiresDependencyCollection = ResolutionScope.TEST
)
public class CFMLTestMojo extends CommonTestConfigMojo {

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

	@Parameter(defaultValue="", property="test.bundles")
	private List<String> bundles;
	@Parameter(defaultValue="", property="test.suites")
	private List<String> suites;
	@Parameter(defaultValue="", property="test.specs")
	private List<String> specs;
	@Parameter(defaultValue="", property="test.labels")
	private List<String> labels;
	
	/**
	 * Import the project
	 */
	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.isSkipTests()) {
			getLog().info("Lucee tests have been skipped.");
			return;
		} else if (!getTestsDirectory().exists()) {
			getLog().info("No CFML tests to run.");
			return;
		}

		TestRun run = new TestRun(project, getLog())
			.luceeRuntime(luceeRuntimeDirectory)
			.testRuntime(getTestboxDirectory())
			.sources(getCfmlSourceDirectory(), larVirtualPath, larType)
			.testSources(getTestsDirectory())
			.additionalMappings(larCompileTimeMappings)
			.trackCodeCoverage()
			.saveReportsTo(getReportsDirectory())
			;

		if (bundles.size() > 0)
			run.setBundles(bundles);
		if (suites.size() > 0)
			run.setSuites(suites);
		if (specs.size() > 0)
			run.setSpecs(specs);
		if (labels.size() > 0)
			run.setLabels(labels);

		TestExecutor.execute(run);
	}

}