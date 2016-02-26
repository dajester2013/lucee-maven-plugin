package org.lucee.maven.lex.packaging;

import java.io.File;
import java.io.Writer;

import org.apache.maven.model.Model;
import org.apache.maven.plugins.shade.pom.PomWriter;
import org.codehaus.plexus.util.WriterFactory;

public class NoDepsPomTask implements PackagingTask {

	public void doPackaging(PackagingContext context) throws Exception {
		Model model = context.getProject().getOriginalModel();
		model.getDependencies().clear();

		File f = new File(context.getProject().getBasedir(), "target/pom.xml");
		Writer w = WriterFactory.newXmlWriter(f);
		PomWriter.write(w, model);
		context.getProject().setFile(f);
	}

}
