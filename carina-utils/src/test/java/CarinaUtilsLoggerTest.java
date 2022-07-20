import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import com.qaprosoft.carina.core.foundation.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class CarinaUtilsLoggerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void testReportingAppender() throws IOException {

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Enumeration<URL> findedResources =  classLoader.getResources("config.properties");
        while(findedResources.hasMoreElements()) {
            URL resourceURL = findedResources.nextElement();
            InputStream stream = resourceURL.openStream();
            Properties configProperties = new Properties();
            configProperties.load(stream);
            LOGGER.info(configProperties.toString());
        }

        LOGGER.info("logger test");
    }
}
