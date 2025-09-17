package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.entity.Comment;
import com.quocbao.taskmanagementsystem.entity.Mention;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Notification.NotificationAddEvent;
import com.quocbao.taskmanagementsystem.repository.MentionRepository;
import com.quocbao.taskmanagementsystem.service.MentionService;
import com.quocbao.taskmanagementsystem.specifications.MentionSpecification;

@Service
public class MentionServiceImpl implements MentionService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final MentionRepository mentionRepository;
    private final IdEncoder idEncoder;

    public MentionServiceImpl(ApplicationEventPublisher applicationEventPublisher, MentionRepository mentionRepository,
            IdEncoder idEncoder) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.mentionRepository = mentionRepository;
        this.idEncoder = idEncoder;
    }

    @Override
    public void createMention(Long userId, Long commentId, List<String> mentionIds) {
        mentionIds.stream().forEach(mention -> {
            Long mentionId = idEncoder.decode(mention);
            Comment comment = Comment.builder().id(commentId).build();
            User user = User.builder().id(mentionId).build();
            mentionRepository.save(Mention.builder().comment(comment).user(user).build());
            publishNotificationEvent(userId, mentionId, commentId);
        });
    }

    @Override
    public void updateMention(Long sender, Long commentId, List<String> mentionIds) {
        List<Mention> existingMentions = mentionRepository
                .findAll(Specification.where(MentionSpecification.findByComment(commentId)));

        Map<Long, Mention> existingMap = existingMentions.stream()
                .collect(Collectors.toMap(m -> m.getUser().getId(), m -> m));

        List<Mention> toDelete = new ArrayList<>();
        List<Mention> toSave = new ArrayList<>();

        for (String encodedId : mentionIds) {
            Long userId = idEncoder.decode(encodedId);

            if (existingMap.containsKey(userId)) {
                existingMap.remove(userId);
            } else {
                Comment comment = Comment.builder().id(commentId).build();
                User user = User.builder().id(userId).build();
                Mention mention = Mention.builder().comment(comment).user(user).build();
                toSave.add(mention);
                publishNotificationEvent(commentId, userId, commentId);
            }
        }

        toDelete.addAll(existingMap.values());

        System.out.println(toSave.size());
        if (!toSave.isEmpty()) {
            mentionRepository.saveAll(toSave);
        }
        System.out.println(toDelete.size());
        if (!toDelete.isEmpty()) {
            mentionRepository.deleteAll(toDelete);
        }
    }

    protected void publishNotificationEvent(Long senderId, Long receiverId, Long commentId) {
        applicationEventPublisher.publishEvent(
                new NotificationAddEvent(senderId, receiverId, commentId, NotificationType.COMMENT.toString(),
                        NotificationType.COMMENT.toString()));
    }
}
