package com.quocbao.taskmanagementsystem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.quocbao.taskmanagementsystem.entity.Chat;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.Tuple;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long>, JpaSpecificationExecutor<Chat> {

	@Query(value = """
						select
						    gm.id,
						    (
				select
					GROUP_CONCAT(u2.first_name separator ' ')
				from
					user u2
				where
					u2.id = (
					select
						mg.user_id
					from
						member mg,
						chat gm
					where
						mg.user_id != u.id
						        )
						    ) as first_names,
						    (
				select
					m.content
				from
					message m,
					chat gm
				where
					m.chat_id = gm.id
				order by
					m.created_at desc
				limit 1
						    ) as last_message,
						    (
				select
					m.created_at
				from
					message m,
					chat gm
				where
					m.chat_id = gm.id
				order by
					m.created_at desc
				limit 1
						    ) as last_message_time
			from
				user u,
				chat gm
			where
				u.id = :userId
						""", nativeQuery = true)

	Page<Tuple> getChatsOfUser(@Param("userId") long userId, Pageable pageable);

}
