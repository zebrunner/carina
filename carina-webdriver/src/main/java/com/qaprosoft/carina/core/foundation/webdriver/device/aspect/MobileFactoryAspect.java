package com.qaprosoft.carina.core.foundation.webdriver.device.aspect;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class MobileFactoryAspect {
    
    private static final Logger LOGGER = Logger.getLogger(MobileFactoryAspect.class);
    
    @Pointcut("execution(* com.qaprosoft.carina.core.foundation.webdriver.core.factory.DriverFactory.create(..))")
    public void createDriver() {}
    
    
    @Before("createDriver()")
    public void performHealthCheck(JoinPoint jp) {
        LOGGER.info("Performed before driver creation.");
    }

}
