package com.qaprosoft.carina.core.foundation.crypto;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * Tests for {@link CryptoTool}
 */
public class CryptoToolTest
{
	@Test
	public void testInitialization()
	{
		CryptoTool cryptoTool = new CryptoTool();
		Assert.assertNotNull(cryptoTool.getAlgorithm());
		Assert.assertNotNull(cryptoTool.getCipher());
		Assert.assertEquals(R.CONFIG.get("crypto_algorithm"), cryptoTool.getAlgorithm());
	}
	
	@Test
	public void testEncrypt()
	{
		CryptoTool cryptoTool = new CryptoTool();
		String input = "EncryptMe";
		String encrypted = cryptoTool.encrypt(input);
		Assert.assertNotNull(encrypted);
		Assert.assertFalse(encrypted.equals(input));
	}
	
	@Test
	public void testDecrypt()
	{
		CryptoTool cryptoTool = new CryptoTool();
		String input = "EncryptMe";
		String encrypted = cryptoTool.encrypt(input);
		String decrypted = cryptoTool.decrypt(encrypted);
		Assert.assertNotNull(decrypted);
		Assert.assertEquals(input, decrypted);
	}
}
