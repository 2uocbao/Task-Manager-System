package com.quocbao.taskmanagementsystem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quocbao.taskmanagementsystem.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>{

	public User findByEmail(String email);
	
	
	@Query("SELECT "
			+ "u.id AS id, "
			+ "u.firstName AS first, "
			+ "u.lastName AS last, "
			+ "u.image AS image, "
			+ "u.email AS email "
			+ "FROM User u "
			+ "WHERE "
			+ "(u.firstName LIKE LOWER(CONCAT('%', :keySearch, '%')) "
			+ "or u.lastName LIKE LOWER(CONCAT('%', :keySearch, '%')) "
			+ "or u.email LIKE LOWER(CONCAT('%', :keySearch, '%')))  "
			+ "AND u.id <> :userId "
			+ "AND u.id NOT IN ("
			+ "SELECT c.user.id FROM Contacts c WHERE c.friendId.id = :userId "
			+ "UNION "
			+ "SELECT c.friendId.id FROM Contacts c WHERE c.user.id = :userId "
			+ ")")
	public Page<UsersProjections> searchUser(@Param("userId") Long userId, @Param("keySearch") String keySearch, Pageable pageable);
	interface UsersProjections {
		Long getId();
		String getFirst();
		String getLast();
		String getImage();
		String getEmail();
	}
	
	
	@Query("SELECT u.token AS token "
			+ "FROM User u "
			+ "WHERE u.id = :userId")
	public TokenProjections getTokenOfUser(@Param("userId") Long userId);
	interface TokenProjections {
		String getToken();
	}
}
