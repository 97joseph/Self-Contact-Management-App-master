package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import com.razorpay.*;

@Controller
//@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	private MyOrderRepository myOrderRepository;

	// METHOD FOR COMMON DATA TO RESPONSE
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME :" + userName); // logged-in user name(email)

		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER :" + user);// logged-in user details here

		model.addAttribute("user", user);

	}

	// DASHBOARD HOME HANDLER
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		/*
		 * String userName = principal.getName(); System.out.println("USERNAME :" +
		 * userName); // logged-in user name(email)
		 * 
		 * User user = userRepository.getUserByUserName(userName);
		 * System.out.println("USER :" + user);// logged-in user details here
		 * 
		 * model.addAttribute("user", user);
		 */
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// OPEN ADD FORM HANDLER
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// PROCESSING ADD CONTACT FORM
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// Processing and uploading file..
			if (file.isEmpty()) {

				contact.setImage("profile.png");
				// System.out.println("No file is there ..!!");

			} else {
				// upload the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded....");
			}

			// Bi-directional mapping

			contact.setUser(user);

			user.getContacts().add(contact);

			// Save in database

			this.userRepository.save(user); // user will also add details of contact.

			System.out.println("Data :" + contact);

			System.out.println("Contact Added To Database...");

			// Message Success!!
			session.setAttribute("message", new Message("Your contact is added successfully!!", "success")); // key-value

		} catch (Exception e) {

			System.out.println("Error Message :" + e.getMessage());
			e.printStackTrace();

			// Message Error!!
			session.setAttribute("message", new Message("Something went wrong, Try Again !!", "danger"));
		}
		return "normal/add_contact_form";
	}

	// VIEW ALL CONTACTS HANDLER
	// per page=5[n]
	// current page=0[page]

	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		/*
		 * One way:(Using Principal interface) String userName = principal.getName();
		 * //which is email User user = this.userRepository.getUserByUserName(userName);
		 * List<Contact> contacts = user.getContacts();
		 */

		// Another Way:(Using Contact Repository)
		String userName = principal.getName(); // userId

		User user = this.userRepository.getUserByUserName(userName);
		// Pageable has 2 information , current page and contact per page

		Pageable pageable = PageRequest.of(page, 3);

		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);

		m.addAttribute("contacts", contacts);

		m.addAttribute("currentPage", page);

		m.addAttribute("totalPages", contacts.getTotalPages());

		m.addAttribute("title", "View-contacts");

		return "normal/show_contacts";
	}

	// HANDLER FOR SHOWING PERTICULAR CONTACT DETAILS
	@RequestMapping("/contact/{cId}")

	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("CID" + cId);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		// For Security purpose that no unauthorised person could access another
		// person's contacts
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) // if logged-in person's id == contact's user's id
		{
			// then
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		return "normal/contact_detail";
	}

	// DELETE CONTACT HANDLER
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {

		Contact contact = this.contactRepository.findById(cId).get();
//		System.out.println("Contact" + contact.getcId());

		// Check For Security purpose that no unauthorised person could access another
		// person's contacts

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

//equal() method will be called then,
		user.getContacts().remove(contact);

		/*
		 * if (user.getId() == contact.getUser().getId()) // if logged-in person's id ==
		 * // contact's user's id
		 * 
		 * // before deleting we have to set user to null because of CASCADING ALL
		 * DELETE // WAS NOT // WORKED HERE so need to unlink user and contact.
		 * //Solution: We use orphanRemoval=true with cascade contact.setUser(null);
		 */

		// NOW REMOVE IMAGE

		// 1.GET THE IMAGE path and NAME then remove the image too.
		/*
		 * File deleteFile = new ClassPathResource("static/img").getFile(); File file1 =
		 * new File(deleteFile, contact.getImage()); file1.delete();
		 */
//		this.contactRepository.delete(contact);

		this.userRepository.save(user); // this will update the associated contacts too

		System.out.println("CONTACT DELETED...");

		session.setAttribute("message", new Message("Contact Removed Successfully!!", "success"));

		return "redirect:/show-contacts/0";
	}

	// UPDATE FORM HANDLER
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model) {

		model.addAttribute("title", "Update Contact");

		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// UPDATE PROCESS HANDLER
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session, Principal principal) {

		try {
			// fetch old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();

			if (!file.isEmpty()) {
				// 1. then delete the old one
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				file1.delete();

				// 2. and update with new one(rewrite)

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename()); // image name also will update

			}

			else {
				contact.setImage(oldContactDetail.getImage()); // as its is as previous
			}
			User user = this.userRepository.getUserByUserName(principal.getName());

			contact.setUser(user);

			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your Contact is Updated", "success"));

		} catch (Exception e) {

		}

		System.out.println("Contact name:" + contact.getName());
		System.out.println("Contact id:" + contact.getcId());

		return "redirect:/contact/" + contact.getcId();
	}

	// YOUR PROFILE HANDLER
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile Page");

		return "normal/profile";
	}

	// open setting handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}

	// Change-password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, Model model, HttpSession session) {

		model.addAttribute("title", "Settings");

		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);

		System.out.println("OlD PASSWORD:" + oldPassword);
		System.out.println("NEW PASSWORD:" + newPassword);
		System.out.println(currentUser.getPassword());

		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			// change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Password Updated!!", "success"));

		} else {
			// error...
			session.setAttribute("message", new Message("Incorrect (old)Password!!", "danger"));
			return "redirect:/settings";
		}

		return "redirect:/index";
	}

	// Payment gateway Handler(creating order for payment)
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {

		System.out.println(data);
		int amt = Integer.parseInt(data.get("amount").toString());

		RazorpayClient client = new RazorpayClient("rzp_test_GXfYoYpvSiGtCx", "Uqa9mRVcrFNcScWNnqQIEvyt");

		JSONObject options = new JSONObject();
		options.put("amount", amt * 100);
		options.put("currency", "INR");
		options.put("receipt", "txn_123456");

		// Creating new order
		Order order = client.Orders.create(options);
		System.out.println(order);

		// save this to our database
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount") + "");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));

		this.myOrderRepository.save(myOrder);
		return order.toString();
	}

	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data) {
		
		MyOrder myorder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		
		myorder.setPaymentId(data.get("payment_id").toString());
		myorder.setStatus(data.get("status").toString());
		
		// update
		this.myOrderRepository.save(myorder);
		
		System.out.println(data);
		return ResponseEntity.ok(Map.of("msg", "updated"));

	}

}