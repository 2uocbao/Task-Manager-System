package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.entity.Comment;
import com.quocbao.taskmanagementsystem.entity.Mention;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Notification.NotificationAddEvent;
import com.quocbao.taskmanagementsystem.repository.CommentRepository;
import com.quocbao.taskmanagementsystem.repository.MentionRepository;
import com.quocbao.taskmanagementsystem.service.MentionService;
import com.quocbao.taskmanagementsystem.service.utils.CommentHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.specifications.MentionSpecification;

@Service
public class MentionServiceImpl implements MentionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MentionServiceImpl.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final CommentHelperService commentHelperService;
    private final UserHelperService userHelperService;
    private final MentionRepository mentionRepository;
    private final IdEncoder idEncoder;

    public MentionServiceImpl(ApplicationEventPublisher applicationEventPublisher,
            CommentHelperService commentHelperService, UserHelperService userHelperService,
            MentionRepository mentionRepository,
            CommentRepository commentRepository,
            IdEncoder idEncoder) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.commentHelperService = commentHelperService;
        this.userHelperService = userHelperService;
        this.mentionRepository = mentionRepository;
        this.idEncoder = idEncoder;
    }

    @Override
    public void createMention(Long userId, Long commentId, List<String> mentionIds) {
        mentionIds.stream().forEach(mention -> {
            Long userMentionId = idEncoder.decode(mention);
            Comment comment = commentHelperService.getCommentById(commentId);
            User user = userHelperService.getUser(userMentionId).get();
            Mention mentionBuilder = Mention.builder().comment(comment).user(user).build();
            mentionRepository.save(mentionBuilder);
            publishNotificationEvent(userId, userMentionId, comment.getTask().getId());
        });
    }

    @Override
    @Transactional
    public void updateMention(Long sender, Long commentId, List<String> mentionIds) {
        try {
            // Retrieve existing mentions
            List<Mention> existingMentions = mentionRepository
                    .findAll(Specification.where(MentionSpecification.findByComment(commentId)));

            // Map key user id and value mention entity
            Map<Long, Mention> existingMap = existingMentions.stream()
                    .collect(Collectors.toMap(m -> m.getUser().getId(), m -> m));

            // Retrieve comment
            Comment comment = commentHelperService.getCommentById(commentId);

            List<Mention> toDelete = new ArrayList<>();
            List<Mention> toSave = new ArrayList<>();

            for (String encodedId : mentionIds) {
                Long userId = idEncoder.decode(encodedId);

                if (existingMap.containsKey(userId)) {
                    // if exist map have contain key with element in list mention id from comment.
                    // do remove this key value in in exist map
                    existingMap.remove(userId);
                } else {
                    User user = userHelperService.getUser(userId).get();
                    Mention mention = Mention.builder().comment(comment).user(user).build();
                    toSave.add(mention);
                    publishNotificationEvent(sender, userId, comment.getTask().getId());
                }
            }

            toDelete.addAll(existingMap.values());

            LOGGER.info("Save " + toSave.size());
            if (!toSave.isEmpty()) {
                mentionRepository.saveAll(toSave);
            }
            LOGGER.info("Delete " + toDelete.size());
            if (!toDelete.isEmpty()) {
                mentionRepository.deleteAll(toDelete);
                // toDelete.stream().forEach(mention -> {
                // mentionRepository.deleteById(mention.getId());
                // });
            }
        } catch (Exception e) {
            LOGGER.error(e.getCause().toString());
        }
    }

    protected void publishNotificationEvent(Long senderId, Long receiverId, Long taskId) {
        applicationEventPublisher.publishEvent(
                new NotificationAddEvent(senderId, receiverId, taskId, NotificationType.COMMENT.toString(),
                        NotificationType.COMMENT.toString()));
    }
}
