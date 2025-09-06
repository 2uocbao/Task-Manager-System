package com.quocbao.taskmanagementsystem.serviceimpl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.common.StatusEnum;
import com.quocbao.taskmanagementsystem.entity.Contacts;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.NotifiEvent.ContactEvent;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.ContactRequest;
import com.quocbao.taskmanagementsystem.payload.request.UpdateContactRequest;
import com.quocbao.taskmanagementsystem.payload.response.ContactResponse;
import com.quocbao.taskmanagementsystem.repository.ContactRepository;
import com.quocbao.taskmanagementsystem.service.ContactService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.NotifiHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;

@Service
public class ContactServiceImpl implements ContactService {

	private final ContactRepository contactRepository;

	private final NotifiHelperService notifiHelperService;

	private final UserHelperService userHelperService;

	private final AuthenticationService authService;

	private final IdEncoder idEncoder;

	private final MethodGeneral methodGeneral;

	private final ApplicationEventPublisher applicationEventPublisher;

	public ContactServiceImpl(ContactRepository contactRepository, NotifiHelperService notifiHelperService,
			UserHelperService userHelperService, AuthenticationService authService, IdEncoder idEncoder,
			MethodGeneral methodGeneral, ApplicationEventPublisher applicationEventPublisher) {
		this.contactRepository = contactRepository;
		this.notifiHelperService = notifiHelperService;
		this.userHelperService = userHelperService;
		this.authService = authService;
		this.idEncoder = idEncoder;
		this.methodGeneral = methodGeneral;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void createContact(ContactRequest contactRequest) {
		Long currentUserId = authService.getUserIdInContext();

		Long toUserId = idEncoder.decode(contactRequest.getToUser());
		Boolean isConnected = contactRepository.isConnected(currentUserId,
				idEncoder.decode(contactRequest.getToUser()));
		if (isConnected || currentUserId == toUserId) {
			return;
		}
		if (userHelperService.isUserExist(currentUserId)) {
			User fromUser = User.builder().id(currentUserId).build();
			User toUser = userHelperService.getUser(toUserId).get();
			Contacts contacts = Contacts.builder().user(fromUser).friendId(toUser).statusEnum(StatusEnum.REQUESTED)
					.build();
			Contacts contact = contactRepository.save(contacts);

			applicationEventPublisher
					.publishEvent(new ContactEvent(currentUserId, idEncoder.decode(contactRequest.getToUser()),
							contact.getId(), contactRequest.getSenderName(), NotificationType.CONTACT.toString(),
							toUser.getLanguage(), toUser.getToken() == null ? null : toUser.getToken()));

		} else {
			throw new ResourceNotFoundException("Can not add contact");
		}
	}

	@Override
	public void updateContact(String contactId, UpdateContactRequest updateContactRequest) {
		Long currentUserId = authService.getUserIdInContext();
		contactRepository.findById(idEncoder.decode(contactId)).ifPresentOrElse(contact -> {
			methodGeneral.validatePermission(currentUserId, contact.getFriendId().getId());
			contact.setStatusEnum(StatusEnum.valueOf(updateContactRequest.getStatus()));
			notifiHelperService.deleteNotification(contact.getId(), NotificationType.CONTACT.toString());
			contactRepository.save(contact);
			userHelperService.getUser(idEncoder.decode(updateContactRequest.getToUser())).ifPresent(user -> {
				applicationEventPublisher.publishEvent(new ContactEvent(currentUserId,
						idEncoder.decode(updateContactRequest.getToUser()), contact.getId(),
						updateContactRequest.getSenderName(), NotificationType.CONTACTACEPT.toString(),
						user.getLanguage(), user.getToken() == null ? null : user.getToken()));
			});

		}, () -> new ResourceNotFoundException("Contact not found"));
	}

	@Override
	public void deleteContact(String id) {
		Long currentUserId = authService.getUserIdInContext();
		contactRepository.findById(idEncoder.decode(id)).ifPresentOrElse(contact -> {
			methodGeneral.havePermission(currentUserId, contact.getUser().getId(), contact.getFriendId().getId());
			notifiHelperService.deleteNotification(contact.getId(), NotificationType.CONTACT.toString());
			contactRepository.delete(contact);

		}, () -> new ResourceNotFoundException("Contact not found"));

	}

	@Override
	public Page<ContactResponse> listContactByStatus(String status, Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		try {
			StatusEnum.valueOf(status);
		} catch (Exception e) {
			throw new ResourceNotFoundException("Status invalid");
		}

		if ("RECEIVED".equals(status)) {
			status = "REQUESTED";
			return contactRepository.getContactsByReceiver(currentUserId, StatusEnum.REQUESTED, pageable)
					.map(contact -> {
						return new ContactResponse(idEncoder.encode(contact.getId()),
								idEncoder.encode(contact.getFriendId()), contact.getFirstName(), contact.getLastName(),
								contact.getEmail(), contact.getImage(), contact.getStatus());
					});
		} else if ("ACCEPTED".equals(status)) {
			return contactRepository.getContactsAccepted(currentUserId, StatusEnum.ACCEPTED, pageable).map(contact -> {
				return new ContactResponse(idEncoder.encode(contact.getId()), idEncoder.encode(contact.getFriendId()),
						contact.getFirstName(), contact.getLastName(), contact.getEmail(), contact.getImage(),
						contact.getStatus());
			});
		}
		return contactRepository.getContactRequested(currentUserId, StatusEnum.REQUESTED, pageable).map(contact -> {
			return new ContactResponse(idEncoder.encode(contact.getId()), idEncoder.encode(contact.getFriendId()),
					contact.getFirstName(), contact.getLastName(), contact.getEmail(), contact.getImage(),
					contact.getStatus());
		});
	}

	@Override
	public Page<ContactResponse> searchContact(String status, String keyword, Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		if ("RECEIVED".equals(status)) {
			status = "REQUESTED";
		}
		return contactRepository.searchContact(currentUserId, StatusEnum.valueOf(status), keyword, pageable)
				.map(t -> new ContactResponse(idEncoder.encode(t.getId()), idEncoder.encode(t.getUserId()),
						t.getFirstName(), t.getLastName(), t.getEmail(), t.getImage(), t.getStatus()));
	}

	@Override
	public Page<ContactResponse> searchAddMember(String teamId, String keyword, Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		return contactRepository.searchForAddToTeam(currentUserId, idEncoder.decode(teamId), keyword, pageable)
				.map(t -> {
					return new ContactResponse(teamId, idEncoder.encode(t.getUserId()), t.getFirstName(),
							t.getLastName(), t.getEmail(), t.getImage(), null);
				});
	}
}
