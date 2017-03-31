package org.jdsnet.maven.lucee.lar.test;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.jdsnet.maven.lucee.lar.AbstractLarMojo;

abstract class CommonTestConfigMojo extends AbstractLarMojo {
	

	@Parameter(defaultValue="${project.build.directory}/cfml-test-reports")
	private File resultFolder;
	
	@Parameter(defaultValue="src/tests/cfml")
	private File testsDirectory;
	
	@Parameter(defaultValue="false", property="skipTests")
	private boolean skipTests;

	@Parameter(defaultValue="false", property="maven.test.skip")
	private boolean skip;

	@Parameter(defaultValue="false", property="maven.test.failure.ignore")
	private boolean testFailureIgnore;

	@Parameter(defaultValue="false", property="failIfNoTests")
	private boolean failIfNoTests;
	
	@Parameter(defaultValue="${project.build.directory}/cfml-testbox")
	private File testboxDirectory;
	
	
	protected File getResultFolder() {
		return resultFolder;
	}

	protected File getTestsDirectory() {
		return testsDirectory;
	}

	protected boolean isSkipTests() {
		return skipTests;
	}

	protected boolean isSkip() {
		return skip;
	}

	protected boolean isTestFailureIgnore() {
		return testFailureIgnore;
	}

	protected boolean isFailIfNoTests() {
		return failIfNoTests;
	}
	
	protected File getTestboxDirectory() {
		return testboxDirectory;
	}

	
}
