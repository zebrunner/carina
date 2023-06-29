#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;


import com.amazonaws.regions.Regions;
import com.zebrunner.carina.amazon.AmazonS3Manager;
import com.zebrunner.carina.amazon.config.AmazonConfiguration;
import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.report.SessionContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class AmazonS3Test implements IAbstractTest {

    private static final String FILE_NAME = "carinademoexample.apk";
    private static final String BUCKET_NAME = "qaprosoft";

    @Test
    public void amazonS3DownloadTest() {
        R.CONFIG.put(AmazonConfiguration.Parameter.S3_REGION.getKey(), Regions.US_WEST_2.getName(), true);
        AmazonS3Manager amazonS3Manager = AmazonS3Manager.getInstance();
        Path artifact = SessionContext.getArtifactsFolder().resolve(FILE_NAME);
        amazonS3Manager.download("qaprosoft", FILE_NAME, artifact.toFile());
        Assert.assertTrue(Files.exists(artifact), "Artifact should exists");
    }

}
