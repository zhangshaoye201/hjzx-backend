package com.container.dataCollect.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import utils.JsonConverter;
/**
 * 任务信息实体类（JPA 映射）
 * 修复：确保主键字段正确标注 @Id
 */
@Data
@Entity
@Table(name = "task") // 数据库表名
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 
     * 主键ID（核心修复：必须有 @Id 注解）
     * 
     */
    @Id // 关键：标注为主键
    @Column(name = "task_id") // 对应数据库主键列名
    private String taskId;
    
    /** 任务名称 */
    @Column(name = "task_name")
    private String taskName;
    /** 单位内编号 */
    @Column(name = "unit_no")
    private String unitNo;
    /** 创建时间 */
    @Column(name = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTime;
    /** 更新时间 */
    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    /** 提交时间 */
    @Column(name = "submit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;
    /** 创建人 */
    @Column(name = "create_by")
    private String createBy;
    /** 更新人 */
    @Column(name = "update_by")
    private String updateBy;
    /** 状态 */
    @Column(name = "status")
    private Integer status;
    /** 来源 */
    @Column(name = "source")
    private Integer source;
    /** 认领人 */
    @Column(name = "claim_by")
    private String claimBy;
    /** 类型 */
    @Column(name = "category")
    private String category;
    /** 使用单位 */
    @Column(name = "unit_name")
    private String unitName;
    
    /** 资料审查 */
    @Convert(converter = JsonConverter.class)
    @Column(name = "review")
    private Object review;
    /** 宏观 */
    @Convert(converter = JsonConverter.class)
    @Column(name = "macro")
    private Object macro;
    /** 宏观附页 */
    @Convert(converter = JsonConverter.class)
    @Column(name = "macro1")
    private Object macro1;
    /** 安全附件 */
    @Convert(converter = JsonConverter.class)
    @Column(name = "safety")
    private Object safety;
    /** 测厚 */
    @Convert(converter = JsonConverter.class)
    @Column(name = "thickness")
    private Object thickness;
    /** 磁粉 */
    @Convert(converter = JsonConverter.class)
    @Column(name = "magnetic")
    private Object magnetic;
    /** 超声 */
    @Convert(converter = JsonConverter.class)
    @Column(name = "sound")
    private Object sound;
    /** 渗透 */
    @Convert(converter = JsonConverter.class)
    @Column(name = "penetrate")
    private Object penetrate;
    /** 硬度 */
    @Convert(converter = JsonConverter.class)
    @Column(name = "hardness")
    private Object hardness;
    
    
}