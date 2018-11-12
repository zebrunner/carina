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
			LOGGER.error("File is not found. Specify correct file pass");
		} catch (IOException e) {
			LOGGER.error("Exception while opening file for upload.");
		}
	}

	public static void uploadData(String ftpHost, String user, String password, String data,
			String destinationFileName) {
		uploadData(ftpHost, DEFAULT_PORT, user, password, data, destinationFileName);
	}

	public static void uploadData(String ftpHost, int port, String user, String password, String data,
			String destinationFileName) {
		byte[] decode = Base64.getDecoder().decode(data);
		try (InputStream is = new ByteArrayInputStream(decode)) {
			upload(ftpHost, port, user, password, is, destinationFileName);
		} catch (IOException e) {
			LOGGER.error("Exception while opening file for upload.");
		}
	}

	private static void upload(String ftpHost, int port, String user, String password, InputStream is,
			String fileName) {
		FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(ftpHost, port);
			LOGGER.debug("Connected to server : " + ftpHost);
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				LOGGER.error("FTP server refused connection.");
				throw new Exception("FTP server refused connection.");
			}
			ftp.login(user, password);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			try {
				ftp.storeFile(fileName, is);
			} catch (IOException e) {
				LOGGER.error("Exception while storing file to FTP");
			}
		} catch (Exception e) {
			LOGGER.error("Exception while uploading while to FTP", e);
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
				LOGGER.error("Exception while disconnecting ftp");
			}
		}
	}

}
