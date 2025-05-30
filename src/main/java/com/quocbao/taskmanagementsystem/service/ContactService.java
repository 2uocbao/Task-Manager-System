package com.quocbao.taskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.payload.request.ContactRequest;
import com.quocbao.taskmanagementsystem.payload.request.UpdateContactRequest;
import com.quocbao.taskmanagementsystem.payload.response.ContactResponse;

public interface ContactService {

	public void createContact(ContactRequest contactRequest);

	public void updateContact(String userId, String id, UpdateContactRequest updateContactRequest);

	public void deleteContact(String userId, String id);

	public Page<ContactResponse> listContactByStatus(String userId, String status, Pageable pageable);

	public Page<ContactResponse> searchContact(String userId, String status, String keySearch, Pageable pageable);
}
