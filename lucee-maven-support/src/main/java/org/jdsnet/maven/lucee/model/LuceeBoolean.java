package org.jdsnet.maven.lucee.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum LuceeBoolean {
	@JsonProperty("true")
	TRUE,
	@JsonProperty("false")
	FALSE,
	@JsonProperty("yes")
	YES,
	@JsonProperty("no")
	NO;
}
