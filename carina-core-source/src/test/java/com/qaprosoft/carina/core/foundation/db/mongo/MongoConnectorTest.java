package com.qaprosoft.carina.core.foundation.db.mongo;

import java.net.UnknownHostException;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

/**
 * Tests for {@link MongoConnector}
 */
public class MongoConnectorTest
{
	@Test(expectedExceptions={RuntimeException.class})
	public void testConfigValidation() throws NumberFormatException, UnknownHostException
	{
		MongoConnector.createClient();
	}
	
	@Test(enabled=false)
	public void testConnect() throws NumberFormatException, UnknownHostException
	{
		MongoClient mc = MongoConnector.createClient();
		DB db = mc.getDB("lcdocs");
		DBCollection collection = db.getCollection("statements.files");
		DBCursor cursor = collection.find(new BasicDBObject("filename", Pattern.compile("/.*278174.*/")));
		while (cursor.hasNext())
		{
			collection.remove(cursor.next());
		}
	}
}
