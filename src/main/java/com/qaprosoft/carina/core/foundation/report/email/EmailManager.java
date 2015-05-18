/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.report.email;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * EmailManager is used for sending emails.
 * 
 * @author Alex Khursevich
 */
public class EmailManager
{
	public static final String HTTP_PROXY_HOSTNAME = "http.proxyHost";
	public static final String HTTP_PROXY_PORT = "http.proxyPort";
	public static final Integer DEFAULT_HTTP_PORT = 80;

	protected static final Logger LOGGER = Logger.getLogger(EmailManager.class);

	public static void send(String subject, String emailContent, String adresses, String senderEmail, String senderPswd)
	{
		//verify that provided list of addresses is valid and don't send email if
		//<5 char long and does NOT contain "@"
		if (!adresses.contains("@") || adresses.length() <5) {
			LOGGER.warn("Invalid email address(es) are specified: '" + adresses + "'. Email report can't be sent!");
			return;
		}
		
		Properties props = new Properties();
		props.put("mail.smtp.host", R.EMAIL.get("mail.smtp.host"));
		props.put("mail.smtp.auth", R.EMAIL.get("mail.smtp.auth"));
		props.put("mail.smtp.port", R.EMAIL.get("mail.smtp.port"));
		
		Session session = null;
		
		if (R.EMAIL.get("mail.smtp.auth").equalsIgnoreCase("true")){

			if (!R.EMAIL.get("mail.smtp.socketFactory.port").isEmpty()){
				props.put("mail.smtp.socketFactory.port", R.EMAIL.get("mail.smtp.socketFactory.port"));
			}
			if (!R.EMAIL.get("mail.smtp.socketFactory.class").isEmpty()){
				props.put("mail.smtp.socketFactory.class", R.EMAIL.get("mail.smtp.socketFactory.class"));
			}
			if (!R.EMAIL.get("mail.smtp.starttls.enable").isEmpty()){
				props.put("mail.smtp.starttls.enable", R.EMAIL.get("mail.smtp.starttls.enable"));
			}				
			
			//session = Session.getDefaultInstance(props, new javax.mail.Authenticator()
			session = Session.getInstance(props, new javax.mail.Authenticator()
			{
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(Configuration.get(Parameter.SENDER_EMAIL), Configuration.get(Parameter.SENDER_PASSWORD));
				}
			});
		}
		else {
			session = Session.getDefaultInstance(props);
		}
		
		try
		{
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(senderEmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(adresses));
			message.setSubject(subject);
			message.setContent(emailContent, "text/html");
			Transport.send(message);
			LOGGER.info("Reports were successfully sent!");
		}
		catch (MessagingException e)
		{
			LOGGER.error("Email with reports was NOT send!", e);
			LOGGER.debug(e.getMessage(), e);
		}
	}


}
