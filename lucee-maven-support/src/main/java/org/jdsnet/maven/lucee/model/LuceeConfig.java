package org.jdsnet.maven.lucee.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public abstract class LuceeConfig {

	abstract public LuceeConfig addComponentMapping(Mapping mapping);
	abstract public LuceeConfig addCustomTagMapping(Mapping mapping);
	abstract public LuceeConfig addRegularMapping(Mapping mapping);

	public LuceeConfig addMapping(Mapping m) {
		switch(m.type) {
			case Cfc: return addComponentMapping(m);
			case CustomTag: return addCustomTagMapping(m);
			case Regular: return addRegularMapping(m);
		}
		return this;
	}

	public Mapping addLarMapping(File lar) throws IOException {
		Mapping mapping = Mapping.fromLar(lar);
		this.addMapping(mapping);
		return mapping;
	}

	public LuceeConfig addRhExtension(File extension) {
		return this;
	}

	public void write(ObjectMapper mapper, File toFile) throws IOException {
		this.write(mapper.writerWithDefaultPrettyPrinter(), toFile);
	}

	void write(ObjectWriter writer, File file) throws IOException {
		String out = writer.writeValueAsString(this);
		try (OutputStream fout = new FileOutputStream(file)) {
			IOUtils.write(out.getBytes("UTF-8"), fout);
		}
	}

}
