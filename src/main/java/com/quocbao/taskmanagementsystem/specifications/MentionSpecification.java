package com.quocbao.taskmanagementsystem.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.entity.Comment_;
import com.quocbao.taskmanagementsystem.entity.Mention;
import com.quocbao.taskmanagementsystem.entity.Mention_;

public class MentionSpecification {

    public MentionSpecification() {
    }

    public static Specification<Mention> findByComment(Long commentId) {
        return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Mention_.comment).get(Comment_.id),
                commentId);
    }

    public static Specification<Mention> existMention(Long mentionId) {
        return (root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(Mention_.id), mentionId);
    }

}
