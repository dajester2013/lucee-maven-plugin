package org.jdsnet.maven.lucee.lar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.resources.ResourcesMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;

/**
 * Uses code borrowed from the ResourcesMojo to copy Lar sources to a build directory from which the LarMojo generates the archive.
 *
 */
@Mojo(name = "lar-copy-sources", threadSafe = true)
public class LarCopySourcesMojo extends AbstractLarMojo {

	/**
	 * The character encoding scheme to be applied when filtering resources.
	 */
	@Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}")
	private String encoding;

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
	 * Overwrite existing files even if the destination files are newer.
	 *
	 */
	@Parameter(property = "maven.resources.overwrite", defaultValue = "false")
	private boolean overwrite;

	/**
	 * Copy any empty directories included in the resources.
	 *
	 */
	@Parameter(property = "maven.resources.includeEmptyDirs", defaultValue = "false")
	protected boolean includeEmptyDirs;

	/**
	 * Whether to escape backslashes and colons in windows-style paths.
	 *
	 */
	@Parameter(property = "maven.resources.escapeWindowsPaths", defaultValue = "true")
	protected boolean escapeWindowsPaths;

	/**
	 * Expression preceded with the String won't be interpolated \${foo} will be
	 * replaced with ${foo}
	 *
	 */
	@Parameter(property = "maven.resources.escapeString")
	protected String escapeString;

	/**
	 * See: {@link ResourcesMojo#buildFilters}.
	 */
	@Parameter(defaultValue = "${project.build.filters}", readonly = true)
	protected List<String> buildFilters;

	/**
	 * See: {@link ResourcesMojo#filters}.
	 */
	@Parameter
	protected List<String> filters;

	/**
	 * See: {@link ResourcesMojo#useBuildFilters}.
	 */
	@Parameter(defaultValue = "true")
	protected boolean useBuildFilters;

	/**
	 * Whether or not to apply filtering to the {@link LarCopySourcesMojo#resourcesDir}
	 */
	@Parameter(defaultValue = "true")
	protected boolean filterResources;

	@Parameter
	protected List<String> delimiters;

	/**
	 */
	@Parameter(defaultValue = "true")
	protected boolean useDefaultDelimiters;

	@Parameter
	protected List<String> nonFilteredFileExtensions;

	/**
	 *
	 */
	@Component(role = MavenResourcesFiltering.class, hint = "default")
	protected MavenResourcesFiltering mavenResourcesFiltering;

	/**
	 * See: {@link ResourcesMojo#mavenFilteringHints}
	 */
	@Parameter
	private List<String> mavenFilteringHints;

	/**
	 */
	private PlexusContainer plexusContainer;

	/**
	 */
	private List<MavenResourcesFiltering> mavenFilteringComponents = new ArrayList<MavenResourcesFiltering>();

	/**
	 * The source directories containing the sources to be compiled.
	 */
	@Parameter(defaultValue = "src/main/lucee")
	private File sourceDir;

	@Parameter(defaultValue = "src/main/lar-resources")
	private File resourcesDir;

	/**
	 * stop searching endToken at the end of line
	 *
	 */
	@Parameter(property = "maven.resources.supportMultiLineFiltering", defaultValue = "false")
	private boolean supportMultiLineFiltering;

	public void contextualize(Context context) throws ContextException {
		plexusContainer = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
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
			r.setFiltering(filterResources);
			r.setIncludes(includesResources);
			r.setExcludes(excludesResources);
			resources.add(r);
		}

		return resources;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			if (StringUtils.isEmpty(encoding) && isFilteringEnabled(getResources())) {
				getLog().warn("File encoding has not been set, using platform encoding " + ReaderFactory.FILE_ENCODING
						+ ", i.e. build is platform dependent!");
			}

			MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution(getResources(),
					getLarOutputDirectory(), getProject(), encoding, filters, Collections.<String> emptyList(),
					getSession());

			mavenResourcesExecution.setEscapeWindowsPaths(escapeWindowsPaths);
			mavenResourcesExecution.setInjectProjectBuildFilters(useBuildFilters);
			mavenResourcesExecution.setEscapeString(escapeString);
			mavenResourcesExecution.setOverwrite(overwrite);
			mavenResourcesExecution.setIncludeEmptyDirs(includeEmptyDirs);
			mavenResourcesExecution.setSupportMultiLineFiltering(supportMultiLineFiltering);

			// if these are NOT set, just use the defaults, which are '${*}' and
			// '@'.
			if (delimiters != null && !delimiters.isEmpty()) {
				LinkedHashSet<String> delims = new LinkedHashSet<String>();
				if (useDefaultDelimiters) {
					delims.addAll(mavenResourcesExecution.getDelimiters());
				}

				for (String delim : delimiters) {
					if (delim == null) {
						// FIXME: ${filter:*} could also trigger this condition.
						// Need a better long-term solution.
						delims.add("${*}");
					} else {
						delims.add(delim);
					}
				}

				mavenResourcesExecution.setDelimiters(delims);
			}

			if (nonFilteredFileExtensions != null) {
				mavenResourcesExecution.setNonFilteredFileExtensions(nonFilteredFileExtensions);
			}
			mavenResourcesFiltering.filterResources(mavenResourcesExecution);

			executeUserFilterComponents(mavenResourcesExecution);

		} catch (MavenFilteringException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	protected void executeUserFilterComponents(MavenResourcesExecution mavenResourcesExecution)
			throws MojoExecutionException, MavenFilteringException {

		if (mavenFilteringHints != null) {
			for (String hint : mavenFilteringHints) {
				try {
					mavenFilteringComponents.add((MavenResourcesFiltering) plexusContainer
							.lookup(MavenResourcesFiltering.class.getName(), hint));
				} catch (ComponentLookupException e) {
					throw new MojoExecutionException(e.getMessage(), e);
				}
			}
		} else {
			getLog().debug("no use filter components");
		}

		if (mavenFilteringComponents != null && !mavenFilteringComponents.isEmpty()) {
			getLog().debug("execute user filters");
			for (MavenResourcesFiltering filter : mavenFilteringComponents) {
				filter.filterResources(mavenResourcesExecution);
			}
		}
	}

	/**
	 * Determines whether filtering has been enabled for any resource.
	 *
	 * @param resources
	 *            The set of resources to check for filtering, may be
	 *            <code>null</code>.
	 * @return <code>true</code> if at least one resource uses filtering,
	 *         <code>false</code> otherwise.
	 */
	private boolean isFilteringEnabled(Collection<Resource> resources) {
		if (resources != null) {
			for (Resource resource : resources) {
				if (resource.isFiltering()) {
					return true;
				}
			}
		}
		return false;
	}
}
