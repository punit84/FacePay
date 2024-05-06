package com.punit.mail;


import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


public class EmailSupport {

	public static void main(String[] args) {
		SpringApplication.run(EmailSupport.class, args);
	}
}
//	@PostMapping("/send-email")
//	public String sendEmail(@RequestBody EmailRequest emailRequest) {
//		JavaMailSender mailSender = getJavaMailSender();
//		SimpleMailMessage message = new SimpleMailMessage();
//		message.setTo(emailRequest.getTo());
//		message.setSubject(emailRequest.getSubject());
//		message.setText(emailRequest.getMessage());
//		mailSender.send(message);
//		return "Email sent successfully!";
//	}
//
//	private JavaMailSender getJavaMailSender() {
//		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//		mailSender.setHost("smtp.example.com");
//		mailSender.setPort(587);
//		mailSender.setUsername("your-email@example.com"); // replace with your email
//		mailSender.setPassword("your-password"); // replace with your password
//		return mailSender;
//	}
//
//	static class EmailRequest {
//		private String to;
//		private String subject;
//		private String message;
//
//		// getters and setters
//
//		public String getTo() {
//			return to;
//		}
//
//		public void setTo(String to) {
//			this.to = to;
//		}
//
//		public String getSubject() {
//			return subject;
//		}
//
//		public void setSubject(String subject) {
//			this.subject = subject;
//		}
//
//		public String getMessage() {
//			return message;
//		}
//
//		public void setMessage(String message) {
//			this.message = message;
//		}
//	}
//}
//
