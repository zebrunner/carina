#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.amazonaws.regions.Regions;
import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.amazon.AmazonS3Manager;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.report.ReportContext;

public class AmazonS3Test implements IAbstractTest {

    private static final String FILE_NAME = "carinademoexample.apk";
    private static final String BUCKET_NAME = "qaprosoft";

    @Test
    public void amazonS3DownloadTest() {
        R.CONFIG.put(Configuration.Parameter.S3_REGION.getKey(), Regions.US_WEST_2.getName(), true);
        AmazonS3Manager amazonS3Manager = AmazonS3Manager.getInstance();
        File artifact = new File(ReportContext.getArtifactsFolder() + File.separator + FILE_NAME);
        amazonS3Manager.download(BUCKET_NAME, FILE_NAME, artifact);
        Assert.assertTrue(artifact.exists(), "Artifact should exists");
    }

}
