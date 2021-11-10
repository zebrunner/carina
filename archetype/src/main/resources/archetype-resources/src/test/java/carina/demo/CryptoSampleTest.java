#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import com.qaprosoft.carina.core.foundation.IAbstractTest;
import com.qaprosoft.carina.core.foundation.utils.R;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This sample shows how use Crypto in tests.
 *
 * @author qpsdemo
 */
public class CryptoSampleTest implements IAbstractTest {

    @Test
    public void testPlaceholdersWithEncryptionTestData() {
        Assert.assertEquals(R.TESTDATA.get("test_credentials"), "test@gmail.com/EncryptMe");
    }


    @Test
    public void testEncryption() {
        Assert.assertEquals(R.CONFIG.get("password"), "EncryptMe");
    }

    @Test
    public void testPlaceholdersWithEncryption() {
        Assert.assertEquals(R.CONFIG.get("credentials"), "test@gmail.com/EncryptMe");
    }

}
