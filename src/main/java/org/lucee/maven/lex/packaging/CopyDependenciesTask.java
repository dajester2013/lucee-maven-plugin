package org.lucee.maven.lex.packaging;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

public class CopyDependenciesTask implements PackagingTask {

	private static String LAR_DIR = "archives";
	private static String JAR_DIR = "jars";
	
	public void doPackaging(PackagingContext context) throws Exception {
		File larsDir = new File(context.getOutputDirectory(), LAR_DIR);
		File jarsDir = new File(context.getOutputDirectory(), JAR_DIR);

		Artifact a;
		for (Object aObj : context.getArtifacts()) {
			if (aObj instanceof Artifact) {
				
				a = (Artifact)aObj;

				if (a.getType().equals("lar"))
					FileUtils.copyFileToDirectory(a.getFile(), larsDir);
				else if (a.getType().equals("jar"))
					FileUtils.copyFileToDirectory(a.getFile(), jarsDir);
				else if (a.getType().equals("lex"))
					// dont know how to do this better?
					System.out.println("[WARNING] Unable to include "+a.toString()+"");
					//throw new MojoFailureException("Lucee extension dependency "+a.toString()+" not included!", );
			}
		}
	}

}
