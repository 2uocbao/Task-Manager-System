package com.quocbao.taskmanagementsystem.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.PriorityEnum;
import com.quocbao.taskmanagementsystem.common.StatusEnum;
import com.quocbao.taskmanagementsystem.payload.request.TaskRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Entity
@Getter
@Table(name = "task")
@Setter
@AllArgsConstructor
public class Task implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Title can not be blank")
	@Column(name = "title")
	private String title;

	@Column(name = "description")
	private String description;

	@Column(name = "priority")
	@Enumerated(EnumType.STRING)
	private PriorityEnum priority;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private StatusEnum status;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Timestamp createdAt;

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Timestamp updatedAt;

	@Column(name = "due_at")
	private Timestamp dueAt;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assign_to")
	private User assignTo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@PrePersist
	void preInsert() {
		if (this.status == null) {
			this.status = StatusEnum.PENDING;
		}
	}

	public Task() {

	}

	public Task(TaskRequest taskRequest) {
		this.title = taskRequest.getTitle();
		this.description = taskRequest.getDescription();
		this.dueAt = ConvertData.toTimestamp(taskRequest.getDueAt());
		this.priority = PriorityEnum.valueOf(taskRequest.getPriority());
	}

	public Task updateTask(TaskRequest taskRequest) {
		this.title = taskRequest.getTitle();
		this.description = taskRequest.getDescription();
		this.dueAt = ConvertData.toTimestamp(taskRequest.getDueAt());
		this.priority = PriorityEnum.valueOf(taskRequest.getPriority());
		this.status = StatusEnum.valueOf(taskRequest.getStatus());
		return this;
	}
}
