package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepository;

	Random random = new Random(1000); // starts from

	// Open Email-Id Form handler
	@RequestMapping("/forgot")
	public String openEmailForm() {

		return "forgot_email_form";

	}

	// OTP verification Handler
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email, HttpSession session) {

		System.out.println("Email: " + email);

		// Generating OTP of 4 Digits

		int otp = random.nextInt(99999); // upper bound(exclusive)

		System.out.println("OTP:" + otp);

		// Write code for send OTP to email
		String subject = "OTP from SCM";
		String message = "" + "<div style='border:1px solid #e2e2e2; padding:20px;'>" + "OTP is :" + "<h1>" + "<b>"
				+ otp + "</n>" + "</h1>" + "</div>";

		String to = email;

		boolean flag = this.emailService.sendEmail(subject, message, to);

		if (flag) {
			// store OTP in the session
			session.setAttribute("myotp", otp); // for matching purpose
			session.setAttribute("email", email); // also save the email
			return "verify_otp";
		} else {

			session.setAttribute("message", "Check Your Email!!");
			return "forgot_email_form";
		}

	}

	// verify OTP
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {

		int myOtp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");

		if (myOtp == otp) {
			// password change form
			// (fetch the user by email)
			User user = this.userRepository.getUserByUserName(email);

			if (user == null) {
				// send error message
				session.setAttribute("message", "No User Found By this Email...");
				return "forgot_email_form";

			} else {

			}

			return "change_password";
		} else {
			session.setAttribute("message", "You Have Entered incorrect OTP");
			return "verify_otp";
		}

	}

	// Change Password Handler

	@PostMapping ("/update-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		String email = (String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);

		
		return "redirect:/signin?change=password changed successfully!!";
		
	}         

}

