package com.quocbao.taskmanagementsystem.serviceimpl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.MessageRequest;
import com.quocbao.taskmanagementsystem.payload.response.MessageResponse;
import com.quocbao.taskmanagementsystem.repository.MessageRepository;
import com.quocbao.taskmanagementsystem.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

	private final MessageRepository messageRepository;

	private final IdEncoder idEndcoder;

	public MessageServiceImpl(MessageRepository messageRepository, IdEncoder idEndcoder) {
		this.messageRepository = messageRepository;
		this.idEndcoder = idEndcoder;
	}

	@Override
	public void sendMessageToChatId(MessageRequest chatMessage) {
//		Specification<Member> spec = MemberSpecification.findMemberByChatId(ChatIds)
//				.and(MemberSpecification.excludeUserId(idEndcoder.decode(chatMessage.getSender())));
//		List<Member> members = memberRepository.findAll(spec);
//		for (Member member : members) {
//			if (!userStatusService.isUserOnline(member.getUser().getId())) {
//				messageRepository.save(new Message(idEndcoder.decode(chatMessage.getReceiver()),
//						idEndcoder.decode(chatMessage.getSender()), chatMessage.getContent()));
//				break;
//			}
//			if (userStatusService.isUserSubscribe(member.getUser().getId(),
//					"/queue/message-user" + chatMessage.getReceiver())) {
//				simpMessagingTemplate.convertAndSend("/queue/message-user" + chatMessage.getReceiver(), chatMessage);
//			} else {
//				simpMessagingTemplate.convertAndSend("/queue/message-notifi" + member.getUser().getId(), chatMessage);
//			}
//			messageRepository
//					.save(new Message(idEndcoder.decode(chatMessage.getReceiver()),
//							idEndcoder.decode(chatMessage.getSender()), chatMessage.getContent()));
//		}
	}

	@Override
	public Page<MessageResponse> getMessagesByChatId(long chatId, Pageable pageable) {
//		Specification<Message> specification = MessageSpecification
//				.getMessagesByChatId(Chat.builder().id(chatId).build());
//		return messageRepository.findAll(specification, pageable).map(MessageResponse::new);
		return null;
	}

	@Override
	public String deleteMessageByUserId(String messageId, String userId) {
		return messageRepository.findById(idEndcoder.decode(messageId)).map(t -> {
			if (t.getUser().getId() == idEndcoder.decode(userId)) {
				messageRepository.delete(t);
			}
			return "Successful";
		}).orElseThrow(() -> new ResourceNotFoundException("Resource can not delete."));
	}

	public User getUser() {
		Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return (User) object;
	}

}
