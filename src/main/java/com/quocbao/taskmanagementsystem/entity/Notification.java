package com.quocbao.taskmanagementsystem.entity;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notification")
@Builder
@Setter
@Getter
@AllArgsConstructor
public class Notification implements Serializable {

    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "receiver_id")
	private Long receiverId;
	
	@Column(name = "sender_id")
	private Long senderId;
	
	@Column(name = "content_id")
	private Long contentId;

	@Column(name = "type_content")
	private String typeContent;

	@Column(name = "is_read")
	@Builder.Default
	private boolean isRead = false;
	
	@Column(name = "type")
	private String type;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Timestamp createdAt;

	public Notification() {

	}
}
