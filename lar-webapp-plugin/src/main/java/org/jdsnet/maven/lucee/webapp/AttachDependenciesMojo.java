package org.jdsnet.maven.lucee.webapp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.jdsnet.maven.lucee.util.LuceeConfigManager;
import org.jdsnet.maven.lucee.util.LuceeConfigManager.ConfigFormat;
import org.jdsnet.maven.lucee.util.LuceeConfigManager.ConfigLevel;
import org.osgi.framework.Version;

@Mojo(
	name = "attach-dependencies",
	requiresProject = true,
	requiresDependencyCollection = ResolutionScope.TEST,
	requiresDependencyResolution = ResolutionScope.TEST
)
public class AttachDependenciesMojo extends AbstractMojo {
	private Version luceeVersion = lucee.VersionInfo.getIntVersion();

	@Parameter(defaultValue = "src/main/webapp", required = true)
	private File warSourceDirectory;

	/**
	 * The directory where the webapp is built.
	 */
	@Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}", required = true)
	private File webappBuildDirectory;

	/**
	 * A source web config file
	 */
	@Parameter(defaultValue = "src/main/webapp/WEB-INF/lucee/lucee-web.xml.cfm")
	private File luceeConfigSource;

	@Parameter(defaultValue = "false", property = "lucee.attach.test-dependencies")
	private boolean includeTestArtifacts;

	@Component
	private MavenSession session;
	/**
	 * Import the project
	 */
	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	private LuceeConfigManager configManager;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		findLuceeVersion();

		try {
			initConfig();


			boolean isTest = includeTestArtifacts || session.getGoals().contains("test");
				
			Map<String, List<Artifact>> artifactsByType = project.getArtifacts().stream()
				.filter(artifact -> (
					artifact.getType().equals("lar") || artifact.getType().equals("lex")
				) && (
					isTest || artifact.getScope().equals("runtime") || artifact.getScope().equals("compile") || artifact.getScope().equals("system")
				))
				
				.collect(Collectors.groupingBy(artifact -> artifact.getType()));

				
			List<Artifact> empty = new ArrayList<>();
			getLog().info("Archives to attach: " + artifactsByType.getOrDefault("lar", empty).size());
			getLog().info("Extensions to attach: " + artifactsByType.getOrDefault("lex", empty).size());

			Function<Artifact,File> artifactFile = artifact -> {return artifact.getFile();};
			
			// INSTALL EXTENSIONS
			artifactsByType.getOrDefault("lar", empty).stream()
				.map(artifact -> {
					getLog().info("Process lucee archive "+artifact.toString());
					return artifact;
				})
				.map(artifactFile)
				.forEach(configManager::installLar);
			artifactsByType.getOrDefault("lex", empty).stream()
				.map(artifact -> {
					getLog().info("Process lucee extension "+artifact.toString());
					return artifact;
				})
				.map(artifactFile)
				.forEach(configManager::installLex);
		} catch(IOException e) {
			throw new MojoExecutionException("Error", e);
		}
	}

	private void findLuceeVersion() throws MojoExecutionException {
		getLog().info("Plugin Lucee Version: "+luceeVersion);
		Optional<Artifact> luceeDep = project.getArtifacts().stream()
				.filter(artifact -> (
					artifact.getGroupId().equals("org.lucee") && artifact.getArtifactId().equals("lucee")
				))
				.findFirst();
		
		if (luceeDep.isPresent()) try (
			URLClassLoader ucl = new URLClassLoader(new URL[] {luceeDep.get().getFile().toURI().toURL()});
		) {			
			String v = new String(ucl.getResourceAsStream("lucee/version").readAllBytes(),"UTF-8");
			v=v.substring(0,v.indexOf(":"));
			luceeVersion = Version.parseVersion(v);
		} catch(Exception e) {
			throw new MojoExecutionException(e);
		}
		getLog().info("Target Lucee Version: "+luceeVersion);
	}

	private void initConfig() throws IOException {
		File configDst=null;

		// a config file is specified.
		if (luceeConfigSource != null && luceeConfigSource.exists()) {
			Path srcCfgRelative = warSourceDirectory.toPath().relativize(luceeConfigSource.toPath());
			
			configDst = new File(webappBuildDirectory, srcCfgRelative.toString());

			FileUtils.copyFile(luceeConfigSource, configDst);

		// no explicit config file specified - try to discover in war source.
		} else {
			String[] testSrc = new String[] {
				"WEB-INF/lucee/lucee-web.xml.cfm",
				"WEB-INF/lucee-server/context/lucee-server.xml",
				"WEB-INF/lucee/.CFConfig.json",
				"WEB-INF/lucee-server/context/.CFConfig.json",
			};

			for (String src : testSrc) {
				File _testBld = new File(webappBuildDirectory, src);
				File _testSrc = new File(warSourceDirectory, src);
				// config already generated, use it.
				if (_testBld.exists()) {
					configDst = _testBld;
					break;
				// check if config exists in source, and copy to build dir.
				} else if (_testSrc.exists()) {
					FileUtils.copyFile(_testSrc, new File(webappBuildDirectory, src));
					configDst = _testSrc;
					break;
				}
			}
		}

		if (configDst == null) {
			configDst = webappBuildDirectory;
			if (!configDst.exists()) configDst.mkdirs();

			if (luceeVersion.getMajor() >= 6) {
				File dcf = ConfigFormat.JSON.getDefaultFile(ConfigLevel.SERVER);

				configManager = new LuceeConfigManager(ConfigLevel.SERVER, ConfigFormat.JSON, new File(configDst, dcf.toString()));
			} else {
				File dcf = ConfigFormat.XML.getDefaultFile(ConfigLevel.WEB);

				configManager = new LuceeConfigManager(ConfigLevel.WEB, ConfigFormat.XML, new File(configDst, dcf.toString()));
			} 
		} else {
			configManager = new LuceeConfigManager(configDst);
		}

	}
}
