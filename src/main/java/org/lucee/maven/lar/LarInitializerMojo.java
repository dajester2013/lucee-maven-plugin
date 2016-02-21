package org.lucee.maven.lar;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Changes the default compile source root from src/main/java to src/main/lucee
 * @author jesse.shafferz
 */
@Mojo(name="initialize-lar")
public class LarInitializerMojo extends AbstractMojo {

	/**
	 * The source directories containing the sources to be compiled.
	 */
	@Parameter(defaultValue = "${project.compileSourceRoots}", readonly = true, required = true)
	private List<String> compileSourceRoots;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (compileSourceRoots.size() == 1 && compileSourceRoots.get(0).endsWith("java")) {
			compileSourceRoots.remove(0);
		}
		
		compileSourceRoots.add(new File("src/main/lucee").getAbsolutePath());
	}

}
