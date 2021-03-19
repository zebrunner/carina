#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*******************************************************************************
 * Copyright 2013-2021 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ${package}.carina.demo;

import com.qaprosoft.carina.core.foundation.AbstractTest;
import com.qaprosoft.carina.core.foundation.utils.R;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This sample shows how use Crypto in tests.
 *
 * @author qpsdemo
 */
public class CryptoSampleTest extends AbstractTest {

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
