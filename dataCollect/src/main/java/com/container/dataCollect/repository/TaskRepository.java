package com.container.dataCollect.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.container.dataCollect.entity.Task;
import com.container.dataCollect.entity.TaskQueryDTO;

@Repository
public interface TaskRepository extends JpaRepository<Task, String>,JpaSpecificationExecutor<Task> {

	// 多条件动态查询
//	@Query("SELECT t FROM Task t WHERE " +
//            "(:taskName IS NULL OR t.taskName LIKE %:taskName%) " +
//            "AND (:unitNo IS NULL OR t.unitNo = :unitNo) " +
//            "AND (:status IS NULL OR t.status = :status) " +
//            "AND (:category IS NULL OR t.category = :category) " +
//            "AND (:claimBy IS NULL OR t.claimBy = :claimBy) " +
//            "AND (:claimed IS NULL OR t.claimed = :claimed)")
//    Page<Task> findByCondition( 
//            @Param("taskName") String taskName,
//            @Param("unitNo") String unitNo,
//            @Param("status") Integer status,
//            @Param("category") String category,
//            @Param("claimBy") String claimBy,
//            @Param("claimed") Integer claimed,
//            Pageable pageable 
//    );
	
	@Query("SELECT t FROM Task t WHERE "
	           + "(:#{#query.category} IS NULL OR t.category = :#{#query.category}) "
	           + "AND (:#{#query.taskName} IS NULL OR t.taskName LIKE %:#{#query.taskName}%) "
	           + "AND (:#{#query.unitNo} IS NULL OR t.unitNo = :#{#query.unitNo}) "
	           + "AND (:#{#query.status} IS NULL OR t.status = :#{#query.status}) "
	           + "AND (:#{#query.claimBy} IS NULL OR t.claimBy = :#{#query.claimBy}) "
	           + "AND (:#{#query.claimed} IS NULL OR t.claimed = :#{#query.claimed}) "
	           + "AND (:#{#query.unitName} IS NULL OR t.unitName like %:#{#query.unitName}%)"
			   + " ORDER BY t.createTime DESC")
	    Page<Task> findByCondition(@Param("query") TaskQueryDTO query, Pageable pageable);
	
	Task findByTaskId(String id);
	
	@Modifying
    @Query("UPDATE Task t SET t.claimBy = :claimBy,t.claimed=1 WHERE t.taskId IN :taskIds and t.claimed = 0")
    int updateClaimByBatch(@Param("taskIds") List<String> taskIds, @Param("claimBy") String claimBy);
}