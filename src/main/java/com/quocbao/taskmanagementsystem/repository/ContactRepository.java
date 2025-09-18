package com.quocbao.taskmanagementsystem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quocbao.taskmanagementsystem.common.StatusEnum;
import com.quocbao.taskmanagementsystem.entity.Contacts;

@Repository
public interface ContactRepository extends JpaRepository<Contacts, Long>, JpaSpecificationExecutor<Contacts> {

	@Query("SELECT "
			+ "c.id AS id, "
			+ "u.id AS userId, "
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.email AS email, "
			+ "u.image AS image, "
			+ "c.statusEnum AS status "
			+ "FROM Contacts c "
			+ "LEFT JOIN User u ON ("
			+ "(c.user.id = :userId AND c.friendId.id = u.id)"
			+ "OR  "
			+ "(c.friendId.id = :userId AND c.user.id = u.id)"
			+ ") "
			+ "WHERE c.statusEnum = :status AND "
			+ "(u.firstName LIKE LOWER(CONCAT('%', :keySearch, '%')) "
			+ "or u.lastName LIKE LOWER(CONCAT('%', :keySearch, '%')) "
			+ "or u.email LIKE LOWER(CONCAT('%', :keySearch, '%')))  ")
	Page<ContactsProjection> searchContact(@Param("userId") Long userId, @Param("status") StatusEnum status,
			@Param("keySearch") String keySearch, Pageable pageable);

	interface ContactsProjection {
		Long getId();

		Long getUserId();

		String getFirstName();

		String getLastName();

		String getImage();

		String getEmail();

		String getStatus();
	}

	@Query("SELECT "
			+ "c.id AS id, "
			+ "c.statusEnum AS status, "
			+ "u.id AS friendId, "
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.email AS email, "
			+ "u.image AS image "
			+ "FROM Contacts c "
			+ "LEFT JOIN User u ON c.user.id = u.id "
			+ "WHERE c.friendId.id = :userId AND statusEnum = :status")
	Page<ContactReceived> getContactsByReceiver(@Param("userId") Long userId, @Param("status") StatusEnum status,
			Pageable pageable);

	interface ContactReceived {
		Long getId();

		Long getFriendId();

		String getFirstName();

		String getLastName();

		String getEmail();

		String getImage();

		String getStatus();
	}

	@Query("SELECT "
			+ "c.id AS id, "
			+ "c.statusEnum AS status, "
			+ "CASE WHEN c.user.id = :userId THEN c.friendId.id ELSE c.user.id END AS friendId, "
			+ "CASE WHEN c.user.id = :userId THEN f.firstName ELSE u.firstName END AS firstName, "
			+ "CASE WHEN c.user.id = :userId THEN f.lastName ELSE u.lastName END AS lastName, "
			+ "CASE WHEN c.user.id = :userId THEN f.email ELSE u.email END AS email, "
			+ "CASE WHEN c.user.id = :userId THEN f.image ELSE u.image END AS image "
			+ "FROM Contacts c "
			+ "LEFT JOIN User u ON c.user.id = u.id "
			+ "LEFT JOIN User f ON c.friendId.id = f.id "
			+ "WHERE (c.friendId.id = :userId OR c.user.id = :userId) AND statusEnum = :status")
	Page<ContactAccepted> getContactsAccepted(@Param("userId") Long userId, @Param("status") StatusEnum status,
			Pageable pageable);

	interface ContactAccepted {
		Long getId();

		Long getFriendId();

		String getFirstName();

		String getLastName();

		String getEmail();

		String getImage();

		String getStatus();
	}

	@Query("SELECT "
			+ "c.id AS id, "
			+ "c.statusEnum AS status, "
			+ "u.id AS friendId, "
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.email AS email, "
			+ "u.image AS image "
			+ "FROM Contacts c "
			+ "LEFT JOIN User u ON u.id = c.friendId.id "
			+ "WHERE c.user.id = :userId AND c.statusEnum = :status")
	Page<ContactRequested> getContactRequested(@Param("userId") Long userId, @Param("status") StatusEnum status,
			Pageable pageable);

	interface ContactRequested {
		Long getId();

		Long getFriendId();

		String getFirstName();

		String getLastName();

		String getEmail();

		String getImage();

		String getStatus();
	}

	@Query("SELECT "
			+ "u.id AS userId, "
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.email AS email, "
			+ "u.image AS image "
			+ "FROM User u "
			+ "JOIN Contacts c ON "
			+ "("
			+ "(c.user.id = :userId AND c.friendId.id = u.id)"
			+ " OR "
			+ "(c.friendId.id = :userId AND c.user.id = u.id)"
			+ ") AND c.statusEnum = 'ACCEPTED' "
			+ "WHERE "
			+ "u.id NOT IN (SELECT tm.user.id FROM TeamMember tm WHERE tm.team.id = :teamId)"
			+ " AND u.id <> :userId "
			+ "AND "
			+ "(u.firstName LIKE LOWER(CONCAT('%', :keySearch, '%')) "
			+ "or u.lastName LIKE LOWER(CONCAT('%', :keySearch, '%')) "
			+ "or u.email LIKE LOWER(CONCAT('%', :keySearch, '%')))")
	Page<SearchForAdd> searchForAddToTeam(@Param("userId") Long userId, @Param("teamId") Long teamId,
			@Param("keySearch") String keySearch, Pageable pageable);

	interface SearchForAdd {
		Long getUserId();

		String getFirstName();

		String getLastName();

		String getEmail();

		String getImage();
	}
}
