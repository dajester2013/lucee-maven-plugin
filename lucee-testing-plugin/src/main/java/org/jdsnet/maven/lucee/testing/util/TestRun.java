package org.jdsnet.maven.lucee.testing.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.jdsnet.maven.lucee.lar.util.CompileTimeMapping;

public class TestRun {
	Log log;
	MavenProject project;

	File sources;
	String sourceVirtualPath;
	String sourceType;

	File testSources;
	File reportsDir;
	File ccReportsDir;

	List<CompileTimeMapping> addtMappings;

	List<String> bundles;
	List<String> suites;
	List<String> specs;
	List<String> labels;

	File luceeRuntimeDir;
	File testRuntimeDir;
	String testboxVersion;
	String mockdataVersion;
	String cbstreamsVersion;

	boolean codeCoverage;
	boolean failIfNoTests;
	boolean ignoreFailures;

	public TestRun(MavenSession session, Log log) {
		this(session.getCurrentProject(), log);
	}
	public TestRun(MavenProject project, Log log) {
		this.project = project;
		this.log = log;
		sources = new File(project.getBasedir(), "src/main/cfml");
		testSources = new File(project.getBasedir(), "src/test/cfml");
		addtMappings = new ArrayList<>();
		reportsDir = new File(project.getBuild().getDirectory(),"test-reports");
		codeCoverage = true;
		failIfNoTests = ignoreFailures = false;
	}

	public TestRun luceeRuntime(File runtime) {
		this.luceeRuntimeDir = runtime;
		return this;
	}

	public TestRun testRuntime(File runtime) {
		this.testRuntimeDir = runtime;
		return this;
	}

	public TestRun sources(File src, String virtualPath, String type) {
		sources = src;
		sourceVirtualPath = virtualPath;
		sourceType = type;
		return this;
	}

	public TestRun testSources(File src) {
		testSources = src;
		return this;
	}

	public TestRun setBundles(List<String> bundles) {
		this.bundles = bundles;
		return this;
	}
	public TestRun setSuites(List<String> suites) {
		this.suites = suites;
		return this;
	}
	public TestRun setSpecs(List<String> specs) {
		this.specs = specs;
		return this;
	}
	public TestRun setLabels(List<String> labels) {
		this.labels = labels;
		return this;
	}

	public TestRun trackCodeCoverage() {
		return trackCodeCoverage(true);
	}
	public TestRun trackCodeCoverage(boolean cc) {
		codeCoverage=cc; 
		return this;
	}

	public TestRun additionalMappings(List<CompileTimeMapping> m) {
		this.addtMappings = m;
		return this;
	}

	public TestRun saveReportsTo(File dest) {
		if (!dest.exists()) dest.mkdirs();
		reportsDir = dest;
		if (ccReportsDir == null) {
			saveCoverageReportsTo(new File(dest, "coverage"));
		};
		return this;
	}

	public TestRun saveCoverageReportsTo(File dest) {
		if (!dest.exists()) dest.mkdirs();
		ccReportsDir = dest;
		return this;
	}

	public TestRun useTestBoxVersion(String version) {
		testboxVersion = version;
		return this;
	}

	public TestRun useTestBoxVersions(String testbox, String mockdata, String cbstreams) {
		testboxVersion = testbox;
		mockdataVersion = mockdata;
		cbstreamsVersion = cbstreams;
		return this;
	}

	public boolean hasBundles() {
		return bundles != null && bundles.size() > 0;
	}
	public boolean hasSuites() {
		return suites != null && suites.size() > 0;
	}
	public boolean hasSpecs() {
		return specs != null && specs.size() > 0;
	}
	public boolean hasLabels() {
		return labels != null && labels.size() > 0;
	}

}
