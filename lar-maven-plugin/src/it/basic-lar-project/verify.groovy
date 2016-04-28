import org.codehaus.plexus.util.FileUtils;
import groovy.io.FileType;

// verify no build errors
String buildLog = FileUtils.fileRead(new File(basedir, "build.log"));
assert buildLog.contains("BUILD SUCCESS") : "build was not successful"


// verify output directory
File outputDir = new File(basedir, "/target");
assert outputDir.exists() : "No output directory found at "+outputDir.getCanonicalPath();


// verify lar file exists
File larFile = null;
outputDir.eachFileRecurse (FileType.FILES) { file ->
	if (file.getPath().endsWith("lar")) larFile = file;
}
assert larFile != null : "LAR file not built."