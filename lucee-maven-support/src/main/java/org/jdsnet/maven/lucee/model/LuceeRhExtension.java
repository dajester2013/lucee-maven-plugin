package org.jdsnet.maven.lucee.model;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LuceeRhExtension {

	@JsonProperty("id")
	@JacksonXmlProperty(isAttribute = true, localName = "id")
	public String id;

	@JsonProperty("name")
	@JacksonXmlProperty(isAttribute = true, localName = "name")
	public String name;

	@JsonProperty("version")
	@JacksonXmlProperty(isAttribute = true, localName = "version")
	public String version;
	
	@JsonProperty("fileName")
	@JacksonXmlProperty(isAttribute = true, localName = "file-name")
	public String fileName;

	@JsonProperty("luceeCoreVersion")
	@JacksonXmlProperty(isAttribute = true, localName = "lucee-core-version")
	public String luceeCoreVersion;

	@JsonProperty("releaseType")
	@JacksonXmlProperty(isAttribute = true, localName = "release-type")
	public String releaseType="all";

	@JsonProperty("startBundles")
	@JacksonXmlProperty(isAttribute = true, localName = "start-bundles")
	public String startBundles="true";

	@JsonProperty("trial")
	@JacksonXmlProperty(isAttribute = true, localName = "trial")
	public String trial="false";

	@JsonProperty("amf")
	@JacksonXmlProperty(isAttribute = true, localName = "amf")
	public String amf;

	@JsonProperty("resource")
	@JacksonXmlProperty(isAttribute = true, localName = "resource")
	public String resource;

	@JsonProperty("search")
	@JacksonXmlProperty(isAttribute = true, localName = "search")
	public String search;

	@JsonProperty("orm")
	@JacksonXmlProperty(isAttribute = true, localName = "orm")
	public String orm;

	@JsonProperty("webservice")
	@JacksonXmlProperty(isAttribute = true, localName = "webservice")
	public String webservice;

	@JsonProperty("monitor")
	@JacksonXmlProperty(isAttribute = true, localName = "monitor")
	public String monitor;

	@JsonProperty("cache")
	@JacksonXmlProperty(isAttribute = true, localName = "cache")
	public String cache; 

	@JsonProperty("cacheHandler")
	@JacksonXmlProperty(isAttribute = true, localName = "cache-handler")
	public String cacheHandler; 

	@JsonProperty("jdbc")
	@JacksonXmlProperty(isAttribute = true, localName = "jdbc")
	public String jdbc; 

	@JsonProperty("startupHook")
	@JacksonXmlProperty(isAttribute = true, localName = "startup-hook")
	public String startupHook; 

	@JsonProperty("eventGatewayInstance")
	@JacksonXmlProperty(isAttribute = true, localName = "event-gateway-instance")
	public String eventGatewayInstance; 


	public Element toXmlElement(Document doc) {
		Element extEl = doc.createElement("rhextension");

		extEl.setAttribute("written-by", "lucee-maven-support");
		
		if (this.id != null) 					extEl.setAttribute("id", this.id);
		if (this.name != null) 					extEl.setAttribute("name", this.name);
		if (this.version != null)				extEl.setAttribute("version", this.version);
		if (this.fileName != null)				extEl.setAttribute("file-name", this.fileName);
		if (this.luceeCoreVersion != null)		extEl.setAttribute("lucee-core-version", this.luceeCoreVersion);
		if (this.releaseType != null)			extEl.setAttribute("release-type", this.releaseType);
		if (this.startBundles != null)			extEl.setAttribute("start-bundles", this.startBundles);
		if (this.trial != null)					extEl.setAttribute("trial", this.trial);
		if (this.amf != null)					extEl.setAttribute("amf", this.amf);
		if (this.resource != null)				extEl.setAttribute("resource", this.resource);
		if (this.search != null)				extEl.setAttribute("search", this.search);
		if (this.orm != null)					extEl.setAttribute("orm", this.orm);
		if (this.webservice != null)			extEl.setAttribute("webservice", this.webservice);
		if (this.monitor != null)				extEl.setAttribute("monitor", this.monitor);
		if (this.cache != null)					extEl.setAttribute("cache", this.cache);
		if (this.cacheHandler != null)			extEl.setAttribute("cache-handler", this.cacheHandler);
		if (this.jdbc != null)					extEl.setAttribute("jdbc", this.jdbc);
		if (this.startupHook != null)			extEl.setAttribute("startup-hook", this.startupHook);
		if (this.eventGatewayInstance != null)	extEl.setAttribute("event-gateway-instance", this.eventGatewayInstance);

		return extEl;
	}

	public static LuceeRhExtension from(File lex) throws IOException {
		try(JarFile lexJar = new JarFile(lex)) {
			return from(lexJar);
		}
	}
	public static LuceeRhExtension from(JarFile lex) throws IOException {
		var ext = new LuceeRhExtension();
		var attrs = lex.getManifest().getMainAttributes();

		ext.id						= dequote(attrs.getValue("id"));
		ext.name					= dequote(attrs.getValue("name"));
		ext.version					= dequote(attrs.getValue("version"));
		ext.fileName				= dequote(lex.getName().replaceAll(".+[\\\\\\/]([^\\\\\\/]+)$","$1"));
		ext.luceeCoreVersion		= dequote(attrs.getValue("lucee-core-version"));
		ext.releaseType				= dequote(coalesce(attrs.getValue("release-type"),"all"));
		ext.startBundles			= dequote(coalesce(attrs.getValue("start-bundles"),"true"));
		ext.trial					= dequote(coalesce(attrs.getValue("trial"),"false"));
		ext.amf						= dequote(attrs.getValue("amf"));
		ext.resource				= dequote(attrs.getValue("resource"));
		ext.search					= dequote(attrs.getValue("search"));
		ext.orm						= dequote(attrs.getValue("orm"));
		ext.webservice				= dequote(attrs.getValue("webservice"));
		ext.monitor					= dequote(attrs.getValue("monitor"));
		ext.cache					= dequote(attrs.getValue("cache"));
		ext.cacheHandler			= dequote(attrs.getValue("cache-handler"));
		ext.jdbc					= dequote(attrs.getValue("jdbc"));
		ext.startupHook				= dequote(attrs.getValue("startup-hook"));
		ext.eventGatewayInstance	= dequote(attrs.getValue("event-gateway-instance"));

		return ext;
	}

	public static String dequote(String quoted) {
		if (quoted == null) return null;
		return quoted.replaceAll("^\\\"|\\\"$", "");
	}

	public static <T> T coalesce(T ...items) {
		for (T i : items) if (i != null) return i;
		return null;
	}

}
