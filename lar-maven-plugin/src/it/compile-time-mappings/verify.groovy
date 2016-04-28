import org.codehaus.plexus.util.FileUtils;

// verify no build errors
String buildLog = FileUtils.fileRead(new File(basedir, "build.log"));
assert buildLog.contains("BUILD SUCCESS") : "build was not successful"