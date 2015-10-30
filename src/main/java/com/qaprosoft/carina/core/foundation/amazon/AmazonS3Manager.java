package com.qaprosoft.carina.core.foundation.amazon;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class AmazonS3Manager {
	protected static final Logger LOGGER = Logger
			.getLogger(AmazonS3Manager.class);

	private static AmazonS3 s3client;
	private static String bucketName = Configuration
			.get(Parameter.S3_BUCKET_NAME);

	public AmazonS3Manager() {
		System.setProperty("aws.accessKeyId",
				Configuration.get(Parameter.ACCESS_KEY_ID));
		System.setProperty("aws.secretKey",
				Configuration.get(Parameter.SECRET_KEY));
		s3client = new AmazonS3Client(new SystemPropertiesCredentialsProvider());
	}

	public void put(String key, String filePath) {

		try {
			LOGGER.info("Uploading a new object to S3 from a file");
			File file = new File(filePath);
			s3client.putObject(new PutObjectRequest(bucketName, key, file));

		} catch (AmazonServiceException ase) {
			//TODO combine errors into the single logger.error
			LOGGER.error("Caught an AmazonServiceException, which "
					+ "means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			LOGGER.error("Error Message:    " + ase.getMessage());
			LOGGER.error("HTTP Status Code: " + ase.getStatusCode());
			LOGGER.error("AWS Error Code:   " + ase.getErrorCode());
			LOGGER.error("Error Type:       " + ase.getErrorType());
			LOGGER.error("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			LOGGER.error("Caught an AmazonClientException, which "
					+ "means the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			LOGGER.error("Error Message: " + ace.getMessage());
		}
	}

	public S3Object get(String key) {
		//S3Object s3object = null;
		try {
			LOGGER.info("Downloading an object...");
			//TODO investigate possibility to add percentage of completed downloading 
			S3Object s3object = s3client
					.getObject(new GetObjectRequest(bucketName, key));
			LOGGER.info("Content-Type: "
					+ s3object.getObjectMetadata().getContentType());
			return s3object;
			/*GetObjectRequest rangeObjectRequest = new GetObjectRequest(
					bucketName, key);
			rangeObjectRequest.setRange(0, 10);
			S3Object objectPortion = s3client.getObject(rangeObjectRequest);*/
		} catch (AmazonServiceException ase) {
			LOGGER.error("Caught an AmazonServiceException, which"
					+ " means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			LOGGER.error("Error Message:    " + ase.getMessage());
			LOGGER.error("HTTP Status Code: " + ase.getStatusCode());
			LOGGER.error("AWS Error Code:   " + ase.getErrorCode());
			LOGGER.error("Error Type:       " + ase.getErrorType());
			LOGGER.error("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			LOGGER.error("Caught an AmazonClientException, which means"
					+ " the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			LOGGER.error("Error Message: " + ace.getMessage());
		}
		//TODO investigate pros and cons returning null
		throw new RuntimeException("Unable to download '" + key + "' from Amazon S3 bucket '" + bucketName  +"'");
	}

	public void read(S3Object s3object) {
		displayTextInputStream(s3object.getObjectContent());
	}

	public void delete(String key) {
		try {
			s3client.deleteObject(new DeleteObjectRequest(bucketName, key));
		} catch (AmazonServiceException ase) {
			LOGGER.error("Caught an AmazonServiceException.");
			LOGGER.error("Error Message:    " + ase.getMessage());
			LOGGER.error("HTTP Status Code: " + ase.getStatusCode());
			LOGGER.error("AWS Error Code:   " + ase.getErrorCode());
			LOGGER.error("Error Type:       " + ase.getErrorType());
			LOGGER.error("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			LOGGER.error("Caught an AmazonClientException.");
			LOGGER.error("Error Message: " + ace.getMessage());
		}
	}

	private void displayTextInputStream(InputStream input) {
		// Read one text line at a time and display.
		LOGGER.info("File content is: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		while (true) {
			String line = null;
			try {
				line = reader.readLine();
			} catch (IOException e) {
				LOGGER.error("Failed to read file", e);
			}
			if (line == null)
				break;

			System.out.println("    " + line);
		}
	}
}
