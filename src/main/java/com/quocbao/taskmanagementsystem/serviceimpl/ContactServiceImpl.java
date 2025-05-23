package com.quocbao.taskmanagementsystem.serviceimpl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.common.StatusEnum;
import com.quocbao.taskmanagementsystem.entity.Contacts;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.ContactRequest;
import com.quocbao.taskmanagementsystem.payload.request.NotifiRequest;
import com.quocbao.taskmanagementsystem.payload.request.UpdateContactRequest;
import com.quocbao.taskmanagementsystem.payload.response.ContactResponse;
import com.quocbao.taskmanagementsystem.repository.ContactRepository;
import com.quocbao.taskmanagementsystem.repository.UserRepository;
import com.quocbao.taskmanagementsystem.service.ContactService;
import com.quocbao.taskmanagementsystem.service.NotificationService;
import com.quocbao.taskmanagementsystem.specifications.ContactSpecification;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ContactServiceImpl implements ContactService {

	private final ContactRepository contactRepository;

	private final NotificationService notifiService;

	private final UserRepository userRepository;

	private final IdEncoder idEncoder;

	private final MethodGeneral methodGeneral;

	public ContactServiceImpl(ContactRepository contactRepository, NotificationService notifiService,
			UserRepository userRepository, IdEncoder idEncoder, MethodGeneral methodGeneral) {
		this.contactRepository = contactRepository;
		this.notifiService = notifiService;
		this.userRepository = userRepository;
		this.idEncoder = idEncoder;
		this.methodGeneral = methodGeneral;
	}

	@Override
	public void createContact(ContactRequest contactRequest) {
		if (contactRepository.isConnected(idEncoder.decode(contactRequest.getFromUser()),
				idEncoder.decode(contactRequest.getToUser()))) {
			return;
		}

		User user = userRepository.findById(idEncoder.decode(contactRequest.getFromUser())).get();
		String userToken = userRepository.getTokenOfUser(idEncoder.decode(contactRequest.getToUser())).getToken();

		Contacts contacts = Contacts.builder().user(user)
				.friendId(User.builder().id(idEncoder.decode(contactRequest.getToUser())).build())
				.statusEnum(StatusEnum.PENDING).build();

		Contacts contact = contactRepository.save(contacts);

		if (!userToken.isEmpty()) {
			notifiService.createNotification(NotifiRequest.builder().senderId(idEncoder.endcode(user.getId()))
					.contentId(contact.getId().toString()).receiverId(contactRequest.getToUser()).type("CONTACT")
					.tokenFcm(userToken).typeContent("REQUEST").build());
		}

	}

	@Override
	public void updateContact(String userId, Long id, UpdateContactRequest updateContactRequest) {
		Contacts contacts = contactRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
		methodGeneral.validatePermission(idEncoder.decode(userId), contacts.getFriendId().getId());
		contacts.setStatusEnum(StatusEnum.valueOf(updateContactRequest.getStatus()));
		contactRepository.save(contacts);
	}

	@Override
	public void deleteContact(String userId, Long id) {
		Contacts contacts = contactRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
		methodGeneral.havePermission(idEncoder.decode(userId), contacts.getUser().getId(),
				contacts.getFriendId().getId());

		notifiService.deleteNotification(contacts.getUser().getId(),
				contacts.getFriendId().getId(), "CONTACT");
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
				return new ContactResponse(contact.getId(), idEncoder.endcode(contact.getFriendId().getId()),
						contact.getFriendId().getFirstName(), contact.getFriendId().getLastName(),
						contact.getFriendId().getEmail(), contact.getFriendId().getImage());
			}
			return new ContactResponse(contact.getId(), idEncoder.endcode(contact.getFriendId().getId()),
					contact.getFriendId().getFirstName(), contact.getFriendId().getLastName(),
					contact.getFriendId().getEmail(), contact.getFriendId().getImage());
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
				.map(t -> new ContactResponse(t.getId(), idEncoder.endcode(t.getUserId()), t.getFirstName(),
						t.getLastName(), t.getEmail(), t.getImage()));
	}

}
