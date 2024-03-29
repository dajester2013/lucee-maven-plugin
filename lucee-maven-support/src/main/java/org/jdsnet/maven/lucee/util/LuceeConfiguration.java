package org.jdsnet.maven.lucee.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.squark.nestedjarclassloader.NestedJarURLConnection;

/**
 * @see LuceeConfigManager 
 */
@Deprecated
public class LuceeConfiguration {
	
	public static final class Mapping {

		public String archive;
		public String physical;
		public String virtual;
		public String primary;
		
		public Element el;

		public Mapping(String archive, String physical, String virtual, String primary) {
			this.archive = archive;
			this.physical = physical;
			this.virtual = virtual;
			this.primary = primary;
		}

		public Mapping(String source, String virtual, String primary) {
			this.virtual = virtual;
			this.primary = primary;
			if (primary.equals("archive")) {
				this.archive = source;
				this.physical = null;
			} else {
				this.archive = null;
				this.physical = source;
			}
		}
		
		public Mapping(Element el) {
			this.el = el;
			this.archive = el.getAttribute("archive");
			this.physical = el.getAttribute("physical");
			this.virtual = el.getAttribute("virtual");
			this.primary = el.getAttribute("primary");
		}

		public Element createElement(Document document) {
			Element _el = document.createElement("mapping");
			
			if (archive != null)
				_el.setAttribute("archive", archive);

			if (physical != null)
				_el.setAttribute("physical", physical);

			if (virtual != null)
				_el.setAttribute("virtual", virtual);

			if (primary != null)
				_el.setAttribute("primary", primary);

			return _el;
		}
		
		public void appendTo(Element el) {
			Element _el = this.createElement(el.getOwnerDocument());
			el.appendChild(_el);
		}


		public boolean equals(Mapping m) {
			if (	m.archive == this.archive
				||	m.physical == this.physical
				||	m.virtual == this.virtual)
				return true;
			
			return false;
		}

	}
	
	private static final Version LUCEE_VERSION = lucee.VersionInfo.getIntVersion();

	File configDest;
	Document config;

	ObjectMapper o = new ObjectMapper();

	public LuceeConfiguration(File dest) throws IOException, MojoExecutionException {
		this(dest, null);
	}

	public LuceeConfiguration(File dest, File source) throws IOException, MojoExecutionException {
		if (!dest.exists()) {
			writeConfigFile(dest, source);
		}
		
		this.configDest = dest;
		
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			config = db.parse(configDest);			
		} catch (Exception e) {
			throw new MojoExecutionException("Error parsing Lucee config.", e);
		}
	}
	
	private void writeConfigFile(File dest, File source) throws IOException, MojoExecutionException {
		if (!dest.getParentFile().exists())
			dest.getParentFile().mkdirs();
		
		if (source != null && source.exists()) {
			FileUtils.copyFile(source, dest.getParentFile());
		} else {
			boolean written = false;
			
			URL lco = LuceeConfiguration.class.getClassLoader().getResource("core/core.lco");
			if (lco != null) {
				URL webx = new URL(lco, "core.lco!/resource/config/web.xml");	
				try (
					NestedJarURLConnection webxNJURL=new NestedJarURLConnection(webx, false, false);
					FileOutputStream configOut = new FileOutputStream(dest);
				) {
					IOUtils.copy(webxNJURL.getInputStream(), configOut);
					written = true;
				}
			}
			
			if (!written) {
				throw new MojoExecutionException("Lucee web config file not available.");
			}
		}
	}
	
	
	private Set<Mapping> readMappings(Element el) {
		HashSet<Mapping> mappings = new HashSet<>();
		
		for (int x = 0; x <= el.getChildNodes().getLength(); x++) {
			Node n = el.getChildNodes().item(x);
			if (n instanceof Element && n.getNodeName().equals("mapping")) {
				mappings.add(new Mapping((Element)n));
			}
		}
		return mappings;
	}
	
	public void write() throws MojoExecutionException {
		try {
			Transformer xfm = TransformerFactory.newInstance().newTransformer();
			xfm.setOutputProperty(OutputKeys.INDENT, "yes");

			FileWriter cfgWriter = new FileWriter(configDest);
			StreamResult cfgStreamResult = new StreamResult(cfgWriter);
			
			xfm.transform(new DOMSource(config), cfgStreamResult);
			cfgWriter.close();
		} catch (IOException | TransformerFactoryConfigurationError | TransformerException e) {
			throw new MojoExecutionException("Error saving config", e);
		}
	}
	
	public void addMapping(String type, Mapping mapping) {
		Element parent = (Element)config.getElementsByTagName(type).item(0);

		Set<Mapping> _existing = readMappings(parent);
		Mapping[] existing = new Mapping[_existing.size()];
		existing = _existing.toArray(existing);
		int removed=0;
		for (int i=0; i < existing.length; i++) {
			Mapping _m = existing[i];
			if (_m.equals(mapping)) {
				parent.removeChild(parent.getChildNodes().item(i - removed++));
			}
		}

		mapping.appendTo(parent);
	}

	public Set<Mapping> getMappings(String type) {
		Element parent = (Element)config.getElementsByTagName(type).item(0);
		return readMappings(parent);
	}

	public void addArtifactMapping(Artifact a) throws IOException {
		if (a.getType().equals("lar")) {
			JarFile lar = new JarFile(a.getFile());
			Attributes attrs = lar.getManifest().getMainAttributes();
			lar.close();
			
			File libDir = new File(configDest.getParentFile(), "lib");
			if (!libDir.exists()) {
				libDir.mkdirs();
			}
			
			String archivePath = "{lucee-web}/lib/" + a.getFile().getName();
			String virtPath = coalesce(
				attrs.getValue("mapping-virtual-path"),
				"/"+a.getFile().getName()
			);
			
			switch(attrs.getValue("mapping-type")) {
				case "regular":
					addMapping("mappings", new Mapping(archivePath, null, virtPath, "archive"));
					break;
					
				case "cfc":
					addMapping("component", new Mapping(archivePath, null, virtPath, "archive"));
					break;
					
				case "custom-tag":
					addMapping("custom-tag", new Mapping(archivePath, null, virtPath, "archive"));
					break;

				default:
					System.out.println("Invalid mapping type "+attrs.getValue("mapping-type"));
					System.exit(100);
			}
			
			FileUtils.copyFileToDirectory(a.getFile(), libDir);
		}
	}
	
	

	private static String coalesce(String... values) {
		for (String val : values) {
			if (val != null)
				return val;
		}
		return null;
	}
}
