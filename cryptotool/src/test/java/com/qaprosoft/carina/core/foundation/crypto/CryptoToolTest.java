package com.qaprosoft.carina.core.foundation.crypto;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;


/**
 * Tests for {@link CryptoTool}
 */
public class CryptoToolTest
{
	private SecretKey key;
	CryptoTool cryptoTool;
	private final static String cryptoFileName = "crypto.key";
	
	@Test(priority = 1)
	public void testGenerateKey() throws NoSuchAlgorithmException
	{
		key = SecretKeyManager.generateKey(SpecialKeywords.CRYPTO_KEY_TYPE, SpecialKeywords.CRYPTO_KEY_SIZE);
		Assert.assertEquals(SpecialKeywords.CRYPTO_KEY_TYPE, key.getAlgorithm());
	}
	
	@Test(priority = 2)
	public void testSaveKey() throws NoSuchAlgorithmException, IOException
	{
		File keyFile = new File(cryptoFileName);
		SecretKeyManager.saveKey(key, keyFile);
		
		Assert.assertTrue(keyFile.exists());
	}

	@Test(priority = 3)
	public void testLoadKey() throws NoSuchAlgorithmException, IOException
	{
		File keyFile = new File(cryptoFileName);
		key = SecretKeyManager.loadKey(keyFile, SpecialKeywords.CRYPTO_KEY_TYPE);
		Assert.assertEquals(SpecialKeywords.CRYPTO_KEY_TYPE, key.getAlgorithm());
	}
	
	@Test(priority = 4)
	public void testInitialization()
	{
		cryptoTool = new CryptoTool(cryptoFileName);
		Assert.assertNotNull(cryptoTool.getAlgorithm());
		Assert.assertNotNull(cryptoTool.getCipher());
		Assert.assertEquals(SpecialKeywords.CRYPTO_ALGORITHM, cryptoTool.getAlgorithm());
	}
	
	@Test(priority = 5)
	public void testEncrypt()
	{
		String input = "EncryptMe";
		String encrypted = cryptoTool.encrypt(input);
		Assert.assertNotNull(encrypted);
		Assert.assertFalse(encrypted.equals(input));
	}
	
	@Test(priority = 6)
	public void testDecrypt()
	{
		String input = "EncryptMe";
		String encrypted = cryptoTool.encrypt(input);
		String decrypted = cryptoTool.decrypt(encrypted);
		Assert.assertNotNull(decrypted);
		Assert.assertEquals(input, decrypted);
	}
	@AfterSuite
	public void cleanup() {
		File keyFile = new File(cryptoFileName);
		Assert.assertTrue(keyFile.exists());
		keyFile.delete();		
	}
}
