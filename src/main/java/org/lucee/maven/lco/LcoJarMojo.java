package org.lucee.maven.lco;

import org.apache.maven.plugins.annotations.Mojo;
import org.lucee.maven.jar.AbstractArchiveMojo;

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
