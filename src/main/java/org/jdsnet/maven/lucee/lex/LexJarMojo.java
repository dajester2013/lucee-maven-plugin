package org.jdsnet.maven.lucee.lex;

import org.apache.maven.plugins.annotations.Mojo;
import org.jdsnet.maven.lucee.jar.AbstractArchiveMojo;

@Mojo(name = "lex-jar", threadSafe = true)
public class LexJarMojo extends AbstractArchiveMojo {

	@Override
	protected boolean getAttachArtifact() {
		return false;
	}
	
}
