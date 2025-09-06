package com.quocbao.taskmanagementsystem.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quocbao.taskmanagementsystem.entity.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {

	@Query("SELECT "
			+ "t.id AS id, "
			+ "t.name AS name, "
			+ "t.createdAt AS createdAt, "
			+ "u.id AS leaderId, "
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.image AS image "
			+ "FROM Team t "
			+ "LEFT JOIN User u ON t.leaderId.id = u.id "
			+ "WHERE t.leaderId.id = :userId OR EXISTS " 
			+ "( SELECT 1 "
			+ "FROM TeamMember tm "
			+ "WHERE tm.team.id = t.id "
			+ " AND tm.user.id = :userId )" )
	List<TeamProjection> getTeams(@Param("userId") Long userId);
	interface TeamProjection {
		Long getId();
		String getName();
		Timestamp getCreatedAt();
		Long getLeaderId();
		String getFirstName();
		String getLastName();
		String getImage();
	}
	
	
	@Query("SELECT "
			+ "t.id AS id, "
			+ "t.name AS name, "
			+ "t.createdAt AS createdAt, "
			+ "u.id AS leaderId, "
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.image AS image "
			+ "FROM Team t "
			+ "LEFT JOIN User u ON t.leaderId.id = u.id "
			+ "WHERE (t.leaderId.id = :userId OR EXISTS " 
			+ "( SELECT 1 "
			+ "FROM TeamMember tm "
			+ "WHERE tm.team.id = t.id "
			+ " AND tm.user.id = :userId )) "
			+ "AND t.name LIKE LOWER(CONCAT('%', :keySearch, '%'))" )
	Page<TeamProjection> searchTeams(@Param("userId") Long userId, @Param("keySearch") String keySearch, Pageable pageable);
	interface TeamSearchProjection {
		Long getId();
		String getName();
		Timestamp getCreatedAt();
		Long getLeaderId();
		String getFirstName();
		String getLastName();
		String getImage();
	}
	
	@Query("SELECT "
			+ "t.id AS id, "
			+ "t.name AS name "
			+ "FROM Team t "
			+ "WHERE t.leaderId.id = :userId OR EXISTS "
			+ "( SELECT 1 "
			+ "FROM TeamMember tm "
			+ "WHERE tm.team.id = t.id "
			+ " AND tm.user.id = :userId )" )
	List<TeamCustomProjection> getCustomTeams(@Param("userId") Long userId);
	interface TeamCustomProjection {
		Long getId();
		String getName();
	}
	
}
