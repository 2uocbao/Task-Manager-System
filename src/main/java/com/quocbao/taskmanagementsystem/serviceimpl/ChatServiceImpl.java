package com.quocbao.taskmanagementsystem.serviceimpl;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.entity.Chat;
import com.quocbao.taskmanagementsystem.entity.Member;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.response.ChatResponse;
import com.quocbao.taskmanagementsystem.payload.response.UserResponse;
import com.quocbao.taskmanagementsystem.repository.ChatRepository;
import com.quocbao.taskmanagementsystem.repository.MemberRepository;
import com.quocbao.taskmanagementsystem.service.ChatService;

import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;

@Service
public class ChatServiceImpl implements ChatService {

	private final ChatRepository chatRepository;
	private final MemberRepository memberRepository;
	private final IdEncoder idEndcoder;

	public ChatServiceImpl(ChatRepository chatRepository, MemberRepository memberRepository, IdEncoder idEndcoder) {
		this.chatRepository = chatRepository;
		this.memberRepository = memberRepository;
		this.idEndcoder = idEndcoder;
	}

	@Override
	@Transactional
	public ChatResponse createChat(String userId, String withUser) {
		Set<Member> members = new HashSet<>();
		Chat chat = chatRepository.save(new Chat("null", members));
		members.add(new Member(chat.getId(), idEndcoder.decode(userId)));
		members.add(new Member(chat.getId(), idEndcoder.decode(withUser)));
		return new ChatResponse(chat.getId(), "null", "null", chat.getCreatedAt());
	}

	// Optimize, JPASpecificationExcuter
	@Override
	public Page<ChatResponse> getChatsByUserId(String userId, Pageable pageable, String keySearch) {
		Page<Tuple> result = chatRepository.getChatsOfUser(idEndcoder.decode(userId), pageable);
		return result.map(
				t -> new ChatResponse((Long) t.get(0), (String) t.get(1), (String) t.get(2), (Timestamp) t.get(3)));
	}

	@Override
	public Chat updateChat(String userId, long chatId, String name) {
		Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("Request failed."));

		chat.setName(name);

		return chatRepository.save(chat);

	}

	@Override
	public String deleteChat(String userId, long chatId) {
		Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("Request failed."));
		chatRepository.delete(chat);
		return "Successful";
	}

	@Override
	public String addUserToChat(String userId, String memberId, long chatId) {
		memberRepository.save(new Member(chatId, idEndcoder.decode(memberId)));
		return "Successful";
	}

	@Override
	public String removeUserInChat(String userId, String memberId, long chatId) {
//		memberRepository.delete(MemberSpecification.findMemberByUserId(idEndcoder.decode(memberId)));
		return "Successful";

	}

	@Override
	public Page<UserResponse> getUsersInChat(long chatId, Pageable pageable, String keySearch) {
//		Specification<Member> specification;
//		specification = MemberSpecification.findMemberByChatId(chatId);
//		Page<Member> members = memberRepository.findAll(specification, pageable);
//		Page<User> users = members.map(Member::getUser);
//		return users.map(UserResponse::new);
		return null;
	}

	@Override
	public Page<UserResponse> getUsersConnect(String userId, Pageable pageable, String keySearch) {
//		Specification<Member> specification = MemberSpecification.findMemberByUserId(idEndcoder.decode(userId));
//		Page<Member> members = memberRepository.findAll(specification, pageable);
//		List<String> chatId = members.stream().map(Member::getChat).map(Chat::getId).distinct().toList();
//		Specification<Member> ChatMembersSpecification = MemberSpecification.findMemberByChatId(chatId)
//				.and(MemberSpecification.excludeUserId(userId));
//		Page<Member> ChatMembers = memberRepository.findAll(ChatMembersSpecification, pageable);
//		return ChatMembers.map(t -> new UserResponse(t.getUser()));
		return null;
	}

}
