package com.smart.service;

import org.springframework.stereotype.Service;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {
	public boolean sendEmail(String subject, String message, String to) {

		boolean flag = false;

		String from = ""; // Email

		// Variable for gmail
		String host = "smtp.gmail.com";

		// get the system properties
		Properties properties = System.getProperties();
		System.out.println("Properties" + properties);

		// setting important information to properties object
		properties.put("mail.smtp.host", host);// key-value
		properties.put("mail.smtp.port", "465");// key-value
		properties.put("mail.smtp.ssl.enable", "true");// key-value
		properties.put("mail.smtp.auth", "true");// key-value

		// Step:1 to get the session object
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication("testingzee21@gmail.com", "21testingzee21*");// Email and Password
			}

		});
		session.setDebug(true);

		// Step:2 Compose the message[text ,multi-media]
		MimeMessage mimeMessage = new MimeMessage(session);

		try {
			// from email
			mimeMessage.setFrom(from);

			// adding recipient to message
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// adding subject to message
			mimeMessage.setSubject(subject);

			// adding text to message
			// mimeMessage.setText(message);
			mimeMessage.setContent(message, "text/html"); // specifies that in which form message has to be send

			// Step:3 send the message using transport class
			Transport.send(mimeMessage);

			System.out.println("Sent Successfully!!");
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
}