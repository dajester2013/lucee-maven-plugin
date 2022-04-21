package org.jdsnet.maven.lucee.testing;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;
import org.jdsnet.maven.lucee.lar.AbstractLarMojo;

public abstract class CommonTestConfigMojo extends AbstractLarMojo {


	@Parameter(defaultValue="${project.build.directory}/test-reports")
	private File reportsDirectory;

	@Parameter(defaultValue="src/main/cfml")
	private File cfmlSourceDirectory;

	@Parameter(defaultValue="src/test/cfml")
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
	

	@Parameter(defaultValue="4.5.0")
	private String testboxVersion;
	@Parameter(defaultValue="3.5.0")
	private String mockdataVersion;
	@Parameter(defaultValue="1.5.0")
	private String cbstreamsVersion;
	
	protected File getReportsDirectory() {
		return reportsDirectory;
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
	
	protected File getCfmlSourceDirectory() {
		return cfmlSourceDirectory;
	}

	protected String getTestboxVersion() {
		return testboxVersion;
	}	

	protected String getMockdataVersion() {
		return mockdataVersion;
	}

	protected String getCbstreamsVersion() {
		return cbstreamsVersion;
	}
}
