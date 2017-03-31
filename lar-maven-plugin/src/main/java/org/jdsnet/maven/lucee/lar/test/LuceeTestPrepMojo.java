package org.jdsnet.maven.lucee.lar.test;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(
	name="cfml-test-prep"
)
public class LuceeTestPrepMojo extends CommonTestConfigMojo {

	private static final String BASE_DOWNLOAD_URL = "https://www.ortussolutions.com/parent/download/testbox";
	
	/**
	 * Which version of testbox to install.  Defaults to the latest release.
	 */
	@Parameter(defaultValue="")
	private String testboxVersion;

	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!getTestboxDirectory().exists()) {
			installTestbox();
		}
	}


	private void installTestbox() throws MojoExecutionException {
		
		try {
			if (testboxVersion == null)
				getLog().info("Downloading latest Testbox");
			else
				getLog().info("Downloading Testbox, version "+testboxVersion);
			
			ZipInputStream zin = openDownloadStream();
			
			ZipEntry entry;
			FileOutputStream eout;
			System.out.println("prepping to extract testbox");
			
			while((entry = zin.getNextEntry()) != null) {
				File dest = new File(getTestboxDirectory(), entry.getName());
				
				if (entry.isDirectory()) {
					dest.mkdirs();
				} else {
					eout = new FileOutputStream(dest);
					while(zin.available() > 0) {
						eout.write(zin.read());
					}
					eout.close();
					zin.closeEntry();
					eout = null;
				}
			}
			
			zin.close();
		} catch(Exception e) {
			throw new MojoExecutionException("Unexpected error", e);
		}
		
	}

	private ZipInputStream openDownloadStream() throws Exception {
		return openDownloadStream(BASE_DOWNLOAD_URL);
	}

	private ZipInputStream openDownloadStream(String url) throws Exception {
		URL baseUrl = new URL(url);
		
		if (testboxVersion != null) {
			baseUrl = new URL(baseUrl, "?version="+testboxVersion);
		}
		
		HttpURLConnection conn = (HttpURLConnection) baseUrl.openConnection();
		conn.setReadTimeout(90000);
		
		boolean redirect=false;
		int status = conn.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_TEMP
				|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)
			redirect = true;
		}
		
		if (redirect) {
			String newUrl = conn.getHeaderField("Location");
			return openDownloadStream(newUrl);
		}
		
		return new ZipInputStream(conn.getInputStream());
	}

}
