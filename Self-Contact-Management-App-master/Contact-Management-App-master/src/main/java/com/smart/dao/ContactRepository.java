package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	// For Pagination
	// HQL
	@Query("from Contact as c where c.user.id=:userId")
	// Pageable has 2 information , current page and contact per page
	public Page<Contact> findContactByUser(@Param("userId") int userId, Pageable pageable);

	// For Search
	// user name contains a keyword that contains the searched data and 
	// only logged-in user's contacts will be searched
	public List<Contact> findByNameContainingAndUser(String name, User user);

}
