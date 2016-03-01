package org.lucee.maven.lex.packaging;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.lucee.maven.lex.Constants;

public class CopyDependenciesTask implements PackagingTask {

	public void doPackaging(PackagingContext context) throws Exception {
		File larsDir = new File(context.getExtensionDirectory(), Constants.DIR_ARCHIVES);
		File jarsDir = new File(context.getExtensionDirectory(), Constants.DIR_JARS);
		
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
		
		File scanDir = context.getOutputDirectory();
		File scanFile;
		for (String file : scanDir.list()) {
			if (file.endsWith(".lar"))
				FileUtils.copyFileToDirectory(new File(scanDir, file), larsDir);
			else if (file.endsWith(".jar"))
				FileUtils.copyFileToDirectory(new File(scanDir, file), jarsDir);
		}
	}

}
