package com.qaprosoft.amazon;


import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AmazonS3ClientTest {

	@Test()
	public void testS3ManagerInit() {
		Assert.assertNotNull(AmazonS3Manager.getInstance(), "Singleton for AmazonS3Manager is null!");
	}

	@Test()
	public void testPutKeyNull() {
		try {
			AmazonS3Manager.getInstance().put("", null, null);
			Assert.fail("Key verification doesn't work!");
		} catch (RuntimeException e) {
			// Expected failure
		}
	}

	@Test()
	public void testPutKeyEmpty() {
		try {
			AmazonS3Manager.getInstance().put("", "", null);
			Assert.fail("Key verification doesn't work!");
		} catch (RuntimeException e) {
			// Expected failure
		}
	}

	@Test()
	public void testPutFilePathNull() {
		try {
			AmazonS3Manager.getInstance().put("", "test", null);
			Assert.fail("FilePath verification doesn't work!");
		} catch (RuntimeException e) {
			// Expected failure
		}
	}

	@Test()
	public void testPutFilePathEmpty() {
		try {
			AmazonS3Manager.getInstance().put("", "test", "");
			Assert.fail("FilePath verification doesn't work!");
		} catch (RuntimeException e) {
			// Expected failure
		}
	}

	@Test()
	public void testPutFilePathNotExist() {
		try {
			AmazonS3Manager.getInstance().put("", "test", "test");
			Assert.fail("File existence verification doesn't work!");
		} catch (RuntimeException e) {
			// Expected failure
		}
	}


	@Test()
	public void testGetKeyNull() {
		try {
			AmazonS3Manager.getInstance().get("test", null);
			Assert.fail("Key verification doesn't work!");
		} catch (RuntimeException e) {
			// Expected failure
		}
	}

	@Test()
	public void testDeleteKeyNull() {
		try {
			AmazonS3Manager.getInstance().delete("", null);
			Assert.fail("Key verification doesn't work!");
		} catch (RuntimeException e) {
			// Expected failure
		}
	}

	@Test()
	public void testDeleteKeyEmpty() {
		try {
			AmazonS3Manager.getInstance().delete("", "");
			Assert.fail("Key verification doesn't work!");
		} catch (RuntimeException e) {
			// Expected failure
		}
	}
	
	@Test()
	public void testgeneratePreSignURL() {
			URL url = AmazonS3Manager.getInstance().generatePreSignUrl("carina.qaprosoft.com", "test.txt", 1000*60*10);
			System.out.println(url.toString());
//			Example: 
//			https://carina.qaprosoft.com.s3.amazonaws.com/test.txt?AWSAccessKeyId=AKIAIF43YTFM7RWG7EVQ&Expires=1506266253&Signature=um7fDD2cZmTKLd%2BLZYs0Yq2%2Fc50%3D
			//TODO: add verification that file is accessible without creds
	}

}