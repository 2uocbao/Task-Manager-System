package com.quocbao.taskmanagementsystem.repository;

import java.sql.Timestamp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quocbao.taskmanagementsystem.entity.TeamMember;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long>, JpaSpecificationExecutor<TeamMember> {

	@Query("SELECT "
			+ "tm.id AS id, "
			+ "u.id AS userId, "
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.image AS image, "
			+ "tm.joinedAt AS joinedAt "
			+ "FROM TeamMember tm "
			+ "LEFT JOIN User u ON u.id = tm.user.id "
			+ "WHERE tm.team.id = :teamId")
	Page<TeamMemberProjection> getTeamMembers(@Param("teamId") Long teamId, Pageable pageable);

	public interface TeamMemberProjection {
		Long getId();

		Long getUserId();

		String getFirstName();

		String getLastName();

		String getImage();

		Timestamp getJoinedAt();
	}

	@Query("SELECT "
			+ "tm.id AS id, "
			+ "u.id AS userId, "
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.image AS image, "
			+ "tm.joinedAt AS joinedAt "
			+ "FROM TeamMember tm "
			+ "LEFT JOIN User u ON u.id = tm.user.id "
			+ "WHERE"
			+ "(u.firstName LIKE LOWER(CONCAT('%', :keySearch, '%')) "
			+ "or u.lastName LIKE LOWER(CONCAT('%', :keySearch, '%')) "
			+ "or u.email LIKE LOWER(CONCAT('%', :keySearch, '%')))  "
			+ "AND tm.team.id = :teamId")
	Page<SearchMemberProjection> searchTeamMembers(@Param("teamId") Long teamId, @Param("keySearch") String keySearch,
			Pageable pageable);

	interface SearchMemberProjection {
		Long getId();

		Long getUserId();

		String getFirstName();

		String getLastName();

		String getImage();

		Timestamp getJoinedAt();
	}

}
