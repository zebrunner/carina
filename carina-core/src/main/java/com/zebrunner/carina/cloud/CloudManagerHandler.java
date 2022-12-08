package com.zebrunner.carina.cloud;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.zebrunner.carina.utils.cloud.CloudManager;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 public class CloudManagerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    // todo check/refactor patterns
    private static final Pattern AMAZON_S3_ENDPOINT_PATTERN = Pattern.compile("^(.+\\.)?s3[.-]([a-z0-9-]+)\\.");
    private static final Pattern AZURE_ENDPOINT_PATTERN = Pattern.compile(
            "\\/\\/([a-z0-9]{3,24})\\.blob.core.windows.net\\/(?:(\\$root|(?:[a-z0-9](?!.*--)[a-z0-9-]{1,61}[a-z0-9]))\\/)?(.{1,1024})");
    // appcenter://appName/platformName/buildType/version
    private static final Pattern APPCENTER_ENDPOINT_PATTERN = Pattern.compile(
            "appcenter:\\/\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)");

    private static final String AMAZON_MANAGER_CLASS_NAME = "com.qaprosoft.amazon.AmazonS3Manager";
    private static final String AZURE_MANAGER_CLASS_NAME = "com.qaprosoft.azure.AzureManager";
    private static final String APPCENTER_MANAGER_CLASS_NAME = "com.qaprosoft.appcenter.AppCenterManager";

    private CloudManagerHandler() {
    }

    public static CloudManager getProxyHandler() {
        InvocationHandler handler = new CloudInvocationHandler();
        return (CloudManager) Proxy.newProxyInstance(CloudManager.class.getClassLoader(),
                new Class<?>[] { CloudManager.class },
                handler);
    }

    private static class CloudInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String url = null;
            for (Object arg : args) {
                if (arg instanceof String) {
                    url = (String) arg;
                }
            }
            if (url == null) {
                throw new RuntimeException("Incorrect implementation of cloud handler! There should be only one url parameter of "
                        + "String type to understand what type of realisation should be choosed!");
            }

            Class<?> managerClass = null;

            LOGGER.debug("Analyzing if url is belong to the Amazon S3...");
            if (AMAZON_S3_ENDPOINT_PATTERN.matcher(url).find()) {
                LOGGER.debug("Detected Amazon S3 link.");
                try {
                    managerClass = ClassUtils.getClass(AMAZON_MANAGER_CLASS_NAME);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Amazon link was detected, but AmazonS3Manager class was not detected."
                            + "Add carina-aws-s3 dependency to your project");
                }
            }

            LOGGER.debug("Analyzing if url app is located on Azure...");
            if (AZURE_ENDPOINT_PATTERN.matcher(url).find()) {
                LOGGER.debug("Detected Azure link.");
                try {
                    managerClass = ClassUtils.getClass(AZURE_MANAGER_CLASS_NAME);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Azure link was detected, but AzureManager class was not detected."
                            + "Add carina-azure dependency to your project");
                }
            }

            LOGGER.debug("Analyzing if url is located on AppCenter...");
            if (APPCENTER_ENDPOINT_PATTERN.matcher(url).find()) {
                LOGGER.debug("Detected AppCenter link.");
                try {
                    managerClass = ClassUtils.getClass(APPCENTER_MANAGER_CLASS_NAME);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("AppCenter link was detected, but AppCenterManager class was not detected."
                            + "Add carina-appcenter dependency to your project");
                }
            }

            if (managerClass == null) {
                throw new RuntimeException("Cannot choose cloud implementation! Please, check your link: " + url);
            }

            Object instance = MethodUtils.invokeStaticMethod(managerClass, "getInstance");
            Class<?>[] argClasses = Arrays.stream(args)
                    .map(Object::getClass)
                    .toArray(Class[]::new);
            Method realMethod = MethodUtils.getMatchingMethod(instance.getClass(), method.getName(), argClasses);
            if (realMethod == null) {
                throw new UnsupportedOperationException(
                        String.format("Method '%s' does not supported by '%s'", method.getName(), instance.getClass()));
            }
            return realMethod.invoke(instance, args);
        }
    }

}
