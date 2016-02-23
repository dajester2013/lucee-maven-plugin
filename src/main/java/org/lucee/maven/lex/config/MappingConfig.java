package org.lucee.maven.lex.config;

import org.apache.maven.plugins.annotations.Parameter;

public class MappingConfig {
	private enum InspectTemplate {
		always,never,once;
	}
	private enum ListenerMode {
		current,currenttoroot,currentorroot,root;
	}
	private enum ListenerType {
		none,classic,modern,mixed;
	}
	
	
	@Parameter
	private String virtual;
	@Parameter
	private String physical;
	@Parameter
	private String archive;
	@Parameter
	private String primary;
	@Parameter
	private InspectTemplate inspect		= null;
	@Parameter
	private ListenerMode listenerMode	= null;
	@Parameter
	private ListenerType listenerType	= null;
	@Parameter
	private boolean topLevel;
	@Parameter
	private boolean readOnly;
}
