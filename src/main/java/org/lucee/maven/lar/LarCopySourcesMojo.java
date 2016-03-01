package org.lucee.maven.lar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;

@Mojo(name = "lar-copy-sources")
public class LarCopySourcesMojo extends AbstractMojo {

	/**
	 * The source directories containing the sources to be compiled.
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true, required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "src/main/lucee")
	private File sourceDir;

	/**
	 * A list of inclusion filters for the compiler.
	 */
	@Parameter
	private Set<String> includes = new HashSet<String>();

	/**
	 * A list of exclusion filters for the compiler.
	 */
	@Parameter
	private Set<String> excludes = new HashSet<String>();

	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println("LAR Source: "+ sourceDir);
		System.out.println("LAR Out: "+ outputDirectory);
		
		includes.add("**/*.cfm");
		includes.add("**/*.cfml");
		includes.add("**/*.cfc");
		includes.add("**/*.lucee");
		includes.add("**/*.lc");

		SourceInclusionScanner scanner = new SimpleSourceInclusionScanner(includes, excludes);

		scanner.addSourceMapping(new SuffixMapping("",""));

		try {
			File rootFile = sourceDir;
			if (rootFile.isDirectory()) {
				for (File source : scanner.getIncludedSources(rootFile, null)) {
					File targetDir = new File(source.getParent().replace(sourceDir.getAbsolutePath(), outputDirectory.getAbsolutePath()));
					if (!source.getParentFile().equals(sourceDir) && !targetDir.exists()) {
						targetDir.mkdirs();
					}
					
					FileUtils.copyFileToDirectory(source, targetDir);
				}
			}
		} catch (InclusionScanException e) {
			throw new MojoExecutionException("Error scanning source root: \'" + sourceDir + "\'.", e);
		} catch (IOException e) {
			throw new MojoExecutionException("Unexpected IO exception.", e);
		} 
	}

}
