package com.container.dataCollect.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.container.dataCollect.entity.User;
import com.container.dataCollect.entity.UserDTO;

@Repository
public interface UserRepository extends JpaRepository<User, String>,JpaSpecificationExecutor<User> {

	@Query("SELECT t FROM User t WHERE " +
		       "( :#{#query.searchName} IS NULL OR t.userName = :#{#query.searchName} ) " +
		       "AND " +
		       "( :#{#query.role} = 1 OR ( :#{#query.role} = 0 AND t.userName = :#{#query.loginName} ) )")
		Page<User> getUserList(@Param("query") UserDTO query, Pageable pageable);

	
	Optional<User> findByUserName(String userName);
	
	Optional<User> findByUserId(String userId);
}