package com.quocbao.taskmanagementsystem.serviceimpl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.RoleEnum;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.Comment;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Mention.MentionAddEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.CommentRequest;
import com.quocbao.taskmanagementsystem.payload.response.CommentResponse;
import com.quocbao.taskmanagementsystem.repository.CommentRepository;
import com.quocbao.taskmanagementsystem.service.CommentService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskAssignmentHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;

@Service
public class CommentServiceImpl implements CommentService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuthenticationService authService;
    private final CommentRepository commentRepository;
    private final TaskHelperService taskHelperService;
    private final TaskAssignmentHelperService taskAssignHelperService;
    private final IdEncoder idEncoder;

    public CommentServiceImpl(ApplicationEventPublisher applicationEventPublisher, AuthenticationService authService,
            CommentRepository commentRepository, TaskHelperService taskHelperService,
            TaskAssignmentHelperService taskAssignHelperService,
            IdEncoder idEncoder) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.authService = authService;
        this.commentRepository = commentRepository;
        this.taskHelperService = taskHelperService;
        this.taskAssignHelperService = taskAssignHelperService;
        this.idEncoder = idEncoder;
    }

    @Override
    @Transactional
    public CommentResponse createComment(String taskId, CommentRequest commentRequest) {
        Long currentUserId = authService.getUserIdInContext();
        Long taskIdLong = idEncoder.decode(taskId);
        if (!taskHelperService.isTaskExist(taskIdLong)) {
            throw new ResourceNotFoundException("The task for add comment do not exist");
        }
        if (!taskAssignHelperService.isUserInTask(currentUserId, taskIdLong)) {
            throw new AccessDeniedException("User can not add comment to this task");
        }
        Task task = Task.builder().id(taskIdLong).build();
        User user = User.builder().id(currentUserId).build();
        Comment commentBuilder = Comment.builder().task(task).user(user).text(commentRequest.getText()).build();
        Comment comment = commentRepository.save(commentBuilder);
        applicationEventPublisher
                .publishEvent(new MentionAddEvent(currentUserId, comment.getId(), taskIdLong,
                        commentRequest.getText()));
        return new CommentResponse(comment);
    }

    @Override
    public CommentResponse updateComment(Long commentId, CommentRequest commentRequest) {
        Long currentUserId = authService.getUserIdInContext();
        return commentRepository.findById(commentId).map(comment -> {
            if (!comment.getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("User do not have access");
            }
            comment.setText(commentRequest.getText());
            Comment result = commentRepository.save(comment);
            // applicationEventPublisher
            // .publishEvent(new MentionUpdateEvent(currentUserId, commentId,
            // comment.getTask().getId(),
            // commentRequest.getMention()));
            return new CommentResponse(result);
        }).orElseThrow(() -> {
            throw new ResourceNotFoundException("Can not update task review");
        });
    }

    @Override
    public Page<CommentResponse> getCommentsofTask(String taskId, Pageable pageable) {
        return commentRepository.getCommentsByTaskIds(idEncoder.decode(taskId),
                pageable)
                .map(t -> new CommentResponse(t.getId(), t.getText(),
                        idEncoder.encode(t.getuserId()),
                        t.getfirstName(), t.getlastName(), t.getimagePath(), t.getCreatedAt()));

    }

    @Override
    public void deleteComment(Long commentId) {
        Long currentUserId = authService.getUserIdInContext();
        commentRepository.findById(commentId).ifPresentOrElse(comment -> {
            if (!comment.getUser().getId().equals(currentUserId)) {
                if (!taskAssignHelperService.isRoleUserInTask(currentUserId,
                        comment.getTask().getId(), RoleEnum.ADMIN)) {
                    throw new AccessDeniedException("The request do not have access");
                }
            }
            commentRepository.delete(comment);
        }, () -> {
            throw new ResourceNotFoundException("This comment was deleted");
        });
    }
}
