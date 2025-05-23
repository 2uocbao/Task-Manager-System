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
public interface ContactRepository extends JpaRepository<Contacts, Long>, JpaSpecificationExecutor<Contacts>{

	
	@Query("SELECT "
			+ "c.id AS id, "
			+ "u.id AS userId, "
			+ "u.firstName AS firstName, "
			+ "u.lastName AS lastName, "
			+ "u.email AS email, "
			+ "u.image AS image "
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
	Page<ContactsProjection> searchContact(@Param("userId") Long userId, @Param("status") StatusEnum status, @Param("keySearch") String keySearch, Pageable pageable);
	
	interface ContactsProjection {
		Long getId();
		Long getUserId();
		String getFirstName();
		String getLastName();
		String getImage();
		String getEmail();
	}
	
	@Query("SELECT COUNT(c) > 0 FROM Contacts c " +
		       "WHERE ((c.user.id = :userId1 AND c.friendId.id = :userId2) " +
		       "   OR (c.user.id = :userId2 AND c.friendId.id = :userId1)) " +
		       "AND (c.statusEnum = 'ACCEPTED' OR c.statusEnum = 'REQUESTED')")
		boolean isConnected(@Param("userId1") Long userId1,
		                    @Param("userId2") Long userId2);
}
