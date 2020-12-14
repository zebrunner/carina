import java.nio.file.Path
import java.nio.file.Paths

import static org.apache.commons.io.FileUtils.deleteDirectory
import static org.apache.commons.io.FileUtils.cleanDirectory
import static org.apache.commons.io.FileUtils.copyDirectory

Path projectPath = Paths.get(request.outputDirectory, request.artifactId)
Properties properties = request.properties
String packageName = properties.get("package")
String packagePath = packageName.replace(".", "/")
boolean demo = Boolean.parseBoolean(properties.getProperty("demo"))

if (!demo) {
    copyDirectory (projectPath.resolve("src/main/java/$packagePath/carina/demo").toFile(),
            projectPath.resolve("src/main/java/$packagePath").toFile())
    deleteDirectory projectPath.resolve("src/main/java/$packagePath/carina").toFile()

    cleanDirectory projectPath.resolve("src/main/java/$packagePath/api").toFile()
    cleanDirectory projectPath.resolve("src/main/java/$packagePath/db/mappers").toFile()
    cleanDirectory projectPath.resolve("src/main/java/$packagePath/db/models").toFile()
    cleanDirectory projectPath.resolve("src/main/java/$packagePath/gui/components").toFile()
    cleanDirectory projectPath.resolve("src/main/java/$packagePath/gui/pages").toFile()
    cleanDirectory projectPath.resolve("src/main/java/$packagePath/mobile/gui/pages/android").toFile()
    cleanDirectory projectPath.resolve("src/main/java/$packagePath/mobile/gui/pages/common").toFile()
    cleanDirectory projectPath.resolve("src/main/java/$packagePath/mobile/gui/pages/ios").toFile()
    cleanDirectory projectPath.resolve("src/main/java/$packagePath/utils").toFile()

    cleanDirectory projectPath.resolve("src/main/resources/L10N").toFile()

    deleteDirectory projectPath.resolve("src/test/java/$packagePath/carina").toFile()

    cleanDirectory projectPath.resolve("src/test/resources/xls").toFile()
    cleanDirectory projectPath.resolve("src/test/resources/features").toFile()
    cleanDirectory projectPath.resolve("src/test/resources/testng_suites").toFile()
    cleanDirectory projectPath.resolve("src/test/resources/api").toFile()
}