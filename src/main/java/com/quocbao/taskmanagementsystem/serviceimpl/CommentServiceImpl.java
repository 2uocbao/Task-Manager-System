package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.Comment;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.NotifiRequest;
import com.quocbao.taskmanagementsystem.payload.request.CommentRequest;
import com.quocbao.taskmanagementsystem.payload.response.CommentResponse;
import com.quocbao.taskmanagementsystem.repository.CommentRepository;
import com.quocbao.taskmanagementsystem.repository.UserRepository;
import com.quocbao.taskmanagementsystem.service.CommentService;
import com.quocbao.taskmanagementsystem.service.NotificationService;

@Service
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;
	private final MethodGeneral methodGeneral;
	private final IdEncoder idEncoder;

	public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository,
			NotificationService notificationService, MethodGeneral methodGeneral, IdEncoder idEncoder) {
		this.commentRepository = commentRepository;
		this.userRepository = userRepository;
		this.notificationService = notificationService;
		this.methodGeneral = methodGeneral;
		this.idEncoder = idEncoder;
	}

	@Override
	public CommentResponse createComment(String userId, String taskId, CommentRequest commentRequest) {

		User user = userRepository.findById(idEncoder.decode(userId))
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Set value new task review
		Comment comment = new Comment(user, Task.builder().id(idEncoder.decode(taskId)).build(),
				commentRequest.getText());

		// Get user have in message
		Pattern pattern = Pattern.compile("@(\\w+)");
		Matcher matcher = pattern.matcher(commentRequest.getText());
		if (matcher.find()) {
			sendNotification(matcher.group().split("@")[1], user.getEmail().split("@")[0], taskId);
		}
		commentRepository.save(comment);
		return new CommentResponse(comment);
	}

	protected void sendNotification(String usernameReceiver, String senderId, String taskId) {
		User user = userRepository.findByEmail(usernameReceiver + "@gmail.com");
		Optional.ofNullable(user).ifPresent(userPre -> {
			notificationService.createNotification(NotifiRequest.builder().tokenFcm(userPre.getToken())
					.contentId(taskId).receiverId(idEncoder.endcode(userPre.getId())).type("COMMENT").typeContent("MENTION")
					.senderId(senderId).build());
		});
	}

	@Override
	public CommentResponse updateComment(String userId, long commentId, CommentRequest commentRequest) {
		return commentRepository.findById(commentId).map(comment -> {
			methodGeneral.validatePermission(idEncoder.decode(userId), comment.getUser().getId());
			comment.setText(commentRequest.getText());
			return new CommentResponse(commentRepository.save(comment));
		}).orElseThrow(() -> new ResourceNotFoundException("Can not update task review"));
	}

	@Override
	public Page<CommentResponse> getCommentsofTask(String taskId, Pageable pageable) {

		return commentRepository.getCommentsByTaskIds(idEncoder.decode(taskId), pageable)
				.map(t -> new CommentResponse(t.getId(), t.getText(), idEncoder.endcode(t.getuserId()),
						t.getfirstName(), t.getlastName(), t.getimagePath(), t.getCreatedAt()));

	}

	@Override
	public void deleteComment(long commentId, String userId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new ResourceNotFoundException("Can not delete this task review"));
		methodGeneral.validatePermission(idEncoder.decode(userId), comment.getUser().getId());
		commentRepository.delete(comment);
	}
}
