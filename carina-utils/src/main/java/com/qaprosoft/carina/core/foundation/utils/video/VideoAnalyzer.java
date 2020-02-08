package com.qaprosoft.carina.core.foundation.utils.video;

import org.apache.log4j.Logger;

public class VideoAnalyzer {
    private static final Logger LOGGER = Logger.getLogger(VideoAnalyzer.class);

    private static ThreadLocal<Boolean> uploadVideo = new ThreadLocal<Boolean>();

    public static void disableVideoUpload() {
        uploadVideo.set(Boolean.FALSE);
        LOGGER.debug("Video upload has been disabled");
    }

    public static void enableVideoUpload() {
        uploadVideo.set(Boolean.TRUE);
        LOGGER.debug("Video upload has been enabled");
    }

    public static Boolean isVideoUploadEnabled() {
        Boolean isEnabled = uploadVideo.get();
        LOGGER.debug("Video upload is enabled : " + isEnabled);
        return isEnabled;    
    }
}