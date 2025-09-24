package com.quocbao.taskmanagementsystem.serviceimpl;

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
import com.quocbao.taskmanagementsystem.events.Notification.NotificationAddEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.ContactRequest;
import com.quocbao.taskmanagementsystem.payload.response.ContactResponse;
import com.quocbao.taskmanagementsystem.repository.ContactRepository;
import com.quocbao.taskmanagementsystem.service.ContactService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.specifications.ContactSpecification;

@Service
public class ContactServiceImpl implements ContactService {

	private final ContactRepository contactRepository;

	private final UserHelperService userHelperService;

	private final AuthenticationService authService;

	private final IdEncoder idEncoder;

	private final MethodGeneral methodGeneral;

	private final ApplicationEventPublisher applicationEventPublisher;

	public ContactServiceImpl(ContactRepository contactRepository,
			UserHelperService userHelperService, AuthenticationService authService, IdEncoder idEncoder,
			MethodGeneral methodGeneral, ApplicationEventPublisher applicationEventPublisher) {
		this.contactRepository = contactRepository;
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
		Boolean isConnected = contactRepository.exists(Specification.where((ContactSpecification
				.findContactByUserId(currentUserId).and(ContactSpecification.findContactReceive(toUserId)))
				.or(ContactSpecification
						.findContactByUserId(toUserId).and(ContactSpecification.findContactReceive(currentUserId)))));
		if (isConnected || currentUserId == toUserId) {
			throw new AccessDeniedException("Request do not access");
		}
		if (userHelperService.isUserExist(toUserId)) {
			User fromUser = User.builder().id(currentUserId).build();
			User toUser = User.builder().id(toUserId).build();
			Contacts contactBuilder = Contacts.builder().user(fromUser).friendId(toUser)
					.statusEnum(StatusEnum.REQUESTED)
					.build();
			Contacts contact = contactRepository.save(contactBuilder);
			publishNotificationEvent(currentUserId, toUserId, contact.getId(), NotificationType.REQUEST.toString());
		} else {
			throw new ResourceNotFoundException("Can not add contact");
		}
	}

	@Override
	public void updateContact(String contactId) {
		Long currentUserId = authService.getUserIdInContext();
		Long contactIdLong = idEncoder.decode(contactId);
		contactRepository.findById(contactIdLong).ifPresentOrElse(contact -> {
			if (!currentUserId.equals(contact.getFriendId().getId())
					|| currentUserId.equals(contact.getUser().getId())) {
				throw new AccessDeniedException("User do not have access");
			}
			contact.setStatusEnum(StatusEnum.ACCEPTED);
			contactRepository.save(contact);
			publishNotificationEvent(currentUserId, contact.getUser().getId(), contactIdLong,
					NotificationType.CONTACTACEPT.toString());
		}, () -> {
			throw new ResourceNotFoundException("Contact not found");
		});
	}

	@Override
	public void deleteContact(String id) {
		Long currentUserId = authService.getUserIdInContext();
		contactRepository.findById(idEncoder.decode(id)).ifPresentOrElse(contact -> {
			methodGeneral.havePermission(currentUserId, contact.getUser().getId(), contact.getFriendId().getId());
			contactRepository.delete(contact);
		}, () -> {
			throw new ResourceNotFoundException("Contact not found");
		});
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

	protected void publishNotificationEvent(Long senderId, Long receiverId, Long contentId, String contentType) {
		applicationEventPublisher.publishEvent(new NotificationAddEvent(senderId, receiverId, contentId,
				NotificationType.CONTACT.toString(), contentType));
	}
}
