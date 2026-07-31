package com.container.dataCollect.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "user") // 数据库表名
public class User {
    /**
     * 主键ID
     */
	@Id // 关键：标注为主键
    @Column(name = "user_id")
    private String userId;

    /**
     * 用户名
     */
	@Column(name = "user_name")
    private String userName;


    /**
     * 密码
     */
	@Column(name = "user_pwd")
    private String userPwd;

    /**
     * 状态 1-正常 0-禁用
     */
	@Column(name = "status")
    private Integer status;

    /**
     * 创建时间
     */
	@Column(name = "create_time")
    private String createTime;

    /**
     * 更新时间
     */
	@Column(name = "updateTime")
    private String updateTime;
	
	/**
     * 权限
     */
	@Column(name = "role")
    private int role;
}