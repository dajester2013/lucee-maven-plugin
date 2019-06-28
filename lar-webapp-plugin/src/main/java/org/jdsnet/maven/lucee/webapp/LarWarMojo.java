package org.jdsnet.maven.lucee.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
		
		if (!lwc.exists()) {
			lwc.getParentFile().mkdirs();

			try {
				if (sourceLuceeWebConfig.exists()) {
					FileUtils.copyFile(sourceLuceeWebConfig, lwc.getParentFile());
				} else {
					boolean written = false;
					
					InputStream s = LarWarMojo.class.getClassLoader().getResourceAsStream("core/core.lco");
					
					if (s != null ) {
						ZipInputStream zin = new ZipInputStream(s);
						
						ZipEntry e;
						while ((e = zin.getNextEntry()) != null) {
							if (e.getName().equals("resource/config/web.xml")) {
								FileOutputStream lwcOut = new FileOutputStream(lwc);
		
								byte[] buf = new byte[1024];
								int len;
								while ((len = zin.read(buf)) != -1) {
									lwcOut.write(buf, 0, len);
								}
		
								lwcOut.close();
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
				
				File libDir = new File(webappDirectory, "WEB-INF/lib/");
				if (!libDir.exists())
					libDir.mkdirs();
				
				project.getArtifacts().stream()
				
						// filter artifacts for runtime/compile scoped lar artifacts
						.filter(a ->
								a.getType().equals("lar")
							&&	(
									a.getScope().equals("runtime")
								|| 	a.getScope().equals("compile")
							)
						)
						
						// process a component or custom tag mapping for the lucee config file
						.forEach(a -> {
							try {
								JarFile lar = new JarFile(a.getFile());

								Attributes attrs = lar.getManifest().getMainAttributes();

								String archivePath = "{web-root-directory}/WEB-INF/lib/" + a.getFile().getName();
								
								Element mapping = cfg.createElement("mapping");
								mapping.setAttribute("virtual", 
									coalesce(
										attrs.getValue("mapping-virtual-path"),
										"/"+a.getFile().getName()
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
									ctagEl.appendChild(mapping);
								}

								lar.close();
								
								FileUtils.copyFileToDirectory(a.getFile(), libDir);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						});
				
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
