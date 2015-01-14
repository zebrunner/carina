/**
 * TestRail API binding for Java (API v2, available since TestRail 3.0)
 *
 * Learn more:
 *
 * http://docs.gurock.com/testrail-api2/start
 * http://docs.gurock.com/testrail-api2/accessing
 *
 * Copyright Gurock Software GmbH. See license.md for details.
 */
 
package com.qaprosoft.carina.core.foundation.report.testrail.core;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class APIClient
{
	private String m_user;
	private String m_password;
	private String m_url;

	public APIClient(String base_url)
	{
		if (!base_url.endsWith("/"))
		{
			base_url += "/";
		}
		
		this.m_url = base_url + "index.php?/api/v2/";
	}

	/**
	 * Set User
	 *
	 * Sets the user used for authenticating the API requests.
	 */

	public void setUser(String user)
	{
		this.m_user = user;
	}

	/**
	 * Set Password
	 *
	 * Sets the password used for authenticating the API requests.
	 */

	public void setPassword(String password)
	{
		this.m_password = password;
	}

	/**
	 * Send Get
	 *
	 * Issues a GET request (read) against the API and returns the result
	 * (as Object, see below).
	 *
	 * Arguments:
	 *
	 * uri                  The API method to call including parameters
	 *                      (e.g. get_case/1)
	 *
	 * Returns the parsed JSON response as standard object which can
	 * either be an instance of JSONObject or JSONArray (depending on the
	 * API method). In most cases, this returns a JSONObject instance which
	 * is basically the same as java.util.Map.
	 */
	public Object sendGet(String uri)
		throws IOException, APIException
	{
		return this.sendRequest("GET", uri, null);
	}

	/**
	 * Send POST
	 *
	 * Issues a POST request (write) against the API and returns the result
	 * (as Object, see below).
	 *
	 * Arguments:
	 *
	 * uri                  The API method to call including parameters
	 *                      (e.g. add_case/1)
	 * data                 The data to submit as part of the request (e.g.,
	 *                      a map)
	 *
	 * Returns the parsed JSON response as standard object which can
	 * either be an instance of JSONObject or JSONArray (depending on the
	 * API method). In most cases, this returns a JSONObject instance which
	 * is basically the same as java.util.Map.	 
	 */
	public Object sendPost(String uri, Object data)
		throws IOException, APIException
	{
		return this.sendRequest("POST", uri, data);
	}

	public Object sendRequest(Request request){
		if (request.getRequetsType().equals("POST"))
		{
			try {
				return sendRequest(request.getRequetsType(),request.getRequstURL(),request.getJsonObject());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (APIException e) {
				e.printStackTrace();
			}
		}else {
			try {
				return sendRequest(request.getRequetsType(),request.getRequstURL(),null);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (APIException e) {
				e.printStackTrace();
			}
	}

		throw  new RuntimeException("Unable to execute request");
	}

	private Object sendRequest(String method, String uri, Object data)
		throws IOException, APIException
	{
		URL url = new URL(this.m_url + uri);
		
		// Create the connection object and set the required HTTP method
		// (GET/POST) and headers (content type and basic auth).
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.addRequestProperty("Content-Type", "application/json");
		
		String auth = getAuthorization(this.m_user, this.m_password);
		conn.addRequestProperty("Authorization", "Basic " + auth);
		
		if (method == "POST")
		{
			// Add the POST arguments, if any. We just serialize the passed
			// data object (i.e. a dictionary) and then add it to the
			// request body.
			if (data != null)
			{				
				byte[] block = JSONValue.toJSONString(data).
					getBytes("UTF-8");

				conn.setDoOutput(true);				
				OutputStream ostream = conn.getOutputStream();			
				ostream.write(block);
				ostream.flush();
			}
		}
		
		// Execute the actual web request (if it wasn't already initiated
		// by getOutputStream above) and record any occurred errors (we use
		// the error stream in this case).
		int status = conn.getResponseCode();
		
		InputStream istream;
		if (status != 200)
		{
			istream = conn.getErrorStream();
			if (istream == null)
			{
				throw new APIException(
					"TestRail API return HTTP " + status + 
					" (No additional error message received)"
				);
			}
		}
		else 
		{
			istream = conn.getInputStream();
		}
		
		// Read the response body, if any, and deserialize it from JSON.
		String text = "";
		if (istream != null)
		{
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(
					istream,
					"UTF-8"
				)
			);
		
			String line;
			while ((line = reader.readLine()) != null)
			{
				text += line;
				text += System.getProperty("line.separator");
			}
			
			reader.close();
		}
		
		Object result;
		if (text != "")
		{
			result = JSONValue.parse(text);
		}
		else 
		{
			result = new JSONObject();
		}
		
		// Check for any occurred errors and add additional details to
		// the exception message, if any (e.g. the error message returned
		// by TestRail).
		if (status != 200)
		{
			String error = "No additional error message received";
			if (result != null && result instanceof JSONObject)
			{
				JSONObject obj = (JSONObject) result;
				if (obj.containsKey("error"))
				{
					error = '"' + (String) obj.get("error") + '"';
				}
			}
			
			throw new APIException(
				"TestRail API returned HTTP " + status +
				"(" + error + ")"
			);
		}
		
		return result;
	}
	
	private static String getAuthorization(String user, String password)
	{
		try 
		{
			return getBase64((user + ":" + password).getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			// Not thrown
		}
		
		return "";
	}
	
	private static String getBase64(byte[] buffer)
	{
		final char[] map = {
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
			'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
			'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
			'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
			'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', '+', '/'
		};
	
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buffer.length; i++)
		{
			byte b0 = buffer[i++], b1 = 0, b2 = 0;

			int bytes = 3;
			if (i < buffer.length)
			{
				b1 = buffer[i++];
				if (i < buffer.length)
				{
					b2 = buffer[i];
				}
				else 
				{
					bytes = 2;
				}
			}
			else
			{
				bytes = 1;
			}
			
			int total = (b0 << 16) | (b1 << 8) | b2;
			
			switch (bytes)
			{
				case 3:
					sb.append(map[(total >> 18) & 0x3f]);
					sb.append(map[(total >> 12) & 0x3f]);
					sb.append(map[(total >> 6) & 0x3f]);
					sb.append(map[total & 0x3f]);
					break;
					
				case 2:
					sb.append(map[(total >> 18) & 0x3f]);
					sb.append(map[(total >> 12) & 0x3f]);
					sb.append(map[(total >> 6) & 0x3f]);
					sb.append('=');
					break;
					
				case 1:
					sb.append(map[(total >> 18) & 0x3f]);
					sb.append(map[(total >> 12) & 0x3f]);
					sb.append('=');
					sb.append('=');
					break;
			}
		}
	
		return sb.toString();
	}
}
