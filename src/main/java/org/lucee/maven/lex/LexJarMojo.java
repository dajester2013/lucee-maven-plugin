package org.lucee.maven.lex;

import org.apache.maven.plugins.annotations.Mojo;
import org.lucee.maven.jar.AbstractArchiveMojo;

@Mojo(name = "lex-jar", threadSafe = true)
public class LexJarMojo extends AbstractArchiveMojo {

	@Override
	protected boolean getAttachArtifact() {
		return false;
	}
	
}
