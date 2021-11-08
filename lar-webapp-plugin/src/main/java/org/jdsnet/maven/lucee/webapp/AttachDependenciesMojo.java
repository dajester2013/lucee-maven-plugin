package org.jdsnet.maven.lucee.webapp;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(
	name = "attach-dependencies",
	requiresProject = true,
	requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class AttachDependenciesMojo extends LarWarMojo {
	
}
