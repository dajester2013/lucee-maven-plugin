package org.lucee.maven.lar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.resources.ResourcesMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "lar-copy-sources", threadSafe = true)
public class LarCopySourcesMojo extends ResourcesMojo {

	/**
	 * Where to output the files that will be archived.
	 */
	@Parameter(defaultValue = "${project.build.directory}/archive", readonly = true, required = true)
	private File outputDirectory;

	/**
	 * A list of inclusion filters for the sourceDir.
	 */
	@Parameter
	private List<String> includes = new ArrayList<String>();

	/**
	 * A list of inclusion filters for the resourcesDir.
	 */
	@Parameter
	private List<String> includesResources = new ArrayList<String>();

	/**
	 * A list of exclusion filters for the sourceDir.
	 */
	@Parameter
	private List<String> excludes = new ArrayList<String>();

	/**
	 * A list of exclusion filters for the resourcesDir.
	 */
	@Parameter
	private List<String> excludesResources = new ArrayList<String>();

	/**
	 * The source directories containing the sources to be compiled.
	 */
	@Parameter(defaultValue = "src/main/lucee")
	private File sourceDir;

	@Parameter(defaultValue = "src/main/lar-resources")
	private File resourcesDir;

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public File getOutputDirectory() {
		return new File("target/archive");
	}

	public List<Resource> getResources() {
		List<Resource> resources = new ArrayList<Resource>();
		Resource r;

		includes.add("**/*.cfm");
		includes.add("**/*.cfml");
		includes.add("**/*.cfc");
		includes.add("**/*.lucee");
		includes.add("**/*.lc");

		if (sourceDir.exists()) {
			r = new Resource();
			r.setDirectory(sourceDir.getAbsolutePath());
			r.setFiltering(false);
			r.setIncludes(includes);
			r.setExcludes(excludes);
			resources.add(r);
		}
		
		if (resourcesDir.exists()) {
			r = new Resource();
			r.setDirectory(resourcesDir.getAbsolutePath());
			r.setFiltering(true);
			r.setIncludes(includesResources);
			r.setExcludes(excludesResources);
			resources.add(r);
		}

		return resources;
	}
}
