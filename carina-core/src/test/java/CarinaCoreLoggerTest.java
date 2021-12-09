import com.qaprosoft.carina.core.foundation.report.email.EmailReportGenerator;
import com.qaprosoft.carina.core.foundation.utils.messager.IMessager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandles;

public class CarinaCoreLoggerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void testReportingAppender(){
        LOGGER.info("logger test");
    }
}
