package com.quocbao.taskmanagementsystem.service.utils;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.StatusEnum;
import com.quocbao.taskmanagementsystem.entity.Contacts;
import com.quocbao.taskmanagementsystem.repository.ContactRepository;
import com.quocbao.taskmanagementsystem.specifications.ContactSpecification;

@Service
public class ContactHelperService {

	private final ContactRepository contactRepository;

	private final IdEncoder idEncoder;

	public ContactHelperService(ContactRepository contactRepository, IdEncoder idEncoder) {
		this.contactRepository = contactRepository;
		this.idEncoder = idEncoder;
	}

	public Boolean isConnected(Long userId, String userId1) {
		Specification<Contacts> specification = Specification.where(
				(ContactSpecification.findContactByUserId(userId)
						.and(ContactSpecification.findContactReceive(idEncoder.decode(userId1))))
						.or(ContactSpecification.findContactByUserId(idEncoder.decode(userId1))
								.and(ContactSpecification.findContactReceive(userId)))
						.and(ContactSpecification.findContactByStatus(StatusEnum.ACCEPTED.toString())));
		return contactRepository.exists(specification);
	}
}
