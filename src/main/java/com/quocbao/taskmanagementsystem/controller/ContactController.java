package com.quocbao.taskmanagementsystem.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quocbao.taskmanagementsystem.common.DataResponse;
import com.quocbao.taskmanagementsystem.common.PaginationResponse;
import com.quocbao.taskmanagementsystem.payload.request.ContactRequest;
import com.quocbao.taskmanagementsystem.payload.request.UpdateContactRequest;
import com.quocbao.taskmanagementsystem.payload.response.ContactResponse;
import com.quocbao.taskmanagementsystem.service.ContactService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping
public class ContactController {

	private final ContactService contactService;

	public ContactController(ContactService contactService) {
		this.contactService = contactService;
	}

	@PostMapping("/contacts")
	public DataResponse sendRequestContact(@RequestBody ContactRequest contactRequest) {
		contactService.createContact(contactRequest);
		return new DataResponse(HttpStatus.OK.value(), null, "Add request successful");
	}

	@PutMapping("/users/{userId}/contacts/{id}")
	public DataResponse updateContact(@PathVariable String userId, @PathVariable Long id,
			@RequestBody UpdateContactRequest updateContactRequest) {
		contactService.updateContact(userId, id, updateContactRequest);
		return new DataResponse(HttpStatus.OK.value(), null, "Update request successful");
	}

	@DeleteMapping("/users/{userId}/contacts/{id}")
	public DataResponse deleteContact(@PathVariable String userId, @PathVariable Long id) {
		contactService.deleteContact(userId, id);
		return new DataResponse(HttpStatus.OK.value(), null, "Success");
	}

	@GetMapping("/users/{userId}/contacts")
	public PaginationResponse<ContactResponse> getContacts(@PathVariable String userId, @RequestParam String status,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		Direction direction = Direction.ASC;

		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

		Page<ContactResponse> contactResponse = contactService.listContactByStatus(userId, status, pageable);

		List<ContactResponse> contactResponses = contactResponse.stream().toList();

		return new PaginationResponse<>(HttpStatus.OK, contactResponses, contactResponse.getPageable().getPageNumber(),
				contactResponse.getSize(), contactResponse.getTotalElements(), contactResponse.getTotalPages(),
				contactResponse.getSort().isSorted(), contactResponse.getSort().isUnsorted(),
				contactResponse.getSort().isEmpty());
	}

	@GetMapping("/users/{userId}/contacts/searchWith")
	public PaginationResponse<ContactResponse> searchContacts(@PathVariable String userId, @RequestParam String status,
			@RequestParam String keySearch, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ContactResponse> contactResponse = contactService.searchContact(userId, status, keySearch, pageable);

		List<ContactResponse> contactResponseList = contactResponse.stream().toList();

		return new PaginationResponse<>(HttpStatus.OK, contactResponseList,
				contactResponse.getPageable().getPageNumber(), contactResponse.getSize(),
				contactResponse.getTotalElements(), contactResponse.getTotalPages(),
				contactResponse.getSort().isSorted(), contactResponse.getSort().isUnsorted(),
				contactResponse.getSort().isEmpty());
	}

}
