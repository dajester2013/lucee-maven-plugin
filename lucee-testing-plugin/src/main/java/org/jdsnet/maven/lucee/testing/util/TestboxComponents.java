package org.jdsnet.maven.lucee.testing.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;

public enum TestboxComponents {
	TESTBOX(
		"/testbox/${version}/testbox-${version}.zip",
		"", 
		"4.5.0"),
	MOCKDATACFC(
		"/coldbox-modules/MockDataCFC/${version}/MockDataCFC-${version}.zip", 
		"testbox/system/modules/mockdatacfc", 
		"3.5.0"
	),
	CBSTREAMS(
		"/coldbox-modules/cbstreams/${version}/cbstreams-${version}.zip", 
		"testbox/system/modules/cbstreams",
		"1.5.0"
	);
	
	private final static String ORTUS_DISTRIBUTION = "https://downloads.ortussolutions.com/ortussolutions";

	private String downloadPath;
	private String installPath;
	private String defaultVersion;

	TestboxComponents(String downloadPath, String installPath, String defaultVersion) {
		this.downloadPath = downloadPath;
		this.installPath = installPath;
		this.defaultVersion = defaultVersion;
	}

	public URL getDownloadUrl() throws MalformedURLException {
		Map<String,String> p = new HashMap<>();
		p.put("version", defaultVersion);
		StringSubstitutor sub = new StringSubstitutor(p);

		return new URL(ORTUS_DISTRIBUTION + sub.replace(downloadPath));
	}

	public URL getDownloadUrl(String version) throws MalformedURLException {
		Map<String,String> p = new HashMap<>();
		p.put("version", version);
		StringSubstitutor sub = new StringSubstitutor(p);

		return new URL(ORTUS_DISTRIBUTION + sub.replace(downloadPath));
	}

	public boolean installed(File in) {
		return new File(in, name().toLowerCase()).exists();
	}

	public void install(File dest, String version) throws IOException {
		if (installed(dest)) return;
		try {
			URL dlUrl = getDownloadUrl(version);


			String name = name().toLowerCase() + ".zip";
			File dlFile = new File(dest, name);

			dlFile.getParentFile().mkdirs();
			FileUtils.copyURLToFile(dlUrl, dlFile);

			if (!dest.exists()) {
				dest.mkdirs();
			}

			Path destPath = dest.toPath();
			ZipFile dlZip = new ZipFile(dlFile);
			Enumeration<? extends ZipEntry> entries = dlZip.entries();
			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				Path extractPath = destPath.resolve(installPath).resolve(entry.getName());
				if (entry.isDirectory()) {
					Files.createDirectories(extractPath);
				} else {
					Files.createDirectories(extractPath.getParent());
					try (
						InputStream is = dlZip.getInputStream(entry);
						OutputStream os = new FileOutputStream(extractPath.toFile());
					) {
						IOUtils.copy(is, os);
					} 
				}

			}
			dlZip.close();
		} catch(MalformedURLException e) {
			throw new IOException("Could not download " + name() + ".", e);
		}
	}

	public static void installAll(File dest) throws IOException {
		for (TestboxComponents c : TestboxComponents.values()) {
			c.install(dest, c.defaultVersion);
		}
	}
}
