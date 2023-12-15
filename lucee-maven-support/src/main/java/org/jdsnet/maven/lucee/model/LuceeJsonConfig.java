package org.jdsnet.maven.lucee.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class LuceeJsonConfig extends LuceeConfig {
	public static final ObjectMapper MAPPER = new ObjectMapper()
		.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	
	@JsonAnyGetter
	@JsonAnySetter
	protected Map<String,Object> otherValues;

	@JsonProperty("componentMappings")
	@JsonInclude(value = Include.NON_ABSENT)
	private List<Mapping> componentMappings = new ArrayList<>();

	@JsonProperty("customTagMappings")
	private List<Mapping> customTagMappings = new ArrayList<>();

	@JsonProperty("mappings")
	private Map<String, Mapping> mappings = new HashMap<>();

	@JsonProperty("extensions")
	private List<LuceeRhExtension> extensions = new ArrayList<>();

	@Override
	public LuceeConfig addComponentMapping(Mapping mapping) {
		for (var m : componentMappings) {
			if (
				m.archive != null && m.archive.equals(mapping.archive) 
				|| 
				m.physical != null && m.physical.equals(mapping.physical)
			) {
				componentMappings.remove(m);
				break;
			}
		}
		componentMappings.add(mapping);
		return this;
	}

	@Override
	public LuceeConfig addCustomTagMapping(Mapping mapping) {
		for (var m : customTagMappings) {
			if (
				m.archive != null && m.archive.equals(mapping.archive) 
				|| 
				m.physical != null && m.physical.equals(mapping.physical)
			) {
				customTagMappings.remove(m);
				break;
			}
		}
		customTagMappings.add(mapping);
		return this;
	}

	@Override
	public LuceeConfig addRegularMapping(Mapping mapping) {
		mappings.put(mapping.virtual, mapping);
		return this;
	}

	@Override
	public LuceeConfig addRhExtension(File extension) {
		// nothing to do - they are automatically picked up and configured from the installed extensions directory.
		return this;
	}

}
