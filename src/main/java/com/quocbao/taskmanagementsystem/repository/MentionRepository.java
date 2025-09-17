package com.quocbao.taskmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.quocbao.taskmanagementsystem.entity.Mention;

@Repository
public interface MentionRepository extends JpaRepository<Mention, Long>, JpaSpecificationExecutor<Mention> {

}
