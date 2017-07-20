package com.qaprosoft.carina.core.foundation.crypto;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * Tests for {@link CryptoTool}
 */
public class CryptoToolTest
{
	@Test
	public void testInitialization() throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, URISyntaxException
	{
		CryptoTool cryptoTool = new CryptoTool();
		Assert.assertNotNull(cryptoTool.getAlgorithm());
		Assert.assertNotNull(cryptoTool.getCipher());
		Assert.assertEquals(R.CONFIG.get("crypto_algorithm"), cryptoTool.getAlgorithm());
	}
	
	@Test
	public void testEncrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, URISyntaxException 
	{
		CryptoTool cryptoTool = new CryptoTool();
		String input = "EncryptMe";
		String encrypted = cryptoTool.encrypt(input);
		Assert.assertNotNull(encrypted);
		Assert.assertFalse(encrypted.equals(input));
	}
	
	@Test
	public void testDecrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, URISyntaxException 
	{
		CryptoTool cryptoTool = new CryptoTool();
		String input = "EncryptMe";
		String encrypted = cryptoTool.encrypt(input);
		String decrypted = cryptoTool.decrypt(encrypted);
		Assert.assertNotNull(decrypted);
		Assert.assertEquals(input, decrypted);
	}
}
