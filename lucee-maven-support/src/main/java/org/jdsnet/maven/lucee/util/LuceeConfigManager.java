package org.jdsnet.maven.lucee.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.core.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jdsnet.maven.lucee.model.LuceeConfig;
import org.jdsnet.maven.lucee.model.LuceeJsonConfig;
import org.jdsnet.maven.lucee.model.LuceeXmlConfig;
import org.jdsnet.maven.lucee.model.Mapping;
import org.jdsnet.maven.lucee.model.Mapping.Primary;
import org.osgi.framework.Version;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import io.squark.nestedjarclassloader.NestedJarURLConnection;

public class LuceeConfigManager {
	private static final Log log = new SystemStreamLog();

	public static enum ConfigFormat {
		XML, JSON;

		public File getDefaultFile(ConfigLevel level) {
			if (this == JSON)
				return new File(level.DefaultPath, level.JsonFile);
			else
				return new File(level.DefaultPath, level.XmlFile);
		}
	}

	public static enum ConfigLevel {
		SERVER("WEB-INF/lucee-server/context", "lucee-server.xml", ".CFConfig.json"), 
		WEB("WEB-INF/lucee", "lucee-web.xml.cfm", ".CFConfig.json");

		public final String DefaultPath;
		public final String XmlFile;
		public final String JsonFile;

		private ConfigLevel(String path, String xmlFileName, String jsonFileName) {
			DefaultPath = path;
			XmlFile = xmlFileName;
			JsonFile = jsonFileName;
		}
	}

	private static final Version LUCEE_VERSION = lucee.VersionInfo.getIntVersion();

	private static ConfigFormat DEFAULT_CONFIG_FMT = LUCEE_VERSION.getMajor() >= 6 ? ConfigFormat.JSON : ConfigFormat.XML;
	private static ConfigLevel DEFAULT_CONFIG_LVL = LUCEE_VERSION.getMajor() >= 6 ? ConfigLevel.SERVER : ConfigLevel.WEB;

	private static String LIB_DIR_NAME = "lib";

	private ConfigLevel 	configLevel;
	private ConfigFormat 	configFormat;
	private File 			configFile;

	private ObjectMapper	configMapper;
	private LuceeConfig		config;

	public LuceeConfigManager() throws IOException {
		configLevel = DEFAULT_CONFIG_LVL;
		configFormat = DEFAULT_CONFIG_FMT;
		configFile = configFormat.getDefaultFile(configLevel);
		initConfig();
	}

	public LuceeConfigManager(ConfigLevel level, ConfigFormat format, File cfgFile) {
		configLevel = level;
		configFormat = format;
		configFile = cfgFile;
	}

	public LuceeConfigManager(File configLocation) throws IOException {
		setupFromFile(configLocation);
		initConfig();
	}

	private void setupFromFile(File configLocation) {
		// If passed a config file, attempt to discover what file it is.
		if (configLocation.getName().matches(".+\\.(json|xml|cfm)$")) {
			this.configFile = configLocation;

			if (configLocation.getName().equals("lucee-server.xml")) {
				configLevel = ConfigLevel.SERVER;
				configFormat = ConfigFormat.XML;
			} else if (configLocation.getName().equals("lucee-web.xml.cfm")) {
				configLevel = ConfigLevel.WEB;
				configFormat = ConfigFormat.XML;
			} else if (configLocation.getName().equals(".CFConfig.json")) {
				configFormat = ConfigFormat.JSON;

				if (configLocation.getPath().contains("lucee-server"))
					configLevel = ConfigLevel.SERVER;
				else
					configLevel = ConfigLevel.WEB;
			}
		// Passed a directory,  Sheesh.  Sleuth out where the config file should end up going.
		} else {
			// existing directory, search for config file
			if (configLocation.exists()) {
				String[] standardConfigs = new String[] {
					"WEB-INF/lucee/lucee-web.xml.cfm",
					"WEB-INF/lucee-server/context/lucee-server.xml",
					"WEB-INF/lucee/.CFConfig.json",
					"WEB-INF/lucee-server/context/.CFConfig.json",
				};

				for (var stdcfg : standardConfigs) {
					File stdCfgFile = new File(configLocation, stdcfg);
					if (stdCfgFile.exists()) {
						setupFromFile(stdCfgFile);
						return;
					}
				}
			}
			// new directory, or no config found in the directory.
			configLevel = DEFAULT_CONFIG_LVL;
			configFormat = DEFAULT_CONFIG_FMT;
			configLocation.mkdirs();
			configFile = new File(configLocation, configFormat.getDefaultFile(configLevel).toString());
		}
	}

	public void initConfig() throws IOException {
		if (config != null) return;

		boolean written;
		if (!exists()) {
			written = false;
			// get template file from lco
			URL lco = LuceeConfigManager.class.getClassLoader().getResource("core/core.lco");
			if (lco != null) {
				// ensure dir exists
				if (!configFile.getParentFile().exists()) 
					configFile.getParentFile().mkdirs();

				URL template = new URL(lco, "core.lco!/resource/config/" + configLevel.name().toLowerCase() + "." + configFormat.name().toLowerCase());
				try (
					NestedJarURLConnection templateInternal=new NestedJarURLConnection(template, false, false);
					FileOutputStream configOut = new FileOutputStream(configFile);
				) {

					IOUtils.copy(templateInternal.getInputStream(), configOut);
					written = true;
				}
			}
		} else {
			written = true;
		}

		if (!written)
			throw new IllegalStateException("Could not initialize the config file.");
		else {
			Class<? extends LuceeConfig> formatClass;

			switch(configFormat) {
				default:
				case XML:
					formatClass = LuceeXmlConfig.class;
					configMapper = LuceeXmlConfig.MAPPER;
					break;

				case JSON:
					formatClass = LuceeJsonConfig.class;
					configMapper = LuceeJsonConfig.MAPPER;
					break;
			}

			config = configMapper.readValue(configFile, formatClass);
		}
	}

	private File getConfigDir() {
		return configFile.getParentFile();
	}

	private File getLarDir() {
		return new File(getConfigDir(), LIB_DIR_NAME);
	}

	public boolean exists() {
		return configFile.exists();
	}

	public void installLar(File lar) {
		try {
			initConfig();

			File larDest = new File(getLarDir(), lar.getName());
			larDest.getParentFile().mkdirs();

			try (
				InputStream larIn = new FileInputStream(lar);
				OutputStream larOut = new FileOutputStream(larDest);
			) {
				IOUtils.copy(larIn, larOut);

				Mapping mapping = Mapping.fromLar(larDest);

				mapping.archive = "{lucee-config}/"+LIB_DIR_NAME+"/"+larDest.getName();
				mapping.primary = Primary.archive;

				config.addMapping(mapping);

				this.write();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void installLex(File lex) {
		try {
			if (configFormat == ConfigFormat.XML){
				installLexXML(lex);
			} else {
			File extDir = new File(getConfigDir(), "extensions/installed/");
			if (!extDir.exists()) extDir.mkdirs();
			FileUtils.copyFileToDirectory(lex, extDir);
			
			try (
				FileOutputStream out = new FileOutputStream(new File(extDir, lex.getName().replace(".lex",".mf")));
				var lexJar = new JarFile(lex)
				) {
					lexJar.getManifest().write(out);
				}
				config.addRhExtension(new File(extDir, lex.getName()));
			}
			this.write();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void installLexXML(File lex) {
		File configDir = getConfigDir();

		// where to put the extension file after processing - must be marked as a RHextension in the config file also
		File extDir = new File(configDir, "extensions/installed/");
		if (!extDir.exists()) extDir.mkdirs();
		
		// holds jars and lars
		File libDir = new File(configDir, "lib/");
		
		// fallback for lex that errors during install
		File deployDir = new File(configDir, "deploy/");
		
		// library files - custom tags, custom functions, etc...
		File libraryDir = new File(configDir, "library/");
			File fldDir = new File(libraryDir, "fld/");
			File tldDir = new File(libraryDir, "tld/");
			File tagDir = new File(libraryDir, "tag/");
			File fnDir = new File(libraryDir, "function/");
		
		// component library
		File cmpDir = new File(configDir, "components/");
		
		// for admin context files.
		File ctxDir = new File(configDir, "context/");
		
		// event gateway files
		File egwDir = new File(configDir, "eventGateways/");
		
		// why anyone would do this, is beyond me.
		File wcdDir = new File(configDir, "web-context-deployment");
		File wadDir = new File(configDir, "web-deployment");
		


		try (
			JarFile lexJar = new JarFile(lex);
		) {
			
			Attributes lexAttrs = lexJar.getManifest().getMainAttributes();

			String extId = lexAttrs.getValue("id");
			String extName = lexAttrs.getValue("name");
						
			// Extract files to correct locations
			Enumeration<JarEntry> lexEntries = lexJar.entries();
			while(lexEntries.hasMoreElements()) {
				JarEntry entry = lexEntries.nextElement();
				if (entry.isDirectory()) continue;

				InputStream is = lexJar.getInputStream(entry);

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
					// archives have a custom install, but must exist on disk.
					destBase = Files.createTempDirectory("").toFile();
				else if (itemPath.startsWith("context"))
					destBase = ctxDir;
				else if (itemPath.startsWith("configs"))
					destBase = getConfigDir();
				else if (itemPath.startsWith("webcontexts") || itemPath.startsWith("web.contexts"))
					destBase = wcdDir;
				else if (itemPath.startsWith("application"))
					destBase = wadDir;
				else if (itemPath.startsWith("plugin"))
					throw new Exception("Plugin lex has to be deployed");
				else
					continue;
				
				File dest = new File(destBase, realPath);
				if		(dest.exists()) dest.delete();
				else	dest.getParentFile().mkdirs();
				
				try (
					FileOutputStream out = new FileOutputStream(dest);
				){
					IOUtils.copy(is, out);
					
					if (itemPath.endsWith(".lar")) {
						installLar(dest);
						// was extracted to a temp dir, delete temp dir.
						try {
							FileUtils.forceDeleteOnExit(destBase);
						} catch(IOException e) {
							log.warn("Error cleaning up temp directory "+destBase.toString());
						}
					}
				}

			}

			FileUtils.copyFileToDirectory(lex, extDir);
			FileOutputStream out = new FileOutputStream(new File(extDir, lex.getName().replace(".lex",".mf")));
			lexJar.getManifest().write(out);
			out.close();
			config.addRhExtension(new File(extDir, lex.getName()));
		} catch (Exception e) {
			// try {
			// 	log.warn("Extension "+lex+" could not be pre-loaded.  It will be placed in the deploy folder.");
			// 	FileUtils.copyFileToDirectory(lex, deployDir);
			// } catch(IOException e2) {
			//	throw new RuntimeException("Could not deploy lex", e2);
			// }

			throw new RuntimeException("Could not deploy lex", e);
		}
	}

	public LuceeConfig getConfig() {
		return config;
	}

	public synchronized void write() throws IOException {
		config.write(configMapper, configFile);
	}

	public static void main(String[] args) throws IOException {
		var lcm = new LuceeConfigManager();
		lcm.installLar(new File("/home/jesse.shaffer@iss.internal/.m2/repository/com/isg/pcsii/aries/legacy-test-api/1.0.0/legacy-test-api-1.0.0.lar"));
		lcm.installLar(new File("/home/jesse.shaffer@iss.internal/.m2/repository/com/isg/pcsii/module/scheduledtasks/1.0.3/scheduledtasks-1.0.3.lar"));

		lcm.installLex(new File("/home/jesse.shaffer@iss.internal/workspace/aries/apps.pcsii.afmd/webapp/src/main/webapp/WEB-INF/lucee/deploy/cfspreadsheet-lucee-5.lex"));
		
	}

}
