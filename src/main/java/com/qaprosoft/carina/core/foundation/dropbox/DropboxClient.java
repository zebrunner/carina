package com.qaprosoft.carina.core.foundation.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWriteMode;

public class DropboxClient {
	DbxClient client;

	public DropboxClient(String accessToken) {
		DbxRequestConfig config = new DbxRequestConfig("JavaTutorial/1.0", Locale.getDefault().toString());

		client = new DbxClient(config, accessToken);
		//System.out.println("Linked account: " + client.getAccountInfo().displayName);
	}
	public String uploadFile(String sourcePath, String targetPath){
		String url = "";
		try {
			upload(sourcePath, targetPath);
			url = getSharedLink(targetPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}	

	private void upload(String sourcePath, String targetPath) throws Exception {
		File inputFile = new File(sourcePath);
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(inputFile);
			DbxEntry.File uploadedFile = client.uploadFile(targetPath, DbxWriteMode.add(), inputFile.length(), inputStream);
			System.out.println("Uploaded: " + uploadedFile.toString());
		} finally {
			inputStream.close();
		}
	}
	
	private String getSharedLink(String targetPath) {
		try {
			return client.createShareableUrl(targetPath);
		} catch (DbxException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to get shared link for: " + targetPath); 
		}
	}
}
