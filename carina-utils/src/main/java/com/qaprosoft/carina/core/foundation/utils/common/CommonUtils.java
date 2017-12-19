package com.qaprosoft.carina.core.foundation.utils.common;

import org.apache.log4j.Logger;

public class CommonUtils {

	private static final Logger LOGGER = Logger.getLogger(CommonUtils.class);
	
	 /**
     * pause
     * 
     * @param timeout Number
     */	
	public static void pause (Number timeout){
		LOGGER.info(String.format("Will wait for %s seconds", timeout));
        try {
        	Float timeoutFloat = timeout.floatValue()*1000;
        	long timeoutLong = timeoutFloat.longValue();
        	Thread.sleep(timeoutLong);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("Pause is overed. Keep going..");
	}	
}
