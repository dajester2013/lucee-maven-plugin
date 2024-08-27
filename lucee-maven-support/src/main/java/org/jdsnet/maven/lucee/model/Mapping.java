package org.jdsnet.maven.lucee.model;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Mapping {

	public static enum Primary {
		archive, physical;
	}

	public static enum Type {
		Cfc,
		Regular,
		CustomTag;

		public static Type from(String type) {
			switch(type.toLowerCase()) {
				case "customTag":
				case "custom-tag":
					return CustomTag;

				case "mapping":
				case "regular":
					return Regular;

				case "component":
				case "cfc":
				default:
					return Cfc;
			}
		}
	}

	@JsonIgnore
	public Type type = Type.Cfc;

	@JsonProperty("archive")
	@JacksonXmlProperty(isAttribute = true, localName = "archive")
	public String archive;

	@JsonProperty("physical")
	@JacksonXmlProperty(isAttribute = true, localName = "physical")
	public String physical;

	@JsonProperty("virtual")
	@JacksonXmlProperty(isAttribute = true, localName = "virtual")
	public String virtual;

	@JsonProperty("primary")
	@JacksonXmlProperty(isAttribute = true, localName = "primary")
	public Primary primary = Primary.archive;

	@JsonProperty("readonly")
	@JacksonXmlProperty(isAttribute = true, localName = "readonly")
	public LuceeBoolean readOnly = LuceeBoolean.YES;

	@JsonProperty("hidden")
	@JacksonXmlProperty(isAttribute = true, localName = "hidden")
	public LuceeBoolean hidden = LuceeBoolean.FALSE;

	@JsonProperty("toplevel")
	@JacksonXmlProperty(isAttribute = true, localName = "toplevel")
	public LuceeBoolean topLevel = LuceeBoolean.TRUE;

	@JsonProperty("trusted")
	@JacksonXmlProperty(isAttribute = true, localName = "trusted")
	public LuceeBoolean trusted = LuceeBoolean.TRUE;

	@JsonProperty("listenerMode")
	@JacksonXmlProperty(isAttribute = true, localName = "listener-mode")
	public String listenerMode;

	@JsonProperty("listenerType")
	@JacksonXmlProperty(isAttribute = true, localName = "listener-type")
	public String listenerType;

	@JsonProperty("inspectTemplate")
	@JacksonXmlProperty(isAttribute = true, localName = "inspect-template")
	public String inspectTemplate;


	public Element toXmlElement(Document doc) {
		Element node = doc.createElement("mapping");

		if (archive != null)
			node.setAttribute("archive", archive);
		if (physical != null)
			node.setAttribute("physical", physical);
		if (virtual != null)
			node.setAttribute("virtual", virtual);
		if (primary != null)
			node.setAttribute("primary", primary.name().toLowerCase());

		if (readOnly != null)
			node.setAttribute("readonly", readOnly.name().toLowerCase());
		if (hidden != null)
			node.setAttribute("hidden", hidden.name().toLowerCase());
		if (topLevel != null)
			node.setAttribute("toplevel", topLevel.name().toLowerCase());
		if (trusted != null)
			node.setAttribute("trusted", trusted.name().toLowerCase());

		
		if (listenerMode != null)
			node.setAttribute("listener-mode", listenerMode);
		if (listenerType != null)
			node.setAttribute("listener-type", listenerType);
		if (inspectTemplate != null)
			node.setAttribute("inspect-template", inspectTemplate);

		return node;
	}

	private static Object coalesce(Object... s) {
		for (Object _s : s) if (_s != null) return _s;
		return null;
	}

	public static Mapping fromLar(File lar) throws IOException {
		JarFile larJar = new JarFile(lar);
		Attributes larAttrs = larJar.getManifest().getMainAttributes();
		larJar.close();
		
		Mapping m = new Mapping();
		switch(larAttrs.getValue("mapping-type")) {
			case "cfc":			m.type = Type.Cfc;			break;
			case "regular":		m.type = Type.Regular;		break;
			case "custom-tag":	m.type = Type.CustomTag;	break;
		}

		m.primary	= coalesce(larAttrs.getValue("mapping-physical-first"), "false").equals("true")
						? Primary.physical
						: Primary.archive;
		m.virtual	= (String)coalesce(larAttrs.getValue("mapping-virtual-path"), "/"+lar.getName());
		m.readOnly	= coalesce(larAttrs.getValue("mapping-readonly"),"false").equals("true") ? LuceeBoolean.YES : LuceeBoolean.NO;
		m.hidden	= coalesce(larAttrs.getValue("mapping-hidden"),"false").equals("true") ? LuceeBoolean.YES : LuceeBoolean.NO;
		m.topLevel	= coalesce(larAttrs.getValue("mapping-top-level"),"true").equals("true") ? LuceeBoolean.TRUE : LuceeBoolean.FALSE;

		m.archive = lar.getAbsolutePath();

		return m;
	}

} 