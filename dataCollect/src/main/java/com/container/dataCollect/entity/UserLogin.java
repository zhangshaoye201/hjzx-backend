package com.container.dataCollect.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

/**
 * 任务信息实体类（JPA 映射）
 * 修复：确保主键字段正确标注 @Id
 */
@Data
@Entity
@Table(name = "user_login") // 数据库表名
public class UserLogin implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 
     * 主键ID（核心修复：必须有 @Id 注解）
     * 
     */
    @Id // 关键：标注为主键
    @Column(name = "id") // 对应数据库主键列名
    private String id;
    /** 用户名 */
    @Column(name = "user_name") 
    private String userName;
    /** 密码 */
    @Column(name = "password")
    private String password;
    /** 盐 */
    @Column(name = "salt")
    private String salt;
    /** 上次登录时间 */
    @Column(name = "last_login_time")
    private String lastLoginTime;
    
}