package com.qaprosoft.carina.core.foundation.utils.video;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by Quang Le (quangltp) on 10/5/20
 */

public class VideoAnalyzerTest {
    @Test
    public void testIsVideoUploadNotEnabled() {
        VideoAnalyzer.disableVideoUpload();
        Assert.assertFalse(VideoAnalyzer.isVideoUploadEnabled(), "Is Video Upload Enabled");
    }

    @Test
    public void testIsVideoUploadEnabled() {
        VideoAnalyzer.enableVideoUpload();
        Assert.assertTrue(VideoAnalyzer.isVideoUploadEnabled(), "Is Video Upload Enabled");
    }

    @Test
    public void testDefaultIsVideoUploadEnabled() {
        Assert.assertFalse(VideoAnalyzer.isVideoUploadEnabled(), "Is Video Upload Enabled");
    }
}