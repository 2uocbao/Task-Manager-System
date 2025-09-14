package com.quocbao.taskmanagementsystem.entity;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import org.hibernate.annotations.CreationTimestamp;

import com.quocbao.taskmanagementsystem.common.RoleEnum;

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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "team_member")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class TeamMember implements Serializable {
	/**
	* 
	*/
	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "role")
	@Enumerated(EnumType.STRING)
	private RoleEnum role;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "joined_at")
	private Timestamp joinedAt;

	public TeamMember() {

	}
}
