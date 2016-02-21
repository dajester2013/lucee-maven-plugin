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

@Mojo(name = "lar-sources")
public class LarSourcesMojo extends AbstractMojo {

	/**
	 * The source directories containing the sources to be compiled.
	 */
	@Parameter(defaultValue = "${project.compileSourceRoots}", readonly = true, required = true)
	private List<String> compileSourceRoots;

	/**
	 * The source directories containing the sources to be compiled.
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true, required = true)
	private String outputDirectory;

	@Parameter(defaultValue = "src/main/lucee")
	private String sourceDir;

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
		includes.add("**/*.cfm");
		includes.add("**/*.cfml");
		includes.add("**/*.cfc");
		includes.add("**/*.lucee");
		includes.add("**/*.lc");

		SourceInclusionScanner scanner = new SimpleSourceInclusionScanner(includes, excludes);

		scanner.addSourceMapping(new SuffixMapping("",""));

		InputStream is=null;
		OutputStream os=null;
		for (String root : compileSourceRoots) {
			try {
				File rootFile = new File(root);
				if (rootFile.isDirectory()) {
					for (File source : scanner.getIncludedSources(rootFile, null)) {
						if (!source.getParentFile().equals(root)) {
							new File(source.getParent().replace(root, outputDirectory)).mkdirs();
						}
						
						try {
							is = new FileInputStream(source);
							os = new FileOutputStream(new File(source.getAbsolutePath().replace(root, outputDirectory)));
							byte[] buffer = new byte[1024];
					        int length;
					        while ((length = is.read(buffer)) > 0) {
					            os.write(buffer, 0, length);
					        }
					    } finally {
						    is.close();
					        os.close();
						}
					}
				}
			} catch (InclusionScanException e) {
				throw new MojoExecutionException("Error scanning source root: \'" + root + "\'.", e);
			} catch (FileNotFoundException e) {
				throw new MojoExecutionException("Error writing file.", e);
			} catch (IOException e) {
				throw new MojoExecutionException("Error in IO.", e);
			} 
		}
	}

}
