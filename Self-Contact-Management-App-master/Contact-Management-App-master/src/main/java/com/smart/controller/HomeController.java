package com.smart.controller;


import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
//HOME HANDLER
	public String home(Model model) {
		model.addAttribute("title", "Home-Contact Manager");
		return "home";
	}

	@RequestMapping("/about")
//ABOUT HANDLER
	public String about(Model model) {
		model.addAttribute("title", "About-Contact Manager");
		return "about";
	}

	@RequestMapping("/signup")
//LOGIN HANDLER
	public String signUp(Model model) {
		model.addAttribute("title", "SignUp-Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	// HANDLER FOR USER REGISTRATION
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {

		try {
			if (!agreement) {
				System.out.println("You aren't agree with terms and coditions...");
				throw new Exception("You aren't agree with terms and coditions...");

			}
			if (bindingResult.hasErrors()) {
				System.out.println("ERROR:" + bindingResult.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("Role_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("Agreement :" + agreement);
			System.out.println("USER:" + user);

			User result = this.userRepository.save(user);

			model.addAttribute("title", "SignUp-Contact Manager");
			model.addAttribute("user", new User()); // after successfully registered blank the user fields

			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));

			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message",
					new Message("Something went wrong !! Agree Terms & Conditions", "alert-danger"));
			return "signup";
		}

	}

	// HANDLER FOR  CUSTOM LOGIN (SPRING SECURITY)
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Secure Login");  //key-value pair
		return "login";
	}
	
	/*
	 * //Dashboard
	 * 
	 * @RequestMapping("/dashboard") public String dashboard(Model model) {
	 * 
	 * 
	 * return "normal/user_dashboard"; }
	 */
}
