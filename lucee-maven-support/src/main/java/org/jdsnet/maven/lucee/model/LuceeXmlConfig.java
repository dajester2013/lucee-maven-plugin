package org.jdsnet.maven.lucee.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

@XmlRootElement(name = "cfLuceeConfiguration")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LuceeXmlConfig extends LuceeConfig {
	public static final ObjectMapper MAPPER = new XmlMapper()
		.registerModule(new JaxbAnnotationModule());
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(Include.NON_EMPTY)
	public static final class Component {
		@XmlAttribute(name="base")
		private String base;
		@XmlAttribute(name="base-cfml")
		private String baseCfml;
		@XmlAttribute(name="base-lucee")
		private String baseLucee;
		@XmlAttribute(name="data-member-default-access")
		private String dataMemberDefaultAccess;
		@XmlAttribute(name="dump-template")
		private String dumpTemplate;

		@JacksonXmlElementWrapper(useWrapping = false)
		private Set<Mapping> mapping=new HashSet<>();

		@JsonIgnore
		public Set<Mapping> getComponentMappings() {
			return mapping;
		}
	}
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static final class CustomTag {
		@JacksonXmlElementWrapper(useWrapping = false)
		private Set<Mapping> mapping=new HashSet<>();

		@JsonIgnore
		public Set<Mapping> getComponentMappings() {
			return mapping;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static final class Mappings {
		@JacksonXmlElementWrapper(useWrapping = false)
		public Set<Mapping> mapping;
	}

	public Component component = new Component();

	@XmlElement(name="custom-tag")
	public CustomTag customTag = new CustomTag();

	@JacksonXmlElementWrapper(localName = "mappings")
	@JacksonXmlProperty(localName = "mapping")
	public Set<Mapping> mappings = new HashSet<>();

	@JacksonXmlElementWrapper(localName = "extensions")
	@JacksonXmlProperty(localName = "rhextension")
	public Set<LuceeRhExtension> extensions = new HashSet<>();


	@Override
	public LuceeConfig addComponentMapping(Mapping mapping) {
		this.component.mapping.add(mapping);
		return this;
	}

	@Override
	public LuceeConfig addCustomTagMapping(Mapping mapping) {
		customTag.mapping.add(mapping);
		return this;
	}

	@Override
	public LuceeConfig addRegularMapping(Mapping mapping) {
		mappings.add(mapping);
		return this;
	}

	private Element initNode(Document doc, String name) {
		Element node = (Element)doc.getElementsByTagName(name).item(0);
		if (node == null) {
			node = doc.createElement(name);
			doc.getFirstChild().appendChild(node);
		}
		return node;
	}

	@Override
	public LuceeConfig addRhExtension(File extension) {
		try {
			extensions.add(LuceeRhExtension.from(extension));
		} catch(IOException e) {
			throw new RuntimeException("Failed to add extension", e);
		}

		return this;
	}
	
	private List<Element> children(Element node) {
		List<Element> nodes = new ArrayList<>();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
			if (children.item(i) instanceof Element)
			nodes.add((Element)children.item(i));
		return nodes;
	}


	@Override
	void write(ObjectWriter writer, File file) throws IOException {
		if (!file.exists()) throw new IOException("Lucee XML config file must already exist.  XML config can only modify in-place.");

		try {
			Document config = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder()
				.parse(file);

			Element component	= initNode(config, "component");
			Element customTag	= initNode(config, "custom-tag");
			Element regular		= initNode(config, "mappings");
			Element extensions	= initNode(config, "extensions");

			Function<Element,String> mappingId = c -> c.getAttribute("archive") + ":" 
				+ c.getAttribute("physical") + ":"
				+ c.getAttribute("primary");

			var curComponents = children(component).stream().map(mappingId).collect(Collectors.toList());
			var curCustomTags = children(customTag).stream().map(mappingId).collect(Collectors.toList());
			var curRegulars = children(regular).stream().map(mappingId).collect(Collectors.toList());
			var curExtensions = children(extensions).stream().map(c -> c.getAttribute("id"))
															.collect(Collectors.toList());

			this.component.mapping.forEach(m -> {
				var el = m.toXmlElement(config);
				if (!curComponents.contains(mappingId.apply(el)))
					component.appendChild(el);
			});
			this.customTag.mapping.forEach(m -> {
				var el = m.toXmlElement(config);
				if (!curCustomTags.contains(mappingId.apply(el)))
					customTag.appendChild(el);
			});
			this.mappings.forEach(m -> {
				var el = m.toXmlElement(config);
				if (!curRegulars.contains(mappingId.apply(el)))
					regular.appendChild(el);
			});

			this.extensions.forEach(e -> {
				if (curExtensions.contains(e.id)) return;

				extensions.appendChild(e.toXmlElement(config));
				try {
					// if (e.amf != null)
					// if (e.resource != null)
					if (e.search != null) {
						JsonNode node = LuceeJsonConfig.MAPPER.readValue(e.search, ArrayNode.class).get(0);
						if (node != null) {
							Element search = this.initNode(config, "search");
							search.setAttribute("engine-bundle-name", node.get("name").asText());
							search.setAttribute("engine-bundle-version", node.get("version").asText());
							search.setAttribute("engine-class", node.get("class").asText());
						}
					}
					if (e.orm != null) {
						JsonNode node = LuceeJsonConfig.MAPPER.readValue(e.orm, ArrayNode.class).get(0);
						if (node != null) {
							Element orm = this.initNode(config, "orm");
							orm.setAttribute("engine-bundle-name", node.get("name").asText());
							orm.setAttribute("engine-bundle-version", node.get("version").asText());
							orm.setAttribute("engine-class", node.get("class").asText());
						}
					}
					// if (e.webservice != null)
					// if (e.monitor != null)
					// if (e.cache != null) {
					// 	Element cache = this.initNode(config, "cache");
					// 	ArrayNode node = LuceeJsonConfig.MAPPER.readValue(e.cache, ArrayNode.class);
					// 	SystemStreamLog log = new SystemStreamLog();
					// 	log.info("wanted to add cache " +e.cache);
					// }
					// if (e.cacheHandler != null)
					// if (e.jdbc != null)
					// if (e.startupHook != null)
					if (e.eventGatewayInstance != null) {
						Element gateways = this.initNode(config, "gateways");
						ArrayNode node = LuceeJsonConfig.MAPPER.readValue(e.eventGatewayInstance, ArrayNode.class);
						node.forEach(egi -> {
							Element gw = config.createElement("gateway");
							var fields = egi.fields();
							while(fields.hasNext()) {
								var field = fields.next();
								gw.setAttribute(field.getKey(), field.getValue().asText());
							}
							gateways.appendChild(gw);
						});
					}

				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}

			});

			Transformer xfm = TransformerFactory.newInstance().newTransformer();
			xfm.setOutputProperty(OutputKeys.INDENT, "yes");

			FileWriter cfgWriter = new FileWriter(file);
			StreamResult cfgStreamResult = new StreamResult(cfgWriter);
			
			xfm.transform(new DOMSource(config), cfgStreamResult);
			cfgWriter.close();
		
		} catch(SAXException|ParserConfigurationException|TransformerException e) {
			throw new IOException(e);
		}
	}

}
