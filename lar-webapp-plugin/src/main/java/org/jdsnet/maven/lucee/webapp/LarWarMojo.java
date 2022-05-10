package org.jdsnet.maven.lucee.webapp;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Deprecated
@Mojo(
	name = "lar-war-attach", 
	requiresProject=true, 
	requiresDependencyCollection = ResolutionScope.TEST,
	requiresDependencyResolution=ResolutionScope.TEST
)
public class LarWarMojo extends AttachDependenciesMojo {
	
}
