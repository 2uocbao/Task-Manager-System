package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.Comment;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.CommentEvent;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.CommentRequest;
import com.quocbao.taskmanagementsystem.payload.response.CommentResponse;
import com.quocbao.taskmanagementsystem.repository.CommentRepository;
import com.quocbao.taskmanagementsystem.service.CommentService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;

@Service
public class CommentServiceImpl implements CommentService {

	private final ApplicationEventPublisher applicationEventPublisher;
	private final CommentRepository commentRepository;
	private final UserHelperService userHelperService;
	private final TaskHelperService taskHelperService;
	private final MethodGeneral methodGeneral;
	private final IdEncoder idEncoder;

	public CommentServiceImpl(ApplicationEventPublisher applicationEventPublisher, CommentRepository commentRepository,
			UserHelperService userHelperService, TaskHelperService taskHelperService, MethodGeneral methodGeneral,
			IdEncoder idEncoder) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.commentRepository = commentRepository;
		this.userHelperService = userHelperService;
		this.taskHelperService = taskHelperService;
		this.methodGeneral = methodGeneral;
		this.idEncoder = idEncoder;
	}

	@Override
	public CommentResponse createComment(String userId, String taskId, CommentRequest commentRequest) {
		Task task = fetchTaskAndCheckUserHaveAccess(taskId, userId);
		User user = userHelperService.userExist(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Comment comment = new Comment(user, task, commentRequest.getText());
		if (commentRequest.getMention() != null) {
			String senderName = Optional.ofNullable(task.getUser())
					.map(userSender -> Optional.ofNullable(userSender.getFirstName()).orElse("") + " "
							+ Optional.ofNullable(userSender.getLastName()).orElse(""))
					.orElse("Unknown User");
			applicationEventPublisher.publishEvent(new CommentEvent(user.getId(),
					idEncoder.decode(commentRequest.getMention()), task.getId(), senderName, task.getTitle()));
		}
		return new CommentResponse(commentRepository.save(comment));
	}

	private Task fetchTaskAndCheckUserHaveAccess(String taskId, String userId) {
		Task task = taskHelperService.existTask(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		if (task.getAssignTo() != null) {
			methodGeneral.havePermission(idEncoder.decode(userId), task.getUser().getId(), task.getAssignTo().getId());
		} else {

			methodGeneral.validatePermission(idEncoder.decode(userId), task.getUser().getId());
		}
		return task;
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
