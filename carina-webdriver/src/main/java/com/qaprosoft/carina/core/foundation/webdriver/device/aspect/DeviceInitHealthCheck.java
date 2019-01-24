package com.qaprosoft.carina.core.foundation.webdriver.device.aspect;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

@Aspect
public class DeviceInitHealthCheck {
    
    private static final Logger LOGGER = Logger.getLogger(DeviceInitHealthCheck.class);
    
    @Pointcut("execution(* com.qaprosoft.carina.core.foundation.webdriver.device.Device.connectRemote(..))")
    public void connectDevice() {}
    
    
    @After("connectDevice()")
    public void performHealthCheck(JoinPoint jp) {
        LOGGER.info("Performed after execution of 'connectDevice()' method.");
        Device device = (Device) jp.getThis();
        LOGGER.info("Device: ".concat(device.toString()));
    }

}
