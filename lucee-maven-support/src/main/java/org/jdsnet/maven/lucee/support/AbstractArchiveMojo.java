package org.jdsnet.maven.lucee.support;

import java.io.File;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;

public abstract class AbstractArchiveMojo extends AbstractMojo {
	private static final String[] DEFAULT_EXCLUDES = new String[] { "**/package.html" };

	private static final String[] DEFAULT_INCLUDES = new String[] { "**/**" };

	/**
	 * List of files to include. Specified as fileset patterns which are
	 * relative to the input directory whose contents is being packaged into the
	 * archive.
	 */
	@Parameter
	private String[] includes;

	/**
	 * List of files to exclude. Specified as fileset patterns which are
	 * relative to the input directory whose contents is being packaged into the
	 * archive.
	 */
	@Parameter
	private String[] excludes;

	/**
	 * Directory containing the generated archive.
	 */
	@Parameter(defaultValue = "${project.build.directory}", required = true)
	private File outputDirectory;

	/**
	 * Name of the generated archive.
	 */
	@Parameter(alias = "jarName", property = "jar.finalName", defaultValue = "${project.build.finalName}")
	private String finalName;

	/**
	 * The Jar archiver.
	 */
	@Component(role = Archiver.class, hint = "jar")
	private JarArchiver jarArchiver;

	/**
	 * The {@link {MavenProject}.
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	/**
	 * The {@link MavenSession}.
	 */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	/**
	 * The archive configuration to use. See
	 * <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven
	 * Archiver Reference</a>.
	 */
	@Parameter
	private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

	/**
	 * Directory containing the classes and resource files that should be
	 * packaged into the archive.
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
	private File classesDirectory;

	/**
	 * Path to the default MANIFEST file to use. It will be used if
	 * <code>useDefaultManifestFile</code> is set to <code>true</code>.
	 *
	 * @since 2.2
	 */
	// CHECKSTYLE_OFF: LineLength
	@Parameter(defaultValue = "${project.build.outputDirectory}/META-INF/MANIFEST.MF", required = true, readonly = true)
	// CHECKSTYLE_ON: LineLength
	private File defaultManifestFile;

	/**
	 * Set this to <code>true</code> to enable the use of the
	 * <code>defaultManifestFile</code>.
	 *
	 * @since 2.2
	 */
	@Parameter(property = "jar.useDefaultManifestFile", defaultValue = "false")
	private boolean useDefaultManifestFile;

	/**
	 *
	 */
	@Component
	private MavenProjectHelper projectHelper;

	/**
	 * Require the jar plugin to build a new archive even if none of the contents
	 * appear to have changed. By default, this plugin looks to see if the
	 * output jar exists and inputs have not changed. If these conditions are
	 * true, the plugin skips creation of the jar. This does not work when other
	 * plugins, like the maven-shade-plugin, are configured to post-process the
	 * jar. This plugin can not detect the post-processing, and so leaves the
	 * post-processed jar in place. This can lead to failures when those plugins
	 * do not expect to find their own output as an input. Set this parameter to
	 * <tt>true</tt> to avoid these problems by forcing this plugin to recreate
	 * the jar every time.
	 */
	@Parameter(property = "jar.forceCreation", defaultValue = "false")
	private boolean forceCreation;

	/**
	 * Skip creating empty archives
	 */
	@Parameter(property = "jar.skipIfEmpty", defaultValue = "false")
	private boolean skipIfEmpty;

	/**
	 * Classifier to add to the artifact generated. If given, the artifact will
	 * be attached as a supplemental artifact. If not given this will create the
	 * main artifact which is the default behavior. If you try to do that a
	 * second time without using a classifier the build will fail.
	 */
	@Parameter(property = "maven.jar.classifier")
	private String classifier;

	/**
	 * Return the specific output directory to serve as the root for the
	 * archive.
	 */
	protected File getClassesDirectory() {
		return classesDirectory;
	}

	protected final MavenProject getProject() {
		return project;
	}

	/**
	 * Overload this to produce a jar with another classifier, for example a
	 * test-jar.
	 */
	protected String getClassifier() {
		return classifier;
	}

	/**
	 * Overload this to produce a test-jar, for example.
	 */
	protected String getType() {
		return "jar";
	}


	/**
	 * Overload this to produce a test-jar, for example.
	 */
	protected boolean getAttachArtifact() {
		return true;
	}
	
	protected String getFileExtension() {
		return "jar";
	}

	/**
	 * Returns the Jar file to generate, based on an optional classifier.
	 *
	 * @param basedir
	 *            the output directory
	 * @param finalName
	 *            the name of the ear file
	 * @param classifier
	 *            an optional classifier
	 * @return the file to generate
	 */
	protected File getJarFile(File basedir, String finalName, String classifier) {
		if (basedir == null) {
			throw new IllegalArgumentException("basedir is not allowed to be null");
		}
		if (finalName == null) {
			throw new IllegalArgumentException("finalName is not allowed to be null");
		}

		StringBuilder fileName = new StringBuilder(finalName);

		if (hasClassifier()) {
			fileName.append("-").append(classifier);
		}

		fileName.append("." + getFileExtension());

		return new File(basedir, fileName.toString());
	}

	/**
	 * Default Manifest location. Can point to a non existing file. Cannot
	 * return null.
	 */
	protected File getDefaultManifestFile() {
		return defaultManifestFile;
	}

	/**
	 * Generates the archive.
	 *
	 * @todo Add license files in META-INF directory.
	 */
	public File createArchive() throws MojoExecutionException {
		File jarFile = getJarFile(outputDirectory, finalName, getClassifier());

		MavenArchiver archiver = new MavenArchiver();

		archiver.setArchiver(jarArchiver);

		archiver.setOutputFile(jarFile);

		archive.setForced(forceCreation);

		try {
			File contentDirectory = getClassesDirectory();
			if (!contentDirectory.exists()) {
				getLog().warn("Archive will be empty - no content was marked for inclusion!");
			} else {
				archiver.getArchiver().addDirectory(contentDirectory, getIncludes(), getExcludes());
			}

			File existingManifest = getDefaultManifestFile();

			if (useDefaultManifestFile && existingManifest.exists() && archive.getManifestFile() == null) {
				getLog().info("Adding existing MANIFEST to archive. Found under: " + existingManifest.getPath());
				archive.setManifestFile(existingManifest);
			}

			archiver.createArchive(session, project, archive);

			return jarFile;
		} catch (Exception e) {
			// TODO: improve error handling
			throw new MojoExecutionException("Error assembling archive", e);
		}
	}

	/**
	 * Generates the archive.
	 *
	 */
	public void execute() throws MojoExecutionException {
		if (skipIfEmpty && (!getClassesDirectory().exists() || getClassesDirectory().list().length < 1)) {
			getLog().info("Skipping packaging of the " + getType());
		} else {
			File jarFile = createArchive();

			if (getAttachArtifact()) {
				if (hasClassifier()) {
					projectHelper.attachArtifact(getProject(), getType(), getClassifier(), jarFile);
				} else {
					if (projectHasAlreadySetAnArtifact()) {
						throw new MojoExecutionException("You have to use a classifier "
								+ "to attach supplemental artifacts to the project instead of replacing them.");
					}
					getProject().getArtifact().setFile(jarFile);
				}
			}
		}
	}

	private boolean projectHasAlreadySetAnArtifact() {
		if (getProject().getArtifact().getFile() != null) {
			return getProject().getArtifact().getFile().isFile();
		} else {
			return false;
		}
	}

	/**
	 * @return true in case where the classifier is not {@code null} and
	 *         contains something else than white spaces.
	 */
	protected boolean hasClassifier() {
		boolean result = false;
		if (getClassifier() != null && getClassifier().trim().length() > 0) {
			result = true;
		}

		return result;
	}

	private String[] getIncludes() {
		if (includes != null && includes.length > 0) {
			return includes;
		}
		return DEFAULT_INCLUDES;
	}

	private String[] getExcludes() {
		if (excludes != null && excludes.length > 0) {
			return excludes;
		}
		return DEFAULT_EXCLUDES;
	}

}
