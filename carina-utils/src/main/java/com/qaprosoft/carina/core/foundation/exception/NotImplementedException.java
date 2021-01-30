package com.qaprosoft.carina.core.foundation.exception;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

/*
 * Exception can be thrown when some method is not implemented for platform.
 * 
 * @author Dzmitry Pinchuk
 */
public class NotImplementedException extends RuntimeException {
    private static final long serialVersionUID = 1147240484643530919L;

    public NotImplementedException() {
        super(generateMessage());
    }
    
    private static String generateMessage() {
        String platform = Configuration.getPlatform();
        List<StackTraceElement> elements = Arrays.asList(Thread.currentThread().getStackTrace());
        String currentMethodName = elements.get(1).getClassName();
        currentMethodName = StringUtils.substringAfterLast(currentMethodName, ".");
        int index = 0;
        for (StackTraceElement element : elements) {
            if (element.getClassName().contains(currentMethodName)) {
                index = elements.indexOf(element) + 2;
                break;
            }
        }
        if (index == 0) {
            throw new UnsupportedOperationException("Unable to identificate correct position of method in stackTrace!");
        }
        String methodName = elements.get(index).getMethodName();
        return String.format("Method [%s] isn't implemented for %s!", methodName, platform);
    }

}
