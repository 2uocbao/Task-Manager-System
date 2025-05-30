package com.quocbao.taskmanagementsystem.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.quocbao.taskmanagementsystem.entity.Notification;

@Repository
public interface NotificationRepository
		extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
	
	List<Notification> findAllByReceiverIdAndIsReadFalse(Long userId);
	
	Notification findByContentIdAndType(Long contentId, String type);
	
	Boolean existsByReceiverIdAndIsReadIsFalse(Long userId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM Notification n WHERE n.contentId = :contentId AND n.type = :type")
	void deleteByReceiverIdOrUserIdAndType(@Param("contentId") Long contentId,
            @Param("type") String type);
	
	@Query("SELECT "
			+ "n.id AS id, "
			+ "n.senderId AS senderId, "
			+ "n.contentId AS contentId, "
			+ "n.isRead AS isRead, "
			+ "n.typeContent AS typeContent, "
			+ "n.type AS type, "
			+ "n.createdAt AS createdAt, "
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.image AS image, "
			+ "t.title AS title "
			+ "FROM Notification n "
			+ "LEFT JOIN User u ON u.id = n.senderId "
			+ "LEFT JOIN Task t ON t.id = n.contentId "
			+ "WHERE n.receiverId = :userId AND n.isRead = :status AND (:type = 'ALL' OR n.type = :type) ")
	Page<NotificationProjections> getNotifications(@Param("userId") Long userId, @Param("type") String type, @Param("status") Boolean status, Pageable pageale);
	interface NotificationProjections {
		Long getId();
		Long getSenderId();
		Long getContentId();
		String getImage();
		String getFirstName();
		String getLastName();
		String getTitleTask();
		Boolean getIsRead();
		String getTypeContent();
		Timestamp getCreatedAt();
		String getType();
	}
	
}
