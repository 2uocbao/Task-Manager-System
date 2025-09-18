package com.quocbao.taskmanagementsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.payload.request.ContactRequest;
import com.quocbao.taskmanagementsystem.payload.response.ContactResponse;

public interface ContactService {

	public void createContact(ContactRequest contactRequest);

	public void updateContact(String id);

	public void deleteContact(String id);

	public Page<ContactResponse> listContactByStatus(String status, Pageable pageable);

	public Page<ContactResponse> searchContact(String status, String keyword, Pageable pageable);

	public Page<ContactResponse> searchAddMember(String teamId, String keyword, Pageable pageable);

}
