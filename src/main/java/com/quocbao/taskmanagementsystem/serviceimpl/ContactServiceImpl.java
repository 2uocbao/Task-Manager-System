package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.common.StatusEnum;
import com.quocbao.taskmanagementsystem.entity.Contacts;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.ContactEvent;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.ContactRequest;
import com.quocbao.taskmanagementsystem.payload.request.UpdateContactRequest;
import com.quocbao.taskmanagementsystem.payload.response.ContactResponse;
import com.quocbao.taskmanagementsystem.repository.ContactRepository;
import com.quocbao.taskmanagementsystem.service.ContactService;
import com.quocbao.taskmanagementsystem.service.utils.NotifiHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.specifications.ContactSpecification;

@Service
public class ContactServiceImpl implements ContactService {

	private final ContactRepository contactRepository;

	private final NotifiHelperService notifiHelperService;

	private final UserHelperService userHelperService;

	private final IdEncoder idEncoder;

	private final MethodGeneral methodGeneral;

	private final ApplicationEventPublisher applicationEventPublisher;

	public ContactServiceImpl(ContactRepository contactRepository, NotifiHelperService notifiHelperService,
			UserHelperService userHelperService, IdEncoder idEncoder, MethodGeneral methodGeneral,
			ApplicationEventPublisher applicationEventPublisher) {
		this.contactRepository = contactRepository;
		this.notifiHelperService = notifiHelperService;
		this.userHelperService = userHelperService;
		this.idEncoder = idEncoder;
		this.methodGeneral = methodGeneral;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void createContact(ContactRequest contactRequest) {
		if (contactRepository.isConnected(idEncoder.decode(contactRequest.getFromUser()),
				idEncoder.decode(contactRequest.getToUser()))) {
			return;
		}
		userHelperService.userExist(contactRequest.getToUser()).ifPresentOrElse(user -> {
			Contacts contacts = Contacts.builder()
					.user(User.builder().id(idEncoder.decode(contactRequest.getFromUser())).build()).friendId(user)
					.statusEnum(StatusEnum.REQUESTED).build();
			Contacts contact = contactRepository.save(contacts);
			String senderName = Optional.ofNullable(user.getFirstName()).orElse("") + " "
					+ Optional.ofNullable(user.getLastName()).orElse("");
			applicationEventPublisher.publishEvent(new ContactEvent(idEncoder.decode(contactRequest.getFromUser()),
					user.getId(), contact.getId(), senderName));
		}, () -> new ResourceNotFoundException("Can not add contact"));

	}

	@Override
	public void updateContact(String userId, String contactId, UpdateContactRequest updateContactRequest) {
		Contacts contacts = contactRepository.findById(idEncoder.decode(contactId))
				.orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
		methodGeneral.validatePermission(idEncoder.decode(userId), contacts.getFriendId().getId());
		contacts.setStatusEnum(StatusEnum.valueOf(updateContactRequest.getStatus()));
		notifiHelperService.updateNotifi(idEncoder.decode(contactId), NotificationType.CONTACT.toString());
		contactRepository.save(contacts);
	}

	@Override
	public void deleteContact(String userId, String id) {
		Contacts contacts = contactRepository.findById(idEncoder.decode(id))
				.orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
		methodGeneral.havePermission(idEncoder.decode(userId), contacts.getUser().getId(),
				contacts.getFriendId().getId());
		notifiHelperService.deleteNotification(contacts.getId(), NotificationType.CONTACT.toString());
		contactRepository.delete(contacts);
	}

	@Override
	public Page<ContactResponse> listContactByStatus(String userId, String status, Pageable pageable) {
		Specification<Contacts> specificationBase = null;
		if ("RECEIVED".equals(status)) {
			status = "REQUESTED";
			specificationBase = ContactSpecification.findContactReceive(idEncoder.decode(userId));
		} else if ("ACCEPTED".equals(status)) {
			specificationBase = ContactSpecification.findContactReceive(idEncoder.decode(userId))
					.or(ContactSpecification.findContactByUserId(idEncoder.decode(userId)));

		} else {
			specificationBase = ContactSpecification.findContactByUserId(idEncoder.decode(userId));
		}
		Specification<Contacts> specification = Specification.where(specificationBase)
				.and(ContactSpecification.findContactByStatus(status));

		Page<Contacts> contacts = contactRepository.findAll(specification, pageable);

		Page<ContactResponse> contactResponse = contacts.map(contact -> {
			if (contact.getUser().getId() == idEncoder.decode(userId)) {
				return new ContactResponse(idEncoder.endcode(contact.getId()),
						idEncoder.endcode(contact.getFriendId().getId()), contact.getFriendId().getFirstName(),
						contact.getFriendId().getLastName(), contact.getFriendId().getEmail(),
						contact.getFriendId().getImage(), contact.getStatusEnum().toString());
			}
			return new ContactResponse(idEncoder.endcode(contact.getId()), idEncoder.endcode(contact.getUser().getId()),
					contact.getUser().getFirstName(), contact.getUser().getLastName(), contact.getUser().getEmail(),
					contact.getUser().getImage(), contact.getStatusEnum().toString());
		});

		return contactResponse;
	}

	@Override
	public Page<ContactResponse> searchContact(String userId, String status, String keySearch, Pageable pageable) {
		if ("RECEIVED".equals(status)) {
			status = "REQUESTED";
		}
		return contactRepository
				.searchContact(idEncoder.decode(userId), StatusEnum.valueOf(status), keySearch, pageable)
				.map(t -> new ContactResponse(idEncoder.endcode(t.getId()), idEncoder.endcode(t.getUserId()),
						t.getFirstName(), t.getLastName(), t.getEmail(), t.getImage(), t.getStatus()));
	}

}
