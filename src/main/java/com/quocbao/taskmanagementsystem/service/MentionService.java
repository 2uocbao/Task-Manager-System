package com.quocbao.taskmanagementsystem.service;

import java.util.List;

public interface MentionService {

    public void createMention(Long userId, Long commentId, List<String> mentionIds);

    public void updateMention(Long userId, Long commentId, List<String> mentionIds);
}
