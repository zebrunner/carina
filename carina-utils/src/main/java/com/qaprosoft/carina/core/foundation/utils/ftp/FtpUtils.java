package com.qaprosoft.carina.core.foundation.utils.ftp;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class FtpUtils {
	private static final Logger LOGGER = Logger.getLogger(FtpUtils.class);
	private static final int DEFAULT_PORT = 21;

	public static void uploadFile(String ftpHost, String user, String password, String filePassToUpload,
			String fileName) {
		uploadFile(ftpHost, DEFAULT_PORT, user, password, filePassToUpload, fileName);
	}

	public static void uploadFile(String ftpHost, int port, String user, String password, String filePassToUpload,
			String fileName) {
		try (InputStream is = new FileInputStream(filePassToUpload)) {
			upload(ftpHost, port, user, password, is, fileName);
		} catch (FileNotFoundException e) {
			LOGGER.info("File is not found. Specify correct file pass");
		} catch (IOException e) {
			LOGGER.info("Exception while opening file for upload.");
		}
	}

	public static void uploadData(String ftpHost, String user, String password, String data,
			String destinationFileName) {
		uploadData(ftpHost, DEFAULT_PORT, user, password, data, destinationFileName);
	}

	public static void uploadData(String ftpHost, int port, String user, String password, String data,
			String destinationFileName) {
		byte[] decode = Base64.getDecoder().decode(data);
		LOGGER.debug("Data size to upload: " + data.length());
		LOGGER.debug("Encoded data size to upload: " + decode.length);
		try (InputStream is = new ByteArrayInputStream(decode)) {
			upload(ftpHost, port, user, password, is, destinationFileName);
		} catch (IOException e) {
			LOGGER.info("Exception while opening file for upload.");
		}
	}

	private static void upload(String ftpHost, int port, String user, String password, InputStream is,
			String fileName) {
	    LOGGER.debug("FTP host to upload data : " + ftpHost);
	    LOGGER.debug("FTP port to upload data : " + port);
        LOGGER.debug("Destination file name : " + fileName);
        long start = System.currentTimeMillis();
		FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(ftpHost, port);
			LOGGER.debug("Connected to server : " + ftpHost);
			reply = ftp.getReplyCode();
			LOGGER.debug("Reply code is : " + reply);
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				LOGGER.info("FTP server refused connection. Reply code is : " + reply);
				throw new Exception("FTP server refused connection.");
			}
			if (!ftp.login(user, password)) {
			    throw new Exception("Login to ftp failed. Check user credentials.");
			};
			LOGGER.debug("User has been successfully logged in.");
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			try {
			    ftp.enterLocalPassiveMode();
			    LOGGER.debug("Passive host : " + ftp.getPassiveHost() + " Passive port : " + ftp.getPassivePort());
			    LOGGER.debug("Default port : " + ftp.getDefaultPort());
			    LOGGER.debug("Local port : " + ftp.getLocalPort());
			    LOGGER.debug("Remote port : " + ftp.getRemotePort());
				if (ftp.storeFile(fileName, is)) {
				    long finish = System.currentTimeMillis();
                    LOGGER.info("Video uploading completed in " + (finish - start) + " msecs.");
				} else {
				    LOGGER.info("Some issues occures during storing file to FTP. storeFile method returns false.");			    
				}
			} catch (IOException e) {
				LOGGER.info("Exception while storing file to FTP", e);
			}
		} catch (Exception e) {
			LOGGER.info("Exception while uploading while to FTP", e);
		} finally {
			ftpDisconnect(ftp);
		}
	}

	public static void ftpDisconnect(FTPClient ftp) {
		if (ftp.isConnected()) {
			try {
				ftp.logout();
				ftp.disconnect();
			} catch (Exception ioe) {
				LOGGER.error("Exception while disconnecting ftp", ioe);
			}
		}
		LOGGER.debug("FTP has been successfully disconnected.");
	}

}
