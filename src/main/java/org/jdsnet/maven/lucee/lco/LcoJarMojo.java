package org.jdsnet.maven.lucee.lco;

import org.apache.maven.plugins.annotations.Mojo;
import org.jdsnet.maven.lucee.jar.AbstractArchiveMojo;

@Mojo(name="lco-jar", threadSafe=true)
public class LcoJarMojo extends AbstractArchiveMojo {

	@Override
	protected String getType() {
		return "lco";
	}

	@Override
	protected String getFileExtension() {
		return "lco";
	}

}
