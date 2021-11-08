package org.jdsnet.maven.lucee.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Deprecated
@Mojo(name = "lar-war-attach", requiresProject=true, requiresDependencyResolution=ResolutionScope.COMPILE_PLUS_RUNTIME)
public class LarWarMojo extends AbstractMojo {
	/**
	 * The directory where the webapp is built.
	 */
	@Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}", required = true)
	private File webappDirectory;

	/**
	 * A source web config file
	 */
	@Parameter(defaultValue = "src/main/webapp/WEB-INF/lucee/lucee-web.xml.cfm")
	private File sourceLuceeWebConfig;

	/**
	 * Import the project
	 */
	@Parameter(defaultValue = "${project}")
	private MavenProject project;
	
	private File getConfigDestination() {
		return new File(webappDirectory,"WEB-INF/lucee/lucee-web.xml.cfm");
	}

	private void writeLuceeWebConfig() throws MojoExecutionException {
		File lwc = getConfigDestination();
		File lwcdir = lwc.getParentFile();
		
		if (!lwc.exists()) {
			// just because the file doesn't exist doesn't mean the parent folder also does not exist.
			if (!lwcdir.exists())
			lwcdir.mkdirs();

			try {
				if (sourceLuceeWebConfig.exists()) {
					FileUtils.copyFile(sourceLuceeWebConfig, lwcdir);
				} else {
					boolean written = false;
					
					InputStream s = LarWarMojo.class.getClassLoader().getResourceAsStream("core/core.lco");
					
					if (s != null ) {
						ZipInputStream zin = new ZipInputStream(s);
						
						ZipEntry e;
						while ((e = zin.getNextEntry()) != null) {
							if (e.getName().equals("resource/config/web.xml")) try (
								FileOutputStream lwcOut = new FileOutputStream(lwc);
							){
								IOUtils.copy(zin, lwcOut);
								written = true;
								break;
							}
						}
						
						zin.close();
					}
					
					if (!written) {
						throw new MojoExecutionException("Lucee web config file not available.");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static HashSet<String> getMappings(Element el) {
		HashSet<String> mappings = new HashSet<>();

		for (int x = 0; x <= el.getChildNodes().getLength(); x++) {
			Node n = el.getChildNodes().item(x);
			if (n instanceof Element && n.getNodeName().equals("mapping")) {
				mappings.add(((Element) n).getAttribute("archive"));
			}
		}

		return mappings;
	}

	private static String coalesce(String... values) {
		for (String val : values) {
			if (val != null)
				return val;
		}
		return null;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (project.getPackaging().equals("war")) {
				writeLuceeWebConfig();
				File lwc = getConfigDestination();
				
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document cfg = db.parse(lwc);

				Element cfcEl = (Element) cfg.getElementsByTagName("component").item(0);
				Element ctagEl = (Element) cfg.getElementsByTagName("custom-tag").item(0);
				Element mapEl = (Element) cfg.getElementsByTagName("mappings").item(0);

				HashSet<String> cfcMappings = getMappings(cfcEl);
				HashSet<String> ctagMappings = getMappings(ctagEl);
				HashSet<String> regMappings = getMappings(mapEl);
				
				File luceeDir = new File(webappDirectory, "WEB-INF/lucee/");
				if (!luceeDir.exists()) luceeDir.mkdirs();

				File libDir = new File(luceeDir, "lib/");
				if (!libDir.exists()) libDir.mkdirs();

				File deployDir = new File(luceeDir, "deploy/");
				if (!deployDir.exists()) deployDir.mkdirs();
				
				File libraryDir = new File(luceeDir, "library/");
				if (!libraryDir.exists()) libraryDir.mkdirs();
				
				File fldDir = new File(libraryDir, "fld/");
				if (!fldDir.exists()) fldDir.mkdirs();
				File tldDir = new File(libraryDir, "tld/");
				if (!tldDir.exists()) tldDir.mkdirs();
				File tagDir = new File(libraryDir, "tag/");
				if (!tagDir.exists()) tagDir.mkdirs();
				File fnDir = new File(libraryDir, "function/");
				if (!fnDir.exists()) fnDir.mkdirs();
				
				File cmpDir = new File(luceeDir, "components/");
				if (!cmpDir.exists()) cmpDir.mkdirs();
				
				File ctxDir = new File(luceeDir, "context/");
				if (!ctxDir.exists()) ctxDir.mkdirs();

				
				Map<String, List<Artifact>> artifactsByType = project.getArtifacts().stream()
					.filter(artifact -> (
						artifact.getType().equals("lar") || artifact.getType().equals("lex")
					) && (
						artifact.getScope().equals("runtime") || artifact.getScope().equals("compile") || artifact.getScope().equals("system")
					))
					.collect(Collectors.groupingBy(artifact -> artifact.getType()));

					
				List<Artifact> empty = new ArrayList<>();
				getLog().info("Archives to attach: " + artifactsByType.getOrDefault("lar", empty).size());
				getLog().info("Extensions to attach: " + artifactsByType.getOrDefault("lex", empty).size());

				Function<Artifact,File> artifactFile = artifact -> {return artifact.getFile();};

				Consumer<File> installLar = artifact->{
					try {
						JarFile lar = new JarFile(artifact);

						Attributes attrs = lar.getManifest().getMainAttributes();

						String archivePath = "{lucee-web}/lib/" + artifact.getName();
						
						Element mapping = cfg.createElement("mapping");
						mapping.setAttribute("virtual", 
							coalesce(
								attrs.getValue("mapping-virtual-path"),
								"/"+artifact.getName()
							)
						);
						mapping.setAttribute("archive", archivePath);
						mapping.setAttribute("primary", "archive");

						if (coalesce(attrs.getValue("mapping-type"), "").equals("cfc")) {
							if (cfcMappings.contains(archivePath)) {
								lar.close();
								return;
							}
							cfcEl.appendChild(mapping);
						} else if (coalesce(attrs.getValue("mapping-type"), "").equals("custom-tag")) {
							if (ctagMappings.contains(archivePath)) {
								lar.close();
								return;
							}
							ctagEl.appendChild(mapping);
						} else if (coalesce(attrs.getValue("mapping-type"), "").equals("regular")) {
							if (regMappings.contains(archivePath)) {
								lar.close();
								return;
							}
							mapEl.appendChild(mapping);
						}

						lar.close();
						
						FileUtils.copyFileToDirectory(artifact, libDir);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				};

				Consumer<File> installLex = artifact->{
					try {
						JarFile lex = new JarFile(artifact);
					
						Enumeration<JarEntry> lexEntries = lex.entries();
						while(lexEntries.hasMoreElements()) {
							JarEntry entry = lexEntries.nextElement();
							if (entry.isDirectory()) continue;

							InputStream is = lex.getInputStream(entry);

							String itemPath = entry.getName();
							String realPath = itemPath.substring(itemPath.indexOf("/")+1);

							File destBase;

							if (itemPath.startsWith("flds")) 
								destBase = fldDir;
							else if (itemPath.startsWith("tlds")) 
								destBase = tldDir;
							else if (itemPath.startsWith("tags")) 
								destBase = tagDir;
							else if (itemPath.startsWith("jars")) 
								destBase = libDir;
							else if (itemPath.startsWith("functions"))
								destBase = fnDir;
							else if (itemPath.startsWith("components"))
								destBase = cmpDir;
							else if (itemPath.startsWith("archives"))
								destBase = new File(project.getBuild().getDirectory());
							else if (itemPath.startsWith("context"))
								destBase = ctxDir;
							else if (itemPath.startsWith("application"))
								destBase = webappDirectory;
							else
								continue;
							
							File dest = new File(destBase, realPath);
							if (dest.exists()) dest.delete();
							else dest.getParentFile().mkdirs();
							try (
								FileOutputStream out = new FileOutputStream(dest);
							){
								IOUtils.copy(is, out);
								
								if (itemPath.endsWith(".lar"))
									installLar.accept(dest);
							}

						}
					} catch (Exception e) {
						getLog().warn("Error configuring " + artifact.getName(), e);

						try {
							getLog().warn("Error configuring " + artifact.getName(), e);
							FileUtils.copyFileToDirectory(artifact, deployDir);
						} catch(IOException e2) {}
					}
				};

				// INSTALL EXTENSIONS
				artifactsByType.getOrDefault("lar", empty).stream()
					.map(artifact -> {
						getLog().info("Process lucee archive "+artifact.toString());
						return artifact;
					})
					.map(artifactFile)
					.forEach(installLar);
				artifactsByType.getOrDefault("lex", empty).stream()
					.map(artifact -> {
						getLog().info("Process lucee extension "+artifact.toString());
						return artifact;
					})
					.map(artifactFile)
					.forEach(installLex);
				
				Transformer xfm = TransformerFactory.newInstance().newTransformer();
				xfm.setOutputProperty(OutputKeys.INDENT, "yes");
				
				FileWriter cfgWriter = new FileWriter(lwc);
				StreamResult cfgStreamResult = new StreamResult(cfgWriter);
				
				xfm.transform(new DOMSource(cfg), cfgStreamResult);
				cfgWriter.close();
			}
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

}
